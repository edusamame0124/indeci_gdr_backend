package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;
import pe.gob.gdr.access.domain.model.GoalChangeRequestStatus;
import pe.gob.gdr.access.domain.model.GoalChangeRequestType;

public record OrhGoalChangeRequestItemResponse(
        Long id,
        Long goalId,
        Long assignmentId,
        String goalTitle,
        String evaluatedName,
        String indicatorName,
        GoalChangeRequestType requestType,
        String requestTypeName,
        String reason,
        String requestedByUsername,
        GoalChangeRequestStatus status,
        String statusName,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        String reviewedByUsername,
        String orhReviewComment
) {
}
