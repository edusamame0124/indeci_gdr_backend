package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record DocumentoFirmadoResumenResponse(
        Long idDocumentoFirmado,
        Long idResultado,
        Long idEvaluado,
        String evaluado,
        Long idTipoDocumento,
        String codigoTipoDocumento,
        String tipoDocumento,
        String descripcionContingencia,
        String nombreOriginal,
        String mimeType,
        Long tamanioBytes,
        Integer versionActual,
        String estado,
        String usuarioCarga,
        LocalDateTime fechaCarga
) {
}
