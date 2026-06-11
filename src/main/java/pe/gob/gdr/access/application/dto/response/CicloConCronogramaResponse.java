package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CicloConCronogramaResponse(
        Long id,
        String code,
        String name,
        String status,
        String estadoEtapa,
        String estadoEtapaLabel,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate fechaFinSeguimiento,
        LocalDate fechaFinEvaluacion,
        LocalDate fechaLimiteInforme,
        boolean active,
        List<CronogramaEtapaResponse> cronograma,
        List<String> transicionesDisponibles
) {
}
