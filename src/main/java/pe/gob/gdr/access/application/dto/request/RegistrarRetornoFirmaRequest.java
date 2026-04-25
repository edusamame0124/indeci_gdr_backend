package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegistrarRetornoFirmaRequest(
        @NotBlank(message = "El estado de retorno es obligatorio.")
        String codigoEstadoFlujo,
        String codigoResultadoFirma,
        String mensajeResultadoFirma
) {
}
