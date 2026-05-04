package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record AssignmentListItemResponse(
        Long id,
        Long cycleId,
        String cycleCode,
        String cycleName,
        AssignmentPersonRefResponse evaluator,
        AssignmentPersonRefResponse evaluated,
        Long segmentId,
        String segmentCode,
        String segmentName,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
