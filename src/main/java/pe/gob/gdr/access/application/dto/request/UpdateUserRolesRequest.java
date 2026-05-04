package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateUserRolesRequest(
        @NotEmpty(message = "Debe asignar al menos un rol tecnico.")
        List<@NotBlank(message = "El codigo de rol no puede estar vacio.") String> roleCodes
) {
}
