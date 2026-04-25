package pe.gob.gdr.access.application.dto.response;

public record HrAssignmentSummaryResponse(
        Long assignmentId,
        Long cycleId,
        String cycleCode,
        String cycleName,
        Long evaluatorPersonId,
        String evaluatorName,
        Long evaluatedPersonId,
        String evaluatedName,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        String status
) {
}
