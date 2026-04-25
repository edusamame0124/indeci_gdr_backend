package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record GoalUpsertRequest(
        @NotNull(message = "La asignacion es obligatoria.")
        @Positive(message = "La asignacion debe ser valida.")
        Long assignmentId,
        @NotNull(message = "El indicador es obligatorio.")
        @Positive(message = "El indicador debe ser valido.")
        Long indicatorId,
        @NotBlank(message = "El titulo de la meta es obligatorio.")
        String title,
        String description,
        @NotNull(message = "El valor esperado es obligatorio.")
        @DecimalMin(value = "0.0001", message = "El valor esperado debe ser mayor a cero.")
        BigDecimal expectedValue,
        @NotNull(message = "El peso es obligatorio.")
        @DecimalMin(value = "0.01", message = "El peso debe ser mayor a cero.")
        @DecimalMax(value = "100.00", message = "El peso no puede exceder 100.")
        BigDecimal weight
) {
}
