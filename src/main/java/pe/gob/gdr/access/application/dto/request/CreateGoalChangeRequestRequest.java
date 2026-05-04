package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.gob.gdr.access.domain.model.GoalChangeRequestType;

public record CreateGoalChangeRequestRequest(
        @NotNull(message = "El tipo de modificacion es obligatorio.")
        GoalChangeRequestType requestType,
        @NotBlank(message = "El motivo de la solicitud es obligatorio.")
        @Size(max = 1000, message = "El motivo de la solicitud no puede exceder 1000 caracteres.")
        String reason,
        @Size(max = 1000, message = "El comentario de la solicitud no puede exceder 1000 caracteres.")
        String comment
) {
}
