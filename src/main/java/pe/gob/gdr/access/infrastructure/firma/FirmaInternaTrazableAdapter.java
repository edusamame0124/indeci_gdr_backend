package pe.gob.gdr.access.infrastructure.firma;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;
import pe.gob.gdr.access.domain.model.DocSignedFile;
import pe.gob.gdr.access.domain.model.FirmaResult;
import pe.gob.gdr.access.domain.port.out.FirmaDigitalPort;

/**
 * Adapter P0 — Conformidad interna electrónica trazable.
 *
 * Registra quién firmó, desde dónde y cuándo, con hash SHA-256 de la clave
 * del archivo (fileKey + firmante + timestamp) como sello de integridad.
 *
 * IMPORTANTE: este adapter NO produce firma digital certificada.
 * El documento se muestra al usuario con el aviso:
 *   "Conformidad interna registrada — Pendiente de firma digital real (P1)."
 *
 * Reemplazar en P1 por FirmaDigitalCertificadaAdapter que integre:
 *   FirmaPeru / PKI institucional / SUNAT / CA externa.
 *   Ver: POSIBLE_CAMBIO_RRHH_GDR_003.
 */
@Component
public class FirmaInternaTrazableAdapter implements FirmaDigitalPort {

    @Override
    public FirmaResult firmar(DocSignedFile documento, String firmante, String ipFirmante, String sessionId) {
        String hash = calcularHash(documento.getFileKey(), firmante);
        return FirmaResult.internaElectronica(hash, ipFirmante, sessionId);
    }

    @Override
    public boolean esFirmaDigitalCertificada() {
        return false;
    }

    private String calcularHash(String fileKey, String firmante) {
        try {
            String input = fileKey + "|" + firmante + "|" + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : encoded) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return "hash-unavailable";
        }
    }

    // P1: inyectar FirmaPeru client + config PKI
    // @Value("${gdr.firma.pkcs12-path}") private String pkcs12Path;
    // @Value("${gdr.firma.firma-peru-url}") private String firmaperuUrl;
}
