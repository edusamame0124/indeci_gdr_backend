package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserListItemResponse(
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
        LocalDateTime lastLoginAt
) {
}
