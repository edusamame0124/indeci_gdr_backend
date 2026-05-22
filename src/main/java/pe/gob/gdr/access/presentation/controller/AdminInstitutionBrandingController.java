package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.gdr.access.application.dto.request.UpdateInstitutionBrandingRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.LoginBrandingResponse;
import pe.gob.gdr.access.application.service.InstitutionBrandingService;

@RestController
@RequestMapping("/admin/branding")
public class AdminInstitutionBrandingController {

    private final InstitutionBrandingService institutionBrandingService;

    public AdminInstitutionBrandingController(InstitutionBrandingService institutionBrandingService) {
        this.institutionBrandingService = institutionBrandingService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<LoginBrandingResponse>> getBranding() {
        return ResponseEntity.ok(ApiResponse.ok(
                institutionBrandingService.getLoginBranding(),
                "Personalizacion institucional consultada correctamente."
        ));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<LoginBrandingResponse>> updateBranding(
            @Valid @ModelAttribute UpdateInstitutionBrandingRequest request,
            @RequestParam(name = "logo", required = false) MultipartFile logo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                institutionBrandingService.updateBranding(request, logo),
                "Personalizacion institucional actualizada correctamente."
        ));
    }
}
