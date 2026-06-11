package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DetalleEvaluacionFinalResponse(
        Long evaluationId,
        Long assignmentId,
        Long evaluatedId,
        String evaluatedName,
        String evaluatorName,
        String cycleName,
        BigDecimal consolidatedScore,
        String qualitativeRatingCode,
        String qualitativeRatingLabel,
        String segmentCode,
        String segmentName,
        String status,
        String evaluationComment,
        LocalDate fechaReunionRetroFinal,
        LocalDate plazoSolicitudConfirmacion,
        Integer diasHabilesRestantesConfirmacion,
        List<DetallePuntajeResponse> details
) {
}
