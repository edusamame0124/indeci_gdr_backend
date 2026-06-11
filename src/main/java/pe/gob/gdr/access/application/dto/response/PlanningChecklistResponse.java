package pe.gob.gdr.access.application.dto.response;

import java.util.List;

/**
 * Contrato del endpoint GET /admin/cycles/{cycleId}/planning-checklist.
 * Consolidado conforme a la decisión T0-03.
 */
public record PlanningChecklistResponse(
        boolean cronogramaCompleto,
        boolean seguimientoMinimoSeisMeses,
        boolean participantesRegistrados,
        boolean asignacionesCompletas,
        boolean cieAplica,
        boolean cieConfigurado,
        int conteoIntegrantesCie,
        boolean indicadoresHabilitados,
        boolean metasFormalizadas100,
        boolean notificacionesTomaConocimiento,
        int porcentajeAvance,
        List<String> pendientes,
        List<String> bloqueantes
) {
}
