package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IndicatorUpsertRequest(
        @NotBlank(message = "El nombre del indicador es obligatorio.")
        String name,
        String description,
        @NotNull(message = "El tipo de valor es obligatorio.")
        @Positive(message = "El tipo de valor debe ser valido.")
        Long valueTypeId,
        @NotNull(message = "La formula es obligatoria.")
        @Positive(message = "La formula debe ser valida.")
        Long formulaId,
        @NotNull(message = "El segmento es obligatorio.")
        @Positive(message = "El segmento debe ser valido.")
        Long segmentId
) {
}
