package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record ConsentimientoHistorialResponse(
        Long idRegistroConsentimiento,
        Long idTipoConsentimiento,
        String codigoConsentimiento,
        String nombreConsentimiento,
        Integer versionConsentimiento,
        LocalDateTime fechaAceptacion,
        String detalleAceptacion,
        boolean aceptado
) {
}
