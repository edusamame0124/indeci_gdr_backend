package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El loginId es obligatorio")
        String loginId,
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
