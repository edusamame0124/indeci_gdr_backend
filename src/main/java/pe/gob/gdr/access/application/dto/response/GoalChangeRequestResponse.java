package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;
import pe.gob.gdr.access.domain.model.GoalChangeRequestStatus;
import pe.gob.gdr.access.domain.model.GoalChangeRequestType;

public record GoalChangeRequestResponse(
        Long id,
        Long goalId,
        Long assignmentId,
        String goalTitle,
        GoalChangeRequestType requestType,
        String requestTypeName,
        String reason,
        String comment,
        GoalChangeRequestStatus status,
        String statusName,
        String requestedByUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
