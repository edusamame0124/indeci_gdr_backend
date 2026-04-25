package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "El refreshToken es obligatorio")
        String refreshToken
) {
}
