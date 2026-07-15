package pe.gob.gdr.access.infrastructure.integration.sisrh;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pe.gob.gdr.access.application.port.SisrhDirectoryPort;
import pe.gob.gdr.access.application.port.SisrhDirectoryUser;
import pe.gob.gdr.access.infrastructure.config.JwtProperties;

/**
 * Adaptador HTTP hacia el directorio de usuarios GDR_USUARIO del SISRH.
 *
 * Autenticacion sin secretos nuevos: GDR firma un token de servicio corto con
 * el MISMO secreto JWT que ya comparte con SISRH para el SSO (app.jwt.secret).
 * SISRH lo acepta porque autoriza por el claim firmado del token. El token lleva
 * solo la autoridad GDR_DIRECTORY_READ (menor privilegio) y dura pocos minutos.
 *
 * Degrada con elegancia: si la integracion esta apagada, sin URL o el SISRH
 * falla, devuelve lista vacia y el buscador usa solo candidatos locales (nunca
 * rompe el modal de asignacion).
 */
@Component
public class SisrhDirectoryClient implements SisrhDirectoryPort {

    private static final Logger log = LoggerFactory.getLogger(SisrhDirectoryClient.class);
    private static final int MIN_QUERY = 2;
    private static final String SERVICE_SUBJECT = "svc-gdr";
    private static final String DIRECTORY_AUTHORITY = "GDR_DIRECTORY_READ";
    private static final long TOKEN_TTL_MS = 300_000L;   // 5 min
    private static final long TOKEN_SKEW_MS = 60_000L;   // re-firma 1 min antes

    private final SisrhIntegrationProperties props;
    private final SecretKey signingKey;
    private final RestClient restClient;

    private final Object tokenLock = new Object();
    private volatile String cachedToken;
    private volatile long tokenExpiryEpochMs;

    public SisrhDirectoryClient(SisrhIntegrationProperties props, JwtProperties jwtProperties) {
        this.props = props;
        String secret = jwtProperties.getSecret() == null ? "" : jwtProperties.getSecret();
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeoutMs());
        factory.setReadTimeout(props.getReadTimeoutMs());
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(props.getBaseUrl() == null ? "" : props.getBaseUrl())
                .build();
    }

    @Override
    public List<SisrhDirectoryUser> searchGdrUsers(String query) {
        if (!props.isEnabled()) {
            return List.of();
        }
        String q = query == null ? "" : query.trim();
        if (q.length() < MIN_QUERY) {
            return List.of();
        }
        if (!props.isConfigured()) {
            log.warn("[SISRH] Integracion de directorio sin URL base (app.sisrh.base-url); se omite.");
            return List.of();
        }
        try {
            String token = obtenerToken();
            SisrhApiResponse<List<SisrhDirectoryUser>> response = restClient.get()
                    .uri(builder -> builder
                            .path("/api/sistemas/rendimiento/usuarios")
                            .queryParam("q", q)
                            .queryParam("rol", "GDR_USUARIO")
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<SisrhApiResponse<List<SisrhDirectoryUser>>>() {});
            List<SisrhDirectoryUser> data = (response == null || response.data() == null)
                    ? List.of()
                    : response.data();
            log.info("[SISRH] Directorio GDR_USUARIO q='{}' base={} resultados={}",
                    q, props.getBaseUrl(), data.size());
            return data;
        } catch (org.springframework.web.client.RestClientResponseException http) {
            log.warn("[SISRH] Directorio GDR_USUARIO respondio {} ({}): {}",
                    http.getStatusCode(), props.getBaseUrl(), http.getResponseBodyAsString());
            return List.of();
        } catch (Exception ex) {
            log.warn("[SISRH] Fallo la consulta del directorio GDR_USUARIO (base={}): {}",
                    props.getBaseUrl(), ex.toString());
            return List.of();
        }
    }

    /**
     * Token de servicio firmado con el secreto compartido. Se cachea hasta poco
     * antes de expirar para no re-firmar en cada tecla del buscador.
     */
    private String obtenerToken() {
        String current = cachedToken;
        if (current != null && System.currentTimeMillis() < tokenExpiryEpochMs) {
            return current;
        }
        synchronized (tokenLock) {
            if (cachedToken != null && System.currentTimeMillis() < tokenExpiryEpochMs) {
                return cachedToken;
            }
            long now = System.currentTimeMillis();
            String token = Jwts.builder()
                    .subject(SERVICE_SUBJECT)
                    .claim("roles", List.of())
                    .claim("permisos", List.of(DIRECTORY_AUTHORITY))
                    .claim("otpValidado", true)
                    .claim("newPassOk", true)
                    .issuedAt(new Date(now))
                    .expiration(new Date(now + TOKEN_TTL_MS))
                    .signWith(signingKey, Jwts.SIG.HS384)
                    .compact();
            cachedToken = token;
            tokenExpiryEpochMs = now + TOKEN_TTL_MS - TOKEN_SKEW_MS;
            return token;
        }
    }

    private record SisrhApiResponse<T>(String estado, String mensaje, T data) {
    }
}
