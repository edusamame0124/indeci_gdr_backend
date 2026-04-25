package pe.gob.gdr.access.infrastructure.signature;

import java.util.UUID;
import org.springframework.stereotype.Component;
import pe.gob.gdr.access.application.port.DigitalSignaturePort;
import pe.gob.gdr.access.application.port.SignatureIntegrationStatus;
import pe.gob.gdr.access.application.port.SignatureStartResult;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.DocSignatureRequest;
import pe.gob.gdr.access.infrastructure.config.FirmaPeruProperties;

@Component
public class FirmaPeruAdapter implements DigitalSignaturePort {

    private final FirmaPeruProperties properties;

    public FirmaPeruAdapter(FirmaPeruProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getProviderCode() {
        return properties.getProveedorPrincipal();
    }

    @Override
    public SignatureStartResult startSignature(DocSignatureRequest request) {
        String launchUrl = resolveLaunchUrl(null);
        return new SignatureStartResult(
                UUID.randomUUID().toString(),
                launchUrl,
                properties.getMensajeIntegracion()
        );
    }

    @Override
    public String resolveLaunchUrl(String externalTransactionId) {
        if (properties.getUrlInicioBase() == null || properties.getUrlInicioBase().isBlank()) {
            throw new DomainException("La URL base de Firma Peru no esta configurada.");
        }
        return properties.getUrlInicioBase();
    }

    @Override
    public SignatureIntegrationStatus getIntegrationStatus() {
        return new SignatureIntegrationStatus(
                properties.isIntegracionOficialHabilitada(),
                properties.isConsultaEstadoHabilitada(),
                properties.isRetornoAutomaticoHabilitado(),
                properties.isIntegracionOficialHabilitada() ? "INSTITUCIONAL" : "PREPARADO_SIN_CONVENIO",
                properties.getMensajeIntegracion()
        );
    }
}
