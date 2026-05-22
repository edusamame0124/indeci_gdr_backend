package pe.gob.gdr.access.application.service;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.gob.gdr.access.application.dto.request.UpdateInstitutionBrandingRequest;
import pe.gob.gdr.access.application.dto.response.LoginBrandingResponse;
import pe.gob.gdr.access.application.port.DocumentStoragePort;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.InstitutionBranding;
import pe.gob.gdr.access.domain.repository.InstitutionBrandingRepository;

@Service
@Transactional(readOnly = true)
public class InstitutionBrandingService {

    private static final long MAX_LOGO_SIZE_BYTES = 2L * 1024L * 1024L;
    private static final String BRANDING_LOGO_CATEGORY = "branding";
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/svg+xml"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".webp", ".svg");

    private final InstitutionBrandingRepository institutionBrandingRepository;
    private final DocumentStoragePort documentStoragePort;

    public InstitutionBrandingService(
            InstitutionBrandingRepository institutionBrandingRepository,
            DocumentStoragePort documentStoragePort
    ) {
        this.institutionBrandingRepository = institutionBrandingRepository;
        this.documentStoragePort = documentStoragePort;
    }

    public LoginBrandingResponse getLoginBranding() {
        return institutionBrandingRepository.findActiveBranding()
                .map(this::toLoginBrandingResponse)
                .orElseGet(() -> new LoginBrandingResponse(null, null, null, null, null, null, false, false));
    }

    public ResponseEntity<Resource> getHeaderLogo() {
        return getLogoFromStorage(false);
    }

    public ResponseEntity<Resource> getMainLogo() {
        return getLogoFromStorage(false);
    }

    public ResponseEntity<Resource> getLogoFromStorage(boolean download) {
        InstitutionBranding branding = institutionBrandingRepository.findActiveBranding()
                .orElseThrow(() -> new ResourceNotFoundException("No existe branding institucional activo."));
        if (!hasValidLogoRoute(branding)) {
            throw new ResourceNotFoundException("El logo institucional no esta disponible.");
        }
        Resource resource = documentStoragePort.loadAsResource(branding.getLogoRuta());
        ContentDisposition contentDisposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(branding.getLogoNombreOriginal() == null ? "logo-institucional" : branding.getLogoNombreOriginal())
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(branding.getLogoMimeType()))
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    @Transactional
    public LoginBrandingResponse updateBranding(UpdateInstitutionBrandingRequest request, MultipartFile logo) {
        InstitutionBranding branding = institutionBrandingRepository.findActiveBranding()
                .orElseGet(() -> InstitutionBranding.builder().status("ACTIVE").build());

        branding.setInstitutionName(normalizeRequired(request.institutionName(), "El nombre de la institucion es obligatorio."));
        branding.setNombreCorto(normalizeOptional(request.nombreCorto()));
        branding.setRuc(normalizeOptional(request.ruc()));
        branding.setDireccion(normalizeOptional(request.direccion()));

        if (logo != null && !logo.isEmpty()) {
            applyLogo(branding, logo);
        }

        return toLoginBrandingResponse(institutionBrandingRepository.save(branding));
    }

    private LoginBrandingResponse toLoginBrandingResponse(InstitutionBranding branding) {
        boolean hasLogoRoute = hasValidLogoRoute(branding);
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        String logoUrl = hasLogoRoute ? baseUrl + "/public/branding/login/logo" : null;

        return new LoginBrandingResponse(
                branding.getInstitutionName(),
                branding.getNombreCorto(),
                branding.getRuc(),
                branding.getDireccion(),
                logoUrl,
                logoUrl,
                hasLogoRoute,
                hasLogoRoute
        );
    }

    private boolean hasValidLogoRoute(InstitutionBranding branding) {
        return branding.getLogoRuta() != null
                && !branding.getLogoRuta().isBlank()
                && branding.getLogoMimeType() != null
                && ALLOWED_MIME_TYPES.contains(branding.getLogoMimeType().trim().toLowerCase(Locale.ROOT));
    }

    private void applyLogo(InstitutionBranding branding, MultipartFile logo) {
        validateLogo(logo);
        String originalName = sanitizeOriginalName(logo.getOriginalFilename());
        String extension = extensionFrom(originalName);
        try {
            String fileKey = documentStoragePort.store(BRANDING_LOGO_CATEGORY, extension, logo.getBytes());
            String mimeType = logo.getContentType() == null ? "" : logo.getContentType().trim().toLowerCase(Locale.ROOT);
            branding.setLogoRuta(fileKey);
            branding.setLogoMimeType(mimeType);
            branding.setLogoNombreOriginal(originalName);
        } catch (IOException exception) {
            throw new DomainException("No se pudo leer el logo institucional adjunto.");
        }
    }

    private void validateLogo(MultipartFile logo) {
        if (logo.getSize() <= 0 || logo.getSize() > MAX_LOGO_SIZE_BYTES) {
            throw new DomainException("El logo institucional no debe superar 2 MB.");
        }
        String mimeType = logo.getContentType() == null ? "" : logo.getContentType().trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new DomainException("El logo institucional debe ser PNG, JPG, WEBP o SVG.");
        }
        String extension = extensionFrom(sanitizeOriginalName(logo.getOriginalFilename()));
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new DomainException("La extension del logo institucional no es valida.");
        }
    }

    private String sanitizeOriginalName(String originalName) {
        String candidate = originalName == null ? "logo-institucional.png" : originalName;
        candidate = candidate.replace('\\', '/');
        if (candidate.contains("/")) {
            candidate = candidate.substring(candidate.lastIndexOf('/') + 1);
        }
        candidate = candidate.replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        return candidate.isBlank() ? "logo-institucional.png" : candidate;
    }

    private String extensionFrom(String originalName) {
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == originalName.length() - 1) {
            throw new DomainException("El logo institucional debe tener una extension valida.");
        }
        return originalName.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new DomainException(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
