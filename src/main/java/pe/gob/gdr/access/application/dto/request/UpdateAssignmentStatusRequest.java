package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateAssignmentStatusRequest(
        @NotBlank(message = "El estado de la relacion es obligatorio.")
        @Pattern(regexp = "ACTIVE|INACTIVE", message = "El estado de la relacion debe ser ACTIVE o INACTIVE.")
        String status
) {
}
