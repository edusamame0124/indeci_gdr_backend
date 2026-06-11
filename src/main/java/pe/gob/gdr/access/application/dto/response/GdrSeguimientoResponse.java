package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GdrSeguimientoResponse(
        Long id,
        Long assignmentId,
        Long cycleId,
        String tipoReunion,
        String tipoReunionLabel,
        LocalDate fechaReunion,
        String descripcionAvance,
        String compromisos,
        String estado,
        Long evaluadorId,
        Long evaluadoId,
        boolean consentimientoEvaluado,
        LocalDateTime createdAt
) {}
