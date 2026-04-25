package pe.gob.gdr.access.application.port;

public record SignatureIntegrationStatus(
        boolean officialIntegrationAvailable,
        boolean providerStatusQueryAvailable,
        boolean automaticReturnEnabled,
        String integrationMode,
        String integrationMessage
) {
}
