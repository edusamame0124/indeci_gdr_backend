package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RemisionResponse(
        Long id,
        Long informeCierreId,
        LocalDate fechaRemision,
        String canalRemision,
        String canalRemisionLabel,
        String numeroTramite,
        String observaciones,
        String nombreDocEvidencia,
        String tipoDocEvidencia,
        String tipoDocEvidenciaLabel,
        String estadoRemision,
        String estadoRemisionLabel,
        String registradoPor,
        LocalDateTime createdAt
) {}
