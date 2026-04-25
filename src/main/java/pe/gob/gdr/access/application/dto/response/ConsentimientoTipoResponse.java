package pe.gob.gdr.access.application.dto.response;

public record ConsentimientoTipoResponse(
        Long idTipoConsentimiento,
        String codigoConsentimiento,
        String nombreConsentimiento,
        String textoConsentimiento,
        Integer versionConsentimiento,
        boolean requerido
) {
}
