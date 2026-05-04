package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "El correo electronico es obligatorio.")
        @Email(message = "El correo electronico no tiene un formato valido.")
        @Size(max = 150, message = "El correo electronico no puede exceder 150 caracteres.")
        String email,
        @NotBlank(message = "El nombre visible es obligatorio.")
        @Size(max = 150, message = "El nombre visible no puede exceder 150 caracteres.")
        String displayName
) {
}
