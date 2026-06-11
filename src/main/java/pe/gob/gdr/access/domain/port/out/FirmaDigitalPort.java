package pe.gob.gdr.access.domain.port.out;

import pe.gob.gdr.access.domain.model.DocSignedFile;
import pe.gob.gdr.access.domain.model.FirmaResult;

/**
 * Port de firma digital (SOLID — Dependency Inversion).
 * Desacopla el dominio del mecanismo concreto de firma.
 *
 * P0 dev: implementado por FirmaInternaTrazableAdapter (conformidad interna).
 * P1 prod: implementar FirmaDigitalCertificadaAdapter (FirmaPeru / PKI / SUNAT).
 *          Requiere: certificado digital, hash PDF firmado, constancia, validador externo.
 *
 * Referencia: POSIBLE_CAMBIO_RRHH_GDR_003.
 */
public interface FirmaDigitalPort {

    /**
     * Registra la conformidad/firma sobre un documento GDR.
     *
     * @param documento  entidad DocSignedFile a firmar
     * @param firmante   login del usuario que firma
     * @param ipFirmante IP del cliente en el momento de la acción
     * @param sessionId  identificador de sesión HTTP para trazabilidad
     * @return resultado de la operación con tipo, estado y hash del documento
     */
    FirmaResult firmar(DocSignedFile documento, String firmante, String ipFirmante, String sessionId);

    /**
     * Indica si este adapter produce firmas digitales certificadas (P1+)
     * o solo conformidad interna electrónica (P0).
     */
    boolean esFirmaDigitalCertificada();
}
