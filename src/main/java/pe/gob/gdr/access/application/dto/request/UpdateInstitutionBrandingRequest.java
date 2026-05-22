package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateInstitutionBrandingRequest(
        @NotBlank(message = "El nombre de la institucion es obligatorio.")
        @Size(max = 200, message = "El nombre de la institucion no puede exceder 200 caracteres.")
        String institutionName,

        @Size(max = 80, message = "El nombre corto no puede exceder 80 caracteres.")
        String nombreCorto,

        @Pattern(regexp = "^$|^[0-9]{11}$", message = "El RUC debe tener 11 digitos.")
        String ruc,

        @Size(max = 300, message = "La direccion no puede exceder 300 caracteres.")
        String direccion
) {
}
