package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;

public record CycleOptionResponse(
        Long id,
        String code,
        String name,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
}
