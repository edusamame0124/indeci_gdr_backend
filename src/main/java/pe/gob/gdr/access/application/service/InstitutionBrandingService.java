package pe.gob.gdr.access.application.service;

import java.util.Set;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.gob.gdr.access.application.dto.response.LoginBrandingResponse;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.InstitutionBranding;
import pe.gob.gdr.access.domain.repository.InstitutionBrandingRepository;

@Service
@Transactional(readOnly = true)
public class InstitutionBrandingService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/svg+xml"
    );

    private final InstitutionBrandingRepository institutionBrandingRepository;

    public InstitutionBrandingService(InstitutionBrandingRepository institutionBrandingRepository) {
        this.institutionBrandingRepository = institutionBrandingRepository;
    }

    public LoginBrandingResponse getLoginBranding() {
        return institutionBrandingRepository.findActiveBranding()
                .map(this::toLoginBrandingResponse)
                .orElseGet(() -> new LoginBrandingResponse(null, null, null, false, false));
    }

    public ResponseEntity<ByteArrayResource> getHeaderLogo() {
        InstitutionBranding branding = institutionBrandingRepository.findActiveBranding()
                .orElseThrow(() -> new ResourceNotFoundException("No existe branding institucional activo."));
        return buildImageResponse(branding.getHeaderLogo(), branding.getHeaderLogoMimeType(), "logo-barra");
    }

    public ResponseEntity<ByteArrayResource> getMainLogo() {
        InstitutionBranding branding = institutionBrandingRepository.findActiveBranding()
                .orElseThrow(() -> new ResourceNotFoundException("No existe branding institucional activo."));
        return buildImageResponse(branding.getMainLogo(), branding.getMainLogoMimeType(), "logo-principal");
    }

    private LoginBrandingResponse toLoginBrandingResponse(InstitutionBranding branding) {
        boolean hasHeaderLogo = hasValidLogo(branding.getHeaderLogo(), branding.getHeaderLogoMimeType());
        boolean hasMainLogo = hasValidLogo(branding.getMainLogo(), branding.getMainLogoMimeType());
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

        return new LoginBrandingResponse(
                branding.getInstitutionName(),
                hasHeaderLogo ? baseUrl + "/public/branding/login/logo/header" : null,
                hasMainLogo ? baseUrl + "/public/branding/login/logo/main" : null,
                hasHeaderLogo,
                hasMainLogo
        );
    }

    private ResponseEntity<ByteArrayResource> buildImageResponse(byte[] content, String mimeType, String filenamePrefix) {
        if (!hasValidLogo(content, mimeType)) {
            throw new ResourceNotFoundException("El logo solicitado no está disponible.");
        }
        ByteArrayResource resource = new ByteArrayResource(content);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .cacheControl(CacheControl.noCache())
                .contentLength(content.length)
                .header("Content-Disposition", "inline; filename=\"" + filenamePrefix + "\"")
                .body(resource);
    }

    private boolean hasValidLogo(byte[] logo, String mimeType) {
        return logo != null
                && logo.length > 0
                && mimeType != null
                && ALLOWED_MIME_TYPES.contains(mimeType.trim().toLowerCase());
    }
}
