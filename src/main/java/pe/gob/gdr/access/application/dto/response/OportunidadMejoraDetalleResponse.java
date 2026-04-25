package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OportunidadMejoraDetalleResponse(
        Long idOportunidadMejora,
        Long idResultado,
        Long idEvaluado,
        String evaluado,
        String evaluador,
        String ciclo,
        String descripcion,
        String responsable,
        LocalDate plazoCompromiso,
        String codigoEstado,
        String nombreEstado,
        String comentarioCierre,
        LocalDateTime fechaCierre,
        LocalDateTime fechaCreacion,
        List<SeguimientoMejoraResponse> seguimientos
) {
}
