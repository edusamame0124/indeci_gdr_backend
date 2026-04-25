package pe.gob.gdr.access.application.dto.response;

import java.util.List;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String username,
        String displayName,
        List<String> roles
) {
}
