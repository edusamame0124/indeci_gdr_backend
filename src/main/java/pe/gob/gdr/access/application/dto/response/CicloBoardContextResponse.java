package pe.gob.gdr.access.application.dto.response;

/**
 * Contexto del tablero GDR para el frontend.
 * Incluye resumen del checklist de planificación para UX (no toda la estructura detallada).
 */
public record CicloBoardContextResponse(
        Long cycleId,
        String estadoEtapa,
        String estadoEtapaLabel,
        boolean canAdvanceToSeguimiento,
        boolean hasExcepcionEdicion,
        String planificacionCompletadaEn,
        String planificacionCompletadaPor,
        // resumen del checklist (igual que PlanningChecklistResponse para compatibilidad)
        boolean cronogramaCompleto,
        boolean seguimientoMinimoSeisMeses,
        boolean participantesRegistrados,
        boolean asignacionesCompletas,
        boolean cieAplica,
        boolean cieConfigurado,
        boolean indicadoresHabilitados,
        boolean metasFormalizadas100,
        boolean notificacionesTomaConocimiento
) {
}
