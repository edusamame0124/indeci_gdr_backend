package pe.gob.gdr.access.application.dto.response;

import java.util.List;

public record UserSessionResponse(
        String username,
        String email,
        String displayName,
        List<String> roles,
        ActiveCycleContextResponse context,
        FeatureAccessResponse featureAccess
) {
}
