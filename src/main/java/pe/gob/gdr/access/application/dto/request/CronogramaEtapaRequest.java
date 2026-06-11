package pe.gob.gdr.access.application.dto.request;

import java.time.LocalDate;

public record CronogramaEtapaRequest(
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
