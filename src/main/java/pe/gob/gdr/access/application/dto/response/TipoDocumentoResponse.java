package pe.gob.gdr.access.application.dto.response;

public record TipoDocumentoResponse(
        Long idTipoDocumento,
        String codigoTipoDocumento,
        String nombreTipoDocumento,
        String descripcion
) {
}
