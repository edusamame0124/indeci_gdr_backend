package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OportunidadMejoraResumenResponse(
        Long idOportunidadMejora,
        Long idResultado,
        Long idEvaluado,
        String evaluado,
        String descripcion,
        String responsable,
        LocalDate plazoCompromiso,
        String codigoEstado,
        String nombreEstado,
        LocalDateTime fechaCreacion
) {
}
