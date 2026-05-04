package pe.gob.gdr.access.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {

    /**
     * Remitente institucional por defecto si el mensaje no indica remitente explícito.
     */
    private String defaultFrom;

    public String getDefaultFrom() {
        return defaultFrom;
    }

    public void setDefaultFrom(String defaultFrom) {
        this.defaultFrom = defaultFrom;
    }
}
