package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;
import pe.gob.gdr.access.domain.model.GoalOrhSubmissionStatus;

public record OrhGoalSubmissionItemResponse(
        Long id,
        Long goalId,
        Long assignmentId,
        String goalTitle,
        String evaluatedName,
        String indicatorName,
        String submittedByUsername,
        String submittedFunctionalActor,
        String comment,
        GoalOrhSubmissionStatus status,
        String statusName,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String reviewedByUsername,
        String orhReviewComment
) {
}
