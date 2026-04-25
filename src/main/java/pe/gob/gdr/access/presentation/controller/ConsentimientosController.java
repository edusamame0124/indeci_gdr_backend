package pe.gob.gdr.access.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.RegistrarAceptacionConsentimientoRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.ConsentimientoHistorialResponse;
import pe.gob.gdr.access.application.dto.response.ConsentimientoResumenResponse;
import pe.gob.gdr.access.application.dto.response.ConsentimientoTipoResponse;
import pe.gob.gdr.access.application.service.ConsentimientosService;

@RestController
@RequestMapping("/consentimientos")
public class ConsentimientosController {

    private final ConsentimientosService consentimientosService;

    public ConsentimientosController(ConsentimientosService consentimientosService) {
        this.consentimientosService = consentimientosService;
    }

    @GetMapping("/tipos")
    @PreAuthorize("@gdrAccessPolicyService.canViewConsents(authentication)")
    public ResponseEntity<ApiResponse<List<ConsentimientoTipoResponse>>> listTypes() {
        return ResponseEntity.ok(ApiResponse.ok(
                consentimientosService.listTypes(),
                "Tipos de consentimiento consultados correctamente."
        ));
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canViewConsents(authentication)")
    public ResponseEntity<ApiResponse<List<ConsentimientoResumenResponse>>> listOwnConsents(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                consentimientosService.listOwnConsents(principal.getName()),
                "Consentimientos del usuario consultados correctamente."
        ));
    }

    @GetMapping("/historial")
    @PreAuthorize("@gdrAccessPolicyService.canViewConsents(authentication)")
    public ResponseEntity<ApiResponse<List<ConsentimientoHistorialResponse>>> listOwnHistory(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                consentimientosService.listOwnHistory(principal.getName()),
                "Historial personal de consentimientos consultado correctamente."
        ));
    }

    @PostMapping("/aceptaciones")
    @PreAuthorize("@gdrAccessPolicyService.canViewConsents(authentication)")
    public ResponseEntity<ApiResponse<ConsentimientoHistorialResponse>> acceptConsent(
            @Valid @RequestBody RegistrarAceptacionConsentimientoRequest request,
            Principal principal,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                consentimientosService.acceptConsent(principal.getName(), request, httpServletRequest),
                "Consentimiento registrado correctamente."
        ));
    }
}
