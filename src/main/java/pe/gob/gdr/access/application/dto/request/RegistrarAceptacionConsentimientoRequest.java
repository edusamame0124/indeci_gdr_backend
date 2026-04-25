package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarAceptacionConsentimientoRequest(
        @NotNull(message = "El tipo de consentimiento es obligatorio.")
        Long idTipoConsentimiento,
        @Size(max = 1000, message = "El detalle de aceptacion no puede exceder 1000 caracteres.")
        String detalleAceptacion
) {
}
