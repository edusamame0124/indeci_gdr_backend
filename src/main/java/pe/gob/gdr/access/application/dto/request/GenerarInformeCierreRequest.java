package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.Size;

public record GenerarInformeCierreRequest(
        @Size(max = 4000, message = "Las observaciones ORH no pueden superar 4000 caracteres.")
        String observacionesOrh
) {
}
