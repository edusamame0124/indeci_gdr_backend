package pe.gob.gdr.access.presentation.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.LoginBrandingResponse;
import pe.gob.gdr.access.application.service.InstitutionBrandingService;

@RestController
@RequestMapping("/public/branding")
public class PublicBrandingController {

    private final InstitutionBrandingService institutionBrandingService;

    public PublicBrandingController(InstitutionBrandingService institutionBrandingService) {
        this.institutionBrandingService = institutionBrandingService;
    }

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<LoginBrandingResponse>> getLoginBranding() {
        return ResponseEntity.ok(ApiResponse.ok(
                institutionBrandingService.getLoginBranding(),
                "Branding consultado correctamente."
        ));
    }

    @GetMapping("/login/logo/header")
    public ResponseEntity<ByteArrayResource> getHeaderLogo() {
        return institutionBrandingService.getHeaderLogo();
    }

    @GetMapping("/login/logo/main")
    public ResponseEntity<ByteArrayResource> getMainLogo() {
        return institutionBrandingService.getMainLogo();
    }
}
