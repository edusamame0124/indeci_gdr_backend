package pe.gob.gdr.access.application.dto.response;

public record ParticipantSummaryResponse(
    Long personId,
    String displayName,
    String documentNumber,
    String role,
    Integer evaluatorCount,
    Integer evaluatedCount
) {}
