package pe.gob.gdr.access.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import pe.gob.gdr.access.infrastructure.config.JwtProperties;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void init() {
        signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String subject, List<String> roles) {
        return Jwts.builder()
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpirationMs()))
                .claim("type", "access")
                .claim("roles", roles)
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpirationMs()))
                .claim("type", "refresh")
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractAllClaims(token).get("type", String.class));
    }

    /**
     * SSO Fase 3 — TRUE si el token proviene del SSO del SISRH: trae el claim
     * anidado "sistemas" (los tokens propios de GDR no lo tienen).
     */
    public boolean isSsoToken(String token) {
        return extractAllClaims(token).get("sistemas") instanceof Map;
    }

    /**
     * SSO Fase 3 — roles que el SISRH asignó a este usuario para el sistema
     * indicado (p. ej. "rendimiento"), leídos del claim anidado
     * sistemas.&lt;codigo&gt;. Para GDR vienen SIN prefijo ROLE_ (el filtro lo
     * antepone al construir authorities). Lista vacía si no aplica.
     */
    public List<String> getSistemaRoles(String token, String sistemaCodigo) {
        Object sistemas = extractAllClaims(token).get("sistemas");
        if (!(sistemas instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object roles = map.get(sistemaCodigo);
        if (!(roles instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).collect(Collectors.toList());
    }

    /**
     * SSO Fase 4 — DNI del titular (claim {@code dni}). Llave puente para
     * crear/vincular el {@code HR_PERSON} local. NULL si el token no lo trae
     * (cuentas técnicas legacy sin persona en el SISRH).
     */
    public String getDni(String token) {
        return extractAllClaims(token).get("dni", String.class);
    }

    /**
     * SSO Fase 4 — nombre completo del titular (claim {@code nombre}). Se usa
     * como {@code DISPLAY_NAME} institucional al crear el HrPerson. NULL si no viene.
     */
    public String getNombre(String token) {
        return extractAllClaims(token).get("nombre", String.class);
    }

    /**
     * SSO Fase 4 — oficina asignada en el SISRH para el sistema indicado, leída
     * del claim anidado areas.&lt;codigo&gt;. Equivale al {@code UNIT_CODE} de
     * HR_ORG_UNIT. NULL si no aplica.
     */
    public String getSistemaArea(String token, String sistemaCodigo) {
        Object areas = extractAllClaims(token).get("areas");
        if (!(areas instanceof Map<?, ?> map)) {
            return null;
        }
        Object area = map.get(sistemaCodigo);
        return area != null ? String.valueOf(area) : null;
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractAllClaims(token).get("type", String.class));
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
