package pe.gob.gdr.access.infrastructure.integration.sisrh;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuracion de la integracion GDR -> SISRH (directorio de usuarios
 * GDR_USUARIO). No requiere secretos nuevos: la autenticacion reutiliza el
 * secreto JWT ya compartido con SISRH para el SSO (app.jwt.secret). Lo unico a
 * configurar es la URL base del SISRH (en Docker, el nombre del servicio).
 */
@Component
@ConfigurationProperties(prefix = "app.sisrh")
public class SisrhIntegrationProperties {

    /** Si es false, el buscador usa solo candidatos locales (integracion apagada). */
    private boolean enabled = true;

    /** URL base del backend SISRH (p. ej. http://indeci-backend:8080). */
    private String baseUrl;

    private int connectTimeoutMs = 5000;

    private int readTimeoutMs = 15000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank();
    }
}
