package pe.gob.gdr.access.application.dto.response;

/**
 * Ítem individual del checklist de avance de etapa. Envuelve, en modo consulta
 * (sin lanzar excepción), la misma validación normativa que {@code avanzar-etapa}
 * aplicaría al intentar la transición real.
 */
public record CicloChecklistItemResponse(
        String code,
        String title,
        String status,
        String detail
) {
    public static final String STATUS_PASSED = "PASSED";
    public static final String STATUS_FAILED = "FAILED";
}
