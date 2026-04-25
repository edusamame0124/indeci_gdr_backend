package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegistrarDocumentoFirmadoRequest(
        @NotNull(message = "El evaluado es obligatorio.")
        @Positive(message = "El evaluado debe ser valido.")
        Long evaluatedId,
        @NotNull(message = "El tipo documental es obligatorio.")
        @Positive(message = "El tipo documental debe ser valido.")
        Long tipoDocumentoId
) {
}
