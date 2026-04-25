package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegistrarSeguimientoMejoraRequest(
        @NotBlank(message = "El comentario de seguimiento es obligatorio.")
        String comentarioSeguimiento
) {
}
