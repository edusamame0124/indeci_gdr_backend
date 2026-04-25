package pe.gob.gdr.access.application.port;

import pe.gob.gdr.access.domain.model.DocSignatureRequest;

public interface DigitalSignaturePort {

    String getProviderCode();

    SignatureStartResult startSignature(DocSignatureRequest request);

    String resolveLaunchUrl(String externalTransactionId);

    SignatureIntegrationStatus getIntegrationStatus();
}
