package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;

public record ReporteResultadoResponse(
        Long idResultado,
        Long idEvaluado,
        String evaluado,
        String evaluador,
        String ciclo,
        BigDecimal puntajeFinal,
        BigDecimal puntajeResultado,
        String comentarioEvaluacion,
        int documentosFirmados,
        int oportunidadesMejora
) {
}
