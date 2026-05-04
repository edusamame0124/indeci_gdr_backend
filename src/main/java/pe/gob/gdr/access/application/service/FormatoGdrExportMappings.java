package pe.gob.gdr.access.application.service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.model.GdrFormula;
import pe.gob.gdr.access.domain.model.GdrGoal;

/**
 * Literales y reglas de presentación compartidas para exportación del Formato GDR (PDF u otros).
 * Deben mantenerse alineados a la plantilla institucional y a {@code docs/formato_gdr/}.
 */
public final class FormatoGdrExportMappings {

    public static final String SELECT_PLACEHOLDER = "[Seleccione]";

    private static final Set<String> EVALUATED_SEGMENT_LIST_LITERALS = Set.of(
            "DIRECTIVO",
            "MANDO MEDIO",
            "EJECUTOR",
            "OPERADOR Y DE ASISTENCIA"
    );

    private static final Locale INSTITUTIONAL_LOCALE = Locale.forLanguageTag("es-PE");

    private FormatoGdrExportMappings() {
    }

    static String resolveEvaluatedSegmentDisplayLiteral(
            GdrEvaluationAssignment assignment,
            Map<String, String> aliases
    ) {
        if (assignment == null || assignment.getSegment() == null) {
            return SELECT_PLACEHOLDER;
        }
        Map<String, String> safe = aliases == null ? Map.of() : aliases;
        String codeKey = normalizeKey(assignment.getSegment().getCode());
        String nameKey = normalizeKey(assignment.getSegment().getName());
        String mapped = firstNonBlankMapped(safe, codeKey, nameKey);
        if (mapped != null && EVALUATED_SEGMENT_LIST_LITERALS.contains(mapped)) {
            return mapped;
        }
        String name = assignment.getSegment().getName();
        if (name == null || name.isBlank()) {
            return SELECT_PLACEHOLDER;
        }
        String trimmed = name.trim();
        for (String literal : EVALUATED_SEGMENT_LIST_LITERALS) {
            if (literal.equalsIgnoreCase(trimmed)) {
                return literal;
            }
        }
        return SELECT_PLACEHOLDER;
    }

    private static String normalizeKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.trim().toUpperCase(INSTITUTIONAL_LOCALE);
    }

    private static String firstNonBlankMapped(Map<String, String> aliases, String codeKey, String nameKey) {
        if (!codeKey.isEmpty()) {
            String v = aliases.get(codeKey);
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        if (!nameKey.isEmpty()) {
            String v = aliases.get(nameKey);
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    public static String mapDirective82ToBook(String directive82Compliance) {
        if (directive82Compliance == null) {
            return SELECT_PLACEHOLDER;
        }
        return switch (directive82Compliance.trim().toUpperCase(Locale.ROOT)) {
            case "Y" -> "Sí";
            case "N" -> "No";
            default -> SELECT_PLACEHOLDER;
        };
    }

    /** Listas de sentido del indicador — coherente con validación institucional del formato. */
    public static String mapIndicatorFormulaToSentido(GdrGoal goal) {
        GdrFormula formula = goal.getIndicator().getFormula();
        String code = formula.getCode() != null ? formula.getCode().toUpperCase(Locale.ROOT) : "";
        String name = formula.getName() != null ? formula.getName().toUpperCase(Locale.ROOT) : "";
        boolean inverse = code.contains("INV") || code.contains("DESC") || name.contains("INV")
                || name.contains("DESCEND");
        return inverse ? "Descendente" : "Ascendente";
    }

    public static String mapSeguimientoLiteral(GdrEvidence latest) {
        if (latest == null) {
            return "No presenta evidencia";
        }
        String code = latest.getEvidenceStatus().getStatusCode().toUpperCase(Locale.ROOT);
        return switch (code) {
            case "APPROVED" -> "Logrado";
            case "OBSERVED", "SUBSANATED", "REGISTERED" -> "En proceso de logro";
            default -> SELECT_PLACEHOLDER;
        };
    }

    public static String mapEvidenciaFinalLiteral(GdrEvidence latest) {
        if (latest == null) {
            return "NO presenta evidencia de logro final";
        }
        return "APPROVED".equalsIgnoreCase(latest.getEvidenceStatus().getStatusCode())
                ? "Sí presenta evidencia final"
                : "NO presenta evidencia de logro final";
    }

    public static String qualitativeRatingLabel(String code) {
        if (code == null || code.isBlank()) {
            return "—";
        }
        return switch (code.trim().toUpperCase(Locale.ROOT)) {
            case "BUEN_RENDIMIENTO" -> "Buen rendimiento";
            case "SUJETO_OBSERVACION" -> "Sujeto a observación";
            case "DISTINGUIDO" -> "Rendimiento distinguido";
            case "DESAPROBADO" -> "Desaprobado";
            case "NO_CALIFICABLE" -> "No calificable";
            default -> code;
        };
    }
}
