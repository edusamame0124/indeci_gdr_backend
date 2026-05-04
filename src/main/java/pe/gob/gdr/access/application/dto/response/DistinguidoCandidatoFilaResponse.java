package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;

public record DistinguidoCandidatoFilaResponse(
        Long assignmentId,
        Long finalEvaluationId,
        Long evaluatedPersonId,
        String evaluatedDisplayName,
        BigDecimal consolidatedScore,
        String qualitativeRatingCode,
        String qualitativeRatingLabel,
        boolean qualRatingNotified,
        boolean directive82Compliance,
        boolean eligibleForDistinguidoPool,
        int rankEligible,
        boolean alreadyDistinguido
) {
}
