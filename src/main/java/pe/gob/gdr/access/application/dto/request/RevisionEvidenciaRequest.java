package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RevisionEvidenciaRequest(
        @NotBlank(message = "La decision de revision es obligatoria.")
        String decisionCode,
        String comment,
        String correctiveActionDetail
) {
}
