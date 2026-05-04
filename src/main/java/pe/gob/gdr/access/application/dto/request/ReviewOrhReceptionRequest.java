package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.Size;

public record ReviewOrhReceptionRequest(
        @Size(max = 1000, message = "El comentario de revision ORH no puede exceder 1000 caracteres.")
        String comment
) {
}
