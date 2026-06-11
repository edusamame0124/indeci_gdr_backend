package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.RegistrarCieConformacionRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.CieConformacionResponse;
import pe.gob.gdr.access.application.service.GdrCieConformacionService;

@RestController
@RequestMapping("/gdr/cie/conformacion")
public class GdrCieConformacionController {

    private final GdrCieConformacionService conformacionService;

    public GdrCieConformacionController(GdrCieConformacionService conformacionService) {
        this.conformacionService = conformacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GDR_ORH','GDR_CIE','GDR_TITULAR','GDR_AUDITOR','ADMIN_SISTEMA')")
    public ResponseEntity<ApiResponse<List<CieConformacionResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(
                conformacionService.listar(),
                "Conformaciones CIE consultadas."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GDR_ORH','GDR_CIE','GDR_TITULAR','GDR_AUDITOR','ADMIN_SISTEMA')")
    public ResponseEntity<ApiResponse<CieConformacionResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                conformacionService.obtener(id),
                "Conformación CIE consultada."));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GDR_ORH','GDR_TITULAR','ADMIN_SISTEMA')")
    public ResponseEntity<ApiResponse<CieConformacionResponse>> registrar(
            @Valid @RequestBody RegistrarCieConformacionRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "sistema-gdr";
        return ResponseEntity.ok(ApiResponse.ok(
                conformacionService.registrar(request, username),
                "Conformación CIE registrada correctamente."));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('GDR_TITULAR','ADMIN_SISTEMA')")
    public ResponseEntity<ApiResponse<CieConformacionResponse>> anular(
            @PathVariable Long id,
            Principal principal) {
        String username = principal != null ? principal.getName() : "sistema-gdr";
        return ResponseEntity.ok(ApiResponse.ok(
                conformacionService.anular(id, username),
                "Conformación CIE anulada."));
    }
}
