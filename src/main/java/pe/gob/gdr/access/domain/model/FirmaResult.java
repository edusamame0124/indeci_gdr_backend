package pe.gob.gdr.access.domain.model;

/**
 * Resultado inmutable de una operación de firma sobre un documento GDR.
 * Para P0 (INTERNA_ELECTRONICA) los campos p1* son nulos.
 * Para P1 (DIGITAL_CERTIFICADA) todos los campos están poblados.
 */
public record FirmaResult(
        String tipoFirma,
        String estadoFirma,
        String hashDocumento,
        String ipFirmante,
        String sessionId
        // P1: String constanciaFirmaDigital
        // P1: String entidadCertificadora
        // P1: String dniTitularCertificado
) {
    public static FirmaResult internaElectronica(String hash, String ip, String session) {
        return new FirmaResult("INTERNA_ELECTRONICA", "FIRMADO_INTERNAMENTE", hash, ip, session);
    }

    // P1: public static FirmaResult digitalCertificada(...) { ... }
}
