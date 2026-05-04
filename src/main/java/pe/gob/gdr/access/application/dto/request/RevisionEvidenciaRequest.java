package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RevisionEvidenciaRequest(
        @NotBlank(message = "La calificacion de la evidencia es obligatoria.")
        String qualificationCode,
        String comment,
        String correctiveActionDetail
) {
}
