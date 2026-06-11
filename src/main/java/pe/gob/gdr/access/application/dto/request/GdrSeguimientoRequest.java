package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record GdrSeguimientoRequest(
        @NotNull(message = "assignmentId es obligatorio")
        Long assignmentId,

        @Size(max = 50, message = "tipoReunion no puede superar 50 caracteres")
        String tipoReunion,

        @NotNull(message = "fechaReunion es obligatoria")
        @PastOrPresent(message = "No se puede registrar una reunión con fecha futura")
        LocalDate fechaReunion,

        @Size(max = 2000, message = "descripcionAvance no puede superar 2000 caracteres")
        String descripcionAvance,

        @Size(max = 2000, message = "compromisos no puede superar 2000 caracteres")
        String compromisos
) {}
