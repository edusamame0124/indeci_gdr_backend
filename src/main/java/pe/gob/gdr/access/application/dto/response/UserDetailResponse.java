package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserDetailResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String status,
        Long personId,
        String personDisplayName,
        String orgUnitName,
        List<RoleOptionResponse> roles,
        String gdrParticipationStatus,
        String gdrParticipationLabel,
        String functionalActor,
        boolean cycleContextAssigned,
        Integer failedAttempts,
        LocalDateTime lockedUntil,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
