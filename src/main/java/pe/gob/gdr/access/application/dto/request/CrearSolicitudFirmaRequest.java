package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearSolicitudFirmaRequest(
        @NotNull(message = "La solicitud de firma es obligatoria.")
        @Positive(message = "La solicitud de firma debe ser valida.")
        Long solicitudFirmaId
) {
}
