package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

/** Vista consolidada del informe de cierre (preview o persistido). */
public record InformeCierreConsolidadoResponse(
        Long informeId,
        Long cycleId,
        String cycleName,
        String estado,
        String estadoLabel,
        int totalEvaluados,
        int totalBuenRendimiento,
        int totalSujetoObservacion,
        int totalDesaprobado,
        int totalDistinguido,
        int totalOportunidadesMejora,
        int totalConfirmaciones,
        int totalConfirmacionesResueltas,
        int totalDocumentosFirmados,
        String observacionesOrh,
        String generadoPor,
        LocalDateTime fechaGeneracion
) {
}
