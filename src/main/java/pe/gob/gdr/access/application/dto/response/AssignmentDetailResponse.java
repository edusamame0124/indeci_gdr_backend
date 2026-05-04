package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record AssignmentDetailResponse(
        Long id,
        Long cycleId,
        String cycleCode,
        String cycleName,
        String cycleStatus,
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
