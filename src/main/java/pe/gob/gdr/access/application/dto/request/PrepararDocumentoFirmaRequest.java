package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PrepararDocumentoFirmaRequest(
        @NotNull(message = "El evaluado es obligatorio.")
        @Positive(message = "El evaluado debe ser valido.")
        Long evaluatedId,
        @NotNull(message = "La plantilla es obligatoria.")
        @Positive(message = "La plantilla debe ser valida.")
        Long plantillaId
) {
}
