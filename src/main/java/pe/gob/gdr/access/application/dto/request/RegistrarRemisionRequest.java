package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public record RegistrarRemisionRequest(

        @NotNull(message = "La fecha de remisión es obligatoria.")
        @PastOrPresent(message = "La fecha de remisión no puede ser futura.")
        LocalDate fechaRemision,

        @NotBlank(message = "El canal de remisión es obligatorio.")
        String canalRemision,

        String numeroTramite,

        String observaciones,

        String nombreDocEvidencia,

        String tipoDocEvidencia
) {}
