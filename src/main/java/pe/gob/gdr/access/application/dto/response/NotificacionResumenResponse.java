package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record NotificacionResumenResponse(
        Long idNotificacion,
        String codigoEvento,
        String tituloNotificacion,
        String mensajeNotificacion,
        String estadoNotificacion,
        LocalDateTime fechaEnvio,
        LocalDateTime fechaLectura,
        String referenciaNegocio
) {
}
