package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record RegistrarOportunidadMejoraRequest(
        @NotNull(message = "El evaluado es obligatorio.")
        @Positive(message = "El evaluado debe ser valido.")
        Long evaluatedId,
        @NotBlank(message = "La descripcion es obligatoria.")
        String descripcion,
        @NotBlank(message = "El responsable es obligatorio.")
        String responsable,
        @NotNull(message = "El plazo compromiso es obligatorio.")
        @FutureOrPresent(message = "El plazo compromiso no puede estar en el pasado.")
        LocalDate plazoCompromiso,
        String estadoCodigo,
        String comentarioCierre
) {
}
