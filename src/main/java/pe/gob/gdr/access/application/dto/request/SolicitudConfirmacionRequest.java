package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de confirmación de calificación presentada por el evaluado.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 41.
 */
public record SolicitudConfirmacionRequest(
        @NotNull(message = "La evaluación final es obligatoria.")
        Long finalEvaluationId,

        @NotBlank(message = "El sustento de la solicitud es obligatorio.")
        @Size(max = 2000, message = "El sustento no puede superar los 2000 caracteres.")
        String sustento
) {
}
