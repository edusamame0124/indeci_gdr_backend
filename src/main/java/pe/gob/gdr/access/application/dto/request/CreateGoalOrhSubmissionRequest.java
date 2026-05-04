package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.Size;

public record CreateGoalOrhSubmissionRequest(
        @Size(max = 1000, message = "El comentario del envio a ORH no puede exceder 1000 caracteres.")
        String comment
) {
}
