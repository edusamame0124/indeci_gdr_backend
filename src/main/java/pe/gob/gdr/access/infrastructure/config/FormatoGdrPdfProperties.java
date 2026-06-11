package pe.gob.gdr.access.infrastructure.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del PDF generado del Formato GDR; ver {@code docs/formato_gdr/README.md}.
 */
@ConfigurationProperties(prefix = "app.formato-gdr-pdf")
public class FormatoGdrPdfProperties {

    /**
     * Nombre de la entidad en la franja superior del formato (texto institucional configurable).
     */
    private String entityName = "Entidad publica";

    private String formVersion = "2025";

    private String formRevision = "RPE 000041-2025/PE";

    /** Referencia normativa persistida en DOC_VERSION al registrar documentos GDR. */
    private String normativeReference = "RPE N.° 000041-2025/PE";

    private String servirFooterNote =
            "AUTORIDAD NACIONAL DEL SERVICIO CIVIL - SERVIR";

    /**
     * Recurso del logo institucional (prefijo {@code classpath:} recomendado en JAR).
     */
    private String headerLogoPath = "classpath:assets/formato_gdr/logo_institucional.png";

    /**
     * Leyenda bajo el nombre de la entidad en el encabezado enmarcado.
     */
    private String entityTagline = "Comprando para la Seguridad y Defensa Nacional";

    /**
     * Texto mostrado en el PDF cuando aún no existe resultado consolidado (vista previa / seguimiento).
     */
    private String draftBannerNote =
            "BORRADOR: Datos según avance al momento. Cierre de evaluación pendiente; no sustituye acta consolidada.";

    /**
     * Equivalencias código/nombre de segmento (MAYÚSCULAS) → literal de lista institucional del formato.
     */
    private Map<String, String> segmentDisplayAliases = new LinkedHashMap<>();

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getHeaderLogoPath() {
        return headerLogoPath;
    }

    public void setHeaderLogoPath(String headerLogoPath) {
        this.headerLogoPath = headerLogoPath;
    }

    public String getEntityTagline() {
        return entityTagline;
    }

    public void setEntityTagline(String entityTagline) {
        this.entityTagline = entityTagline;
    }

    public String getFormVersion() {
        return formVersion;
    }

    public void setFormVersion(String formVersion) {
        this.formVersion = formVersion;
    }

    public String getFormRevision() {
        return formRevision;
    }

    public void setFormRevision(String formRevision) {
        this.formRevision = formRevision;
    }

    public String getNormativeReference() {
        return normativeReference;
    }

    public void setNormativeReference(String normativeReference) {
        this.normativeReference = normativeReference;
    }

    public String getServirFooterNote() {
        return servirFooterNote;
    }

    public void setServirFooterNote(String servirFooterNote) {
        this.servirFooterNote = servirFooterNote;
    }

    public Map<String, String> getSegmentDisplayAliases() {
        return segmentDisplayAliases;
    }

    public void setSegmentDisplayAliases(Map<String, String> segmentDisplayAliases) {
        this.segmentDisplayAliases =
                segmentDisplayAliases != null ? new LinkedHashMap<>(segmentDisplayAliases) : new LinkedHashMap<>();
    }

    public String getDraftBannerNote() {
        return draftBannerNote;
    }

    public void setDraftBannerNote(String draftBannerNote) {
        this.draftBannerNote = draftBannerNote != null ? draftBannerNote : "";
    }
}
