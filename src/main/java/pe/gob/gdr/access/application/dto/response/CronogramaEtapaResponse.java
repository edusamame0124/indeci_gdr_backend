package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;

public record CronogramaEtapaResponse(
        Long id,
        String etapa,
        String etapaLabel,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaFinNormativa,
        String estado,
        boolean vencida,
        long diasRestantes
) {
}
