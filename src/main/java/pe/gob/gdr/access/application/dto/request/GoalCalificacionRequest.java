package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record GoalCalificacionRequest(
        @NotNull(message = "El valor alcanzado es obligatorio.")
        @DecimalMin(value = "0", inclusive = true, message = "El valor alcanzado no puede ser negativo.")
        BigDecimal achievedValue
) {
}
