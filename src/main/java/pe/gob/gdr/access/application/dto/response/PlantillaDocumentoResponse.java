package pe.gob.gdr.access.application.dto.response;

public record PlantillaDocumentoResponse(
        Long idPlantilla,
        Long idTipoDocumento,
        String codigoTipoDocumento,
        String nombreTipoDocumento,
        String nombrePlantilla,
        String descripcion,
        String nombreOriginal,
        String mimeType,
        Long tamanioBytes
) {
}
