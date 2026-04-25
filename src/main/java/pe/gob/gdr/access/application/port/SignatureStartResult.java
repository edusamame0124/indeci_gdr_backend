package pe.gob.gdr.access.application.port;

public record SignatureStartResult(
        String externalTransactionId,
        String launchUrl,
        String message
) {
}
