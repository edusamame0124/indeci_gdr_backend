package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Estado de una solicitud de confirmación de calificación (vista evaluado/ORH). */
public record SolicitudConfirmacionResponse(
        Long id,
        Long finalEvaluationId,
        Long evaluadoId,
        String evaluadoNombre,
        String cicloNombre,
        LocalDateTime fechaSolicitud,
        String sustentoEvaluado,
        String estado,
        String estadoLabel,
        LocalDateTime fechaDerivacionCie,
        LocalDate plazoSolicitudConfirmacion,
        String numeroCaso,
        String casoEstado,
        String decision,
        String calificacionResultado,
        String calificacionResultadoLabel,
        String sustentoCie,
        LocalDateTime fechaDecision
) {
}
