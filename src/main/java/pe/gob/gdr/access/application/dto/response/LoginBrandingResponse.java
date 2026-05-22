package pe.gob.gdr.access.application.dto.response;

public record LoginBrandingResponse(
        String institutionName,
        String shortName,
        String ruc,
        String address,
        String headerLogoUrl,
        String mainLogoUrl,
        boolean headerLogoAvailable,
        boolean mainLogoAvailable
) {
}
