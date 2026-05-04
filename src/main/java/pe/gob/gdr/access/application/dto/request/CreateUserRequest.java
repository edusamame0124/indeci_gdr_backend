package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateUserRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio.")
        @Size(max = 60, message = "El nombre de usuario no puede exceder 60 caracteres.")
        String username,
        @NotBlank(message = "El correo electronico es obligatorio.")
        @Email(message = "El correo electronico no tiene un formato valido.")
        @Size(max = 150, message = "El correo electronico no puede exceder 150 caracteres.")
        String email,
        @NotBlank(message = "El nombre visible es obligatorio.")
        @Size(max = 150, message = "El nombre visible no puede exceder 150 caracteres.")
        String displayName,
        @NotBlank(message = "La contrasena inicial es obligatoria.")
        @Size(max = 128, message = "La contrasena inicial no puede exceder 128 caracteres.")
        String initialPassword,
        @NotBlank(message = "El DNI es obligatorio.")
        @Size(max = 20, message = "El numero de documento no puede exceder 20 caracteres.")
        String documentNumber,
        @NotNull(message = "La unidad organica es obligatoria.")
        Long orgUnitId,
        @NotEmpty(message = "Debe asignar al menos un rol tecnico.")
        List<@NotBlank(message = "El codigo de rol no puede estar vacio.") String> roleCodes
) {
}
