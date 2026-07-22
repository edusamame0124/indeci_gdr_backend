package pe.gob.gdr.access.application.dto.response;

import java.util.List;

/**
 * Checklist de requisitos para la SIGUIENTE transición de etapa del ciclo
 * (la que devolvería {@code transicionesDisponibles}). Se calcula reutilizando
 * las mismas validaciones normativas que aplica {@code avanzar-etapa}, en modo
 * consulta (no lanza excepción), para que el frontend pueda mostrarlas ANTES de
 * que ORH intente el avance, en cualquier etapa del ciclo (no solo Planificación).
 */
public record CicloAvanceChecklistResponse(
        boolean canAdvance,
        List<CicloChecklistItemResponse> items
) {
}
