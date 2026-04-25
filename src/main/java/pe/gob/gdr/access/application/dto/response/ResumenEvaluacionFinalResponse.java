package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;

public record ResumenEvaluacionFinalResponse(
        Long assignmentId,
        Long evaluatedId,
        String evaluatedName,
        String evaluatorName,
        String cycleName,
        Long evaluationId,
        BigDecimal consolidatedScore,
        String status
) {
}
