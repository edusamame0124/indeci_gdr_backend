package pe.gob.gdr.access.application.dto.response;

public record AssignmentPersonOptionResponse(
        Long personId,
        String documentNumber,
        String displayName,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName
) {
}
