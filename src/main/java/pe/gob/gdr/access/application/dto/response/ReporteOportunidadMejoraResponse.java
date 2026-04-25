package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReporteOportunidadMejoraResponse(
        Long idOportunidadMejora,
        Long idResultado,
        Long idEvaluado,
        String evaluado,
        String evaluador,
        String ciclo,
        String estadoCodigo,
        String estadoNombre,
        String responsable,
        LocalDate plazoCompromiso,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaCierre,
        int totalSeguimientos
) {
}
