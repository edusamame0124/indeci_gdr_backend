package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record NotificacionDetalleResponse(
        Long idNotificacion,
        String codigoEvento,
        String tituloNotificacion,
        String mensajeNotificacion,
        String estadoNotificacion,
        LocalDateTime fechaEnvio,
        LocalDateTime fechaLectura,
        String referenciaNegocio,
        String codigoPlantilla,
        String nombrePlantilla,
        String nombreCanal
) {
}
