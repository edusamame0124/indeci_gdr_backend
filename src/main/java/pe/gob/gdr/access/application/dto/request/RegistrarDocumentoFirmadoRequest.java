package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistrarDocumentoFirmadoRequest(
        @NotNull(message = "El evaluado es obligatorio.")
        @Positive(message = "El evaluado debe ser valido.")
        Long evaluatedId,
        @NotBlank(message = "La descripcion es obligatoria.")
        @Size(max = 400, message = "La descripcion no puede exceder 400 caracteres.")
        String descripcion
) {
}
