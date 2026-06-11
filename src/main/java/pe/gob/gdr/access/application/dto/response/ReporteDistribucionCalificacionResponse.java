package pe.gob.gdr.access.application.dto.response;

import java.math.BigDecimal;

/** Distribución de calificaciones cualitativas del ciclo activo. */
public record ReporteDistribucionCalificacionResponse(
        String codigoCalificacion,
        String etiquetaCalificacion,
        int cantidad,
        BigDecimal porcentaje
) {
}
