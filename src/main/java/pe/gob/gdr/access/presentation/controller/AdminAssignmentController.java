package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.CreateAssignmentRequest;
import pe.gob.gdr.access.application.dto.request.UpdateAssignmentRequest;
import pe.gob.gdr.access.application.dto.request.UpdateAssignmentSegmentRequest;
import pe.gob.gdr.access.application.dto.request.UpdateAssignmentStatusRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.AssignmentDetailResponse;
import pe.gob.gdr.access.application.dto.response.AssignmentListItemResponse;
import pe.gob.gdr.access.application.dto.response.AssignmentPersonOptionResponse;
import pe.gob.gdr.access.application.dto.response.AssignmentSummaryByPersonResponse;
import pe.gob.gdr.access.application.service.AdminAssignmentService;

@RestController
@RequestMapping("/admin/assignments")
public class AdminAssignmentController {

    private final AdminAssignmentService adminAssignmentService;

    public AdminAssignmentController(AdminAssignmentService adminAssignmentService) {
        this.adminAssignmentService = adminAssignmentService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<List<AssignmentListItemResponse>>> listAssignments(
            @RequestParam(name = "cycleId") Long cycleId,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAssignmentService.listByCycle(cycleId, search, status),
                "Relaciones de participacion GDR consultadas correctamente."
        ));
    }

    @GetMapping("/summary")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<List<AssignmentSummaryByPersonResponse>>> summary(
            @RequestParam(name = "cycleId") Long cycleId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAssignmentService.summaryByCycle(cycleId),
                "Resumen por persona consultado correctamente."
        ));
    }

    @GetMapping("/persons/searchable")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<List<AssignmentPersonOptionResponse>>> searchablePersons(
            @RequestParam(name = "q", required = false) String search
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAssignmentService.searchablePersons(search),
                "Personas elegibles consultadas correctamente."
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<AssignmentDetailResponse>> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAssignmentService.getById(id),
                "Relacion consultada correctamente."
        ));
    }

    @PostMapping
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<AssignmentDetailResponse>> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request
    ) {
        AssignmentDetailResponse created = adminAssignmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                created,
                "Relacion creada correctamente."
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<AssignmentDetailResponse>> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAssignmentService.update(id, request),
                "Relacion actualizada correctamente."
        ));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<AssignmentDetailResponse>> updateAssignmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAssignmentService.updateStatus(id, request),
                "Estado de la relacion actualizado correctamente."
        ));
    }

    @PatchMapping("/{id}/segment")
    @PreAuthorize("@gdrAccessPolicyService.canSetAssignmentSegment(authentication, #id)")
    public ResponseEntity<ApiResponse<AssignmentDetailResponse>> updateAssignmentSegment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentSegmentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAssignmentService.updateSegment(id, request.segmentId()),
                "Segmento de la relacion actualizado correctamente."
        ));
    }
}
