package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;
import pe.gob.gdr.access.domain.model.GoalOrhSubmissionStatus;

public record GoalOrhSubmissionResponse(
        Long id,
        Long goalId,
        Long assignmentId,
        String goalTitle,
        GoalOrhSubmissionStatus status,
        String statusName,
        String submittedByUsername,
        String submittedFunctionalActor,
        String comment,
        LocalDateTime submittedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
