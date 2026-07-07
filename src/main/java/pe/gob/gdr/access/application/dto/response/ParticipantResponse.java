package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record ParticipantResponse(
    Long participantId,
    Long cycleId,
    Long personId,
    String displayName,
    String documentNumber,
    String orgUnitName,
    String role,
    String status,
    LocalDateTime createdAt
) {}
