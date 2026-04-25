package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDateTime;

public record SolicitudFirmaDetalleResponse(
        Long idSolicitudFirma,
        Long idResultado,
        Long idEvaluado,
        String evaluado,
        String evaluador,
        String ciclo,
        Long idTipoDocumento,
        String codigoTipoDocumento,
        String tipoDocumento,
        Long idPlantilla,
        String nombrePlantilla,
        String nombreArchivoPreparado,
        String estadoFlujo,
        String proveedorFirma,
        String idTransaccionExterna,
        String urlFirma,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaInicioFirma,
        LocalDateTime fechaRetorno,
        LocalDateTime fechaRegistroDocumento,
        String codigoResultadoFirma,
        String mensajeResultadoFirma,
        Long idDocumentoFirmado,
        boolean puedeIniciarFirma,
        boolean puedeRegistrarRetorno,
        boolean documentoPreparadoDisponible,
        boolean documentoFirmadoDisponible,
        boolean integracionOficialDisponible,
        boolean consultaProveedorDisponible,
        boolean retornoAutomaticoHabilitado,
        String modoIntegracion,
        String mensajeIntegracion
) {
}
