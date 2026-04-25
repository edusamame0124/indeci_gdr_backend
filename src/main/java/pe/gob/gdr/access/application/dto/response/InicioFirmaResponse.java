package pe.gob.gdr.access.application.dto.response;

public record InicioFirmaResponse(
        Long idSolicitudFirma,
        String estadoFlujo,
        String proveedorFirma,
        String idTransaccionExterna,
        String urlFirma,
        String mensaje,
        boolean integracionOficialDisponible,
        boolean consultaProveedorDisponible,
        boolean retornoAutomaticoHabilitado,
        String modoIntegracion,
        String mensajeIntegracion
) {
}
