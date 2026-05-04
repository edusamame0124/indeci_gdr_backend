package pe.gob.gdr.access.application.dto.response;

public record AssignmentSummaryByPersonResponse(
        Long personId,
        String documentNumber,
        String displayName,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        long asEvaluatorCount,
        long asEvaluatedCount,
        String resolvedFunctionalActor
) {
}
