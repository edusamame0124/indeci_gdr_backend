package pe.gob.gdr.access.application.dto.response;

import java.util.List;

/**
 * VAL-13A — Alerta de evaluaciones finales sin retroalimentación registrada.
 * Una evaluación "sin notificar" es aquella donde el evaluador aún no registró
 * la fecha de reunión de retroalimentación final (FECHA_REUNION_RETRO_FINAL IS NULL).
 * Referencia: RPE 068-2020-SERVIR-PE Art. 33-39.
 */
public record AlertaEvaluacionesSinNotificarResponse(
        int totalSinNotificar,
        List<String> evaluadosSinNotificar,
        boolean bloquearCierre,
        String mensaje,
        String referenciaNormativa
) {
}
