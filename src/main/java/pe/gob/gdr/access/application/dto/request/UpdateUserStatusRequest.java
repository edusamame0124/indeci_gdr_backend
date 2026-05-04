package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserStatusRequest(
        @NotBlank(message = "El estado del usuario es obligatorio.")
        @Pattern(regexp = "ACTIVE|INACTIVE", message = "El estado del usuario debe ser ACTIVE o INACTIVE.")
        String status
) {
}
