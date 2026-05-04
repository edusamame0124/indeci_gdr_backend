package pe.gob.gdr.access.application.service;

/**
 * Etiquetas que deben coincidir con las cadenas usadas en fórmulas del libro Excel institucional
 * (FORMATO_GDR_XLSX_MAPPING §5–6); difieren de {@code QualitativeRating#label()} en DISTINGUIDO y
 * NO_CALIFICABLE.
 */
public final class QualitativeRatingSpreadsheetLabels {

    private QualitativeRatingSpreadsheetLabels() {
    }

    public static String forQualitativeCode(String qualitativeRatingCode) {
        if (qualitativeRatingCode == null || qualitativeRatingCode.isBlank()) {
            return "";
        }
        return switch (qualitativeRatingCode) {
            case "BUEN_RENDIMIENTO" -> "Buen rendimiento";
            case "SUJETO_OBSERVACION" -> "Rendimiento sujeto a observación";
            case "DISTINGUIDO" -> "Rendimiento Distinguido";
            case "DESAPROBADO" -> "Desaprobado";
            case "NO_CALIFICABLE" -> "No corresponde calificación";
            default -> {
                String legacy = pe.gob.gdr.access.domain.policy.QualitativeRating.labelOf(qualitativeRatingCode);
                yield legacy != null ? legacy : "";
            }
        };
    }
}
