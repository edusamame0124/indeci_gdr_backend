package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record ConsentimientoResumenResponse(
        Long idTipoConsentimiento,
        String codigoConsentimiento,
        String nombreConsentimiento,
        String textoConsentimiento,
        Integer versionConsentimiento,
        boolean requerido,
        boolean aceptado,
        LocalDateTime fechaAceptacion
) {
}
