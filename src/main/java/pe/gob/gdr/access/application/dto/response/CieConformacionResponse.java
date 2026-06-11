package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CieConformacionResponse(
        Long id,
        Long cycleId,
        String cycleNombre,
        String resolucionNumero,
        LocalDate resolucionFecha,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFin,
        String observaciones,
        String estado,
        String estadoLabel,
        String registradoPor,
        LocalDateTime createdAt,
        List<IntegranteDto> integrantes
) {
    public record IntegranteDto(
            Long id,
            String rolCie,
            String rolCieLabel,
            String segmento,
            Long idPersona,
            String nombreDisplay,
            String cargoDescripcion,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String estado
    ) {}
}
