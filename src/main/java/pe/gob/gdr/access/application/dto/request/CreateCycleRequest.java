package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateCycleRequest(

        @NotBlank(message = "El código del ciclo es obligatorio.")
        @Size(max = 40, message = "El código no puede superar 40 caracteres.")
        String code,

        @NotBlank(message = "El nombre del ciclo es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
        String name,

        @NotNull(message = "La fecha de inicio es obligatoria.")
        LocalDate startDate,

        @NotNull(message = "La fecha de fin es obligatoria.")
        LocalDate endDate
) {
}
