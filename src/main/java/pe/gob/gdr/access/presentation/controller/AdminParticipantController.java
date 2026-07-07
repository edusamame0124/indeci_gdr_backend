package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.gob.gdr.access.application.dto.request.CreateParticipantRoleRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.ParticipantResponse;
import pe.gob.gdr.access.application.service.AdminParticipantService;

@RestController
@RequestMapping("/admin/participants")
public class AdminParticipantController {

    private final AdminParticipantService adminParticipantService;

    public AdminParticipantController(AdminParticipantService adminParticipantService) {
        this.adminParticipantService = adminParticipantService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication) or @gdrAccessPolicyService.canManageGoals(authentication)")
    public ResponseEntity<ApiResponse<List<ParticipantResponse>>> listParticipants(
            @RequestParam(name = "cycleId") Long cycleId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminParticipantService.listParticipantsByCycle(cycleId),
                "Participantes del ciclo consultados correctamente."
        ));
    }

    @GetMapping("/summary")
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication)")
    public ResponseEntity<ApiResponse<List<pe.gob.gdr.access.application.dto.response.ParticipantSummaryResponse>>> summary(
            @RequestParam(name = "cycleId") Long cycleId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminParticipantService.summaryByCycle(cycleId),
                "Resumen por persona consultado correctamente."
        ));
    }

    @PostMapping("/role")
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication)")
    public ResponseEntity<ApiResponse<ParticipantResponse>> assignRole(
            @Valid @RequestBody CreateParticipantRoleRequest request
    ) {
        ParticipantResponse assigned = adminParticipantService.assignRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                assigned,
                "Rol asignado correctamente al participante."
        ));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/role")
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication)")
    public ResponseEntity<ApiResponse<ParticipantResponse>> updateRole(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @Valid @RequestBody pe.gob.gdr.access.application.dto.request.UpdateParticipantRoleRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminParticipantService.updateRole(id, request.role()),
                "Rol actualizado correctamente."
        ));
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{id}/status")
    @PreAuthorize("@gdrAccessPolicyService.canEditCronograma(authentication)")
    public ResponseEntity<ApiResponse<ParticipantResponse>> updateStatus(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @Valid @RequestBody pe.gob.gdr.access.application.dto.request.UpdateParticipantStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminParticipantService.updateStatus(id, request.status()),
                "Estado del participante actualizado correctamente."
        ));
    }
}
