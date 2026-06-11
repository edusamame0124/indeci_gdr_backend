package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Caso CIE para bandeja y detalle (vista CIE/ORH). */
public record CasoCieResponse(
        Long id,
        String numeroCaso,
        Long solicitudId,
        Long finalEvaluationId,
        Long evaluadoId,
        String evaluadoNombre,
        String evaluadorNombre,
        String cicloNombre,
        LocalDateTime fechaIngresoCie,
        LocalDate plazoConvocatoria,
        Integer diasHabilesRestantesConvocatoria,
        boolean convocatoriaVencida,
        String estado,
        String estadoLabel,
        String sustentoEvaluado,
        BigDecimal puntajeActual,
        String calificacionActualCode,
        String calificacionActualLabel,
        String decision,
        String calificacionResultado,
        String calificacionResultadoLabel,
        String sustentoCie,
        LocalDateTime fechaDecision
) {
}
