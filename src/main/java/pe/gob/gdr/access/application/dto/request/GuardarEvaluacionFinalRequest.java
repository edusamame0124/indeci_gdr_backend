package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record GuardarEvaluacionFinalRequest(
        @NotNull(message = "La asignacion es obligatoria.")
        @Positive(message = "La asignacion debe ser valida.")
        Long assignmentId,
        String evaluationComment,
        @NotEmpty(message = "Debe registrar al menos un detalle de evaluacion.")
        List<@Valid DetallePuntajeInput> details
) {

    public record DetallePuntajeInput(
            @NotNull(message = "La meta es obligatoria.")
            @Positive(message = "La meta debe ser valida.")
            Long goalId,
            @NotNull(message = "El valor alcanzado es obligatorio.")
            @DecimalMin(value = "0.00", inclusive = true, message = "El valor alcanzado no puede ser negativo.")
            BigDecimal achievedValue,
            String detailComment
    ) {
    }
}
