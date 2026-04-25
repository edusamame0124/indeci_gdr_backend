package pe.gob.gdr.access.application.dto.response;

public record LoginBrandingResponse(
        String institutionName,
        String headerLogoUrl,
        String mainLogoUrl,
        boolean headerLogoAvailable,
        boolean mainLogoAvailable
) {
}
