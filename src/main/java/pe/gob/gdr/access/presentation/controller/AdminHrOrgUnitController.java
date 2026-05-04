package pe.gob.gdr.access.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.HrOrgUnitOptionResponse;
import pe.gob.gdr.access.application.service.AdminUserService;

@RestController
@RequestMapping("/admin/hr-org-units")
public class AdminHrOrgUnitController {

    private final AdminUserService adminUserService;

    public AdminHrOrgUnitController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<List<HrOrgUnitOptionResponse>>> listOrgUnits() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.listActiveOrgUnits(),
                "Unidades organicas consultadas correctamente."
        ));
    }
}
