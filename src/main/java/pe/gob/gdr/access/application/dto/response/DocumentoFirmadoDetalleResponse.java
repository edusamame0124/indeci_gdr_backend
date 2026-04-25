package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record DocumentoFirmadoDetalleResponse(
        Long idDocumentoFirmado,
        Long idResultado,
        Long idEvaluado,
        String evaluado,
        String evaluador,
        String ciclo,
        Long idTipoDocumento,
        String codigoTipoDocumento,
        String tipoDocumento,
        String nombreOriginal,
        String mimeType,
        Long tamanioBytes,
        Integer versionActual,
        String estado,
        String usuarioCarga,
        LocalDateTime fechaCarga
) {
}
