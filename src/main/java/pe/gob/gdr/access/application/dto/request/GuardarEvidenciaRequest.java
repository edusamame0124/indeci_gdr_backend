package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record GuardarEvidenciaRequest(
        @NotBlank(message = "El titulo de la evidencia es obligatorio.")
        String title,
        String detail,
        @NotBlank(message = "El tipo de evidencia es obligatorio.")
        String evidenceTypeCode,
        @NotBlank(message = "El formato esperado es obligatorio.")
        String expectedFormatCode,
        LocalDate expectedDate
) {
}
