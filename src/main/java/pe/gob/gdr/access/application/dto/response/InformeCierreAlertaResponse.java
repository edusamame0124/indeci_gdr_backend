package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;

/** VAL-06 — Alerta de plazo para informe de cierre (31 mayo año siguiente). */
public record InformeCierreAlertaResponse(
        LocalDate fechaLimite,
        Integer diasRestantes,
        boolean vencida,
        String nivelSemaforo,
        String mensaje,
        String referenciaNormativa
) {
}
