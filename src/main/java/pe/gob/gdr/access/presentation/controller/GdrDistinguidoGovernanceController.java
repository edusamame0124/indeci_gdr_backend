package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.ActualizarRequisitosDistinguidoRequest;
import pe.gob.gdr.access.application.dto.request.AsignarDistinguidoRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.AsignarDistinguidoResultResponse;
import pe.gob.gdr.access.application.dto.response.DistinguidoCandidatosResponse;
import pe.gob.gdr.access.application.service.GdrDistinguidoGovernanceService;

@RestController
@RequestMapping("/distinguidos")
public class GdrDistinguidoGovernanceController {

    private final GdrDistinguidoGovernanceService distinguidoGovernanceService;

    public GdrDistinguidoGovernanceController(GdrDistinguidoGovernanceService distinguidoGovernanceService) {
        this.distinguidoGovernanceService = distinguidoGovernanceService;
    }

    @GetMapping("/candidatos")
    @PreAuthorize("@gdrAccessPolicyService.canViewDistinguidoCandidates(authentication)")
    public ResponseEntity<ApiResponse<DistinguidoCandidatosResponse>> listCandidates() {
        return ResponseEntity.ok(ApiResponse.ok(
                distinguidoGovernanceService.listCandidatos(),
                "Cupos de rendimiento distinguido consultados correctamente."
        ));
    }

    @PatchMapping("/candidatos/{assignmentId}/requisitos")
    @PreAuthorize("@gdrAccessPolicyService.canManageDistinguidoRequisites(authentication, #assignmentId)")
    public ResponseEntity<ApiResponse<Void>> updateRequisites(
            @PathVariable Long assignmentId,
            @Valid @RequestBody ActualizarRequisitosDistinguidoRequest request
    ) {
        distinguidoGovernanceService.actualizarRequisitos(assignmentId, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Requisitos de gobernanza actualizados correctamente."));
    }

    @PostMapping("/asignar")
    @PreAuthorize("@gdrAccessPolicyService.canAssignDistinguido(authentication)")
    public ResponseEntity<ApiResponse<AsignarDistinguidoResultResponse>> assign(
            @Valid @RequestBody AsignarDistinguidoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                distinguidoGovernanceService.asignar(request),
                "Asignaciones de Rendimiento distinguido aplicadas correctamente."
        ));
    }
}
