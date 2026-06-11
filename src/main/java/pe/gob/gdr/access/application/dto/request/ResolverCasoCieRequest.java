package pe.gob.gdr.access.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Decisión del CIE sobre un caso de confirmación de calificación.
 * Normativa: RPE 068-2020-SERVIR-PE Art. 42 (decisión definitiva).
 */
public record ResolverCasoCieRequest(
        @NotBlank(message = "La decisión del CIE es obligatoria.")
        @Pattern(regexp = "CONFIRMA|MODIFICA", message = "La decisión debe ser CONFIRMA o MODIFICA.")
        String decision,

        @Size(max = 40, message = "La calificación no puede superar los 40 caracteres.")
        String calificacionResultado,

        @NotBlank(message = "El sustento de la decisión del CIE es obligatorio.")
        @Size(max = 2000, message = "El sustento no puede superar los 2000 caracteres.")
        String sustentoCie
) {
}
