package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

/**
 * Registro de la reunión de retroalimentación final de la evaluación
 * (RPE 068-2020 Art. 33-39). Activa el plazo de solicitud de confirmación.
 */
public record RegistrarRetroFinalRequest(
        @NotNull(message = "La fecha de reunión de retroalimentación final es obligatoria.")
        @PastOrPresent(message = "La fecha de reunión de retroalimentación final no puede ser futura.")
        LocalDate fechaReunionRetroFinal
) {
}
