package pe.gob.gdr.access.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.CreateUserRequest;
import pe.gob.gdr.access.application.dto.request.UpdateUserRequest;
import pe.gob.gdr.access.application.dto.request.UpdateUserRolesRequest;
import pe.gob.gdr.access.application.dto.request.UpdateUserStatusRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.RoleOptionResponse;
import pe.gob.gdr.access.application.dto.response.UserDetailResponse;
import pe.gob.gdr.access.application.dto.response.UserListItemResponse;
import pe.gob.gdr.access.application.service.AdminUserService;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/users")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<List<UserListItemResponse>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.listUsers(),
                "Usuarios consultados correctamente."
        ));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.getUser(id),
                "Usuario consultado correctamente."
        ));
    }

    @PostMapping("/users")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<UserDetailResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.createUser(request),
                "Usuario creado correctamente."
        ));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.updateUser(id, request),
                "Usuario actualizado correctamente."
        ));
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.updateUserStatus(id, request),
                "Estado de usuario actualizado correctamente."
        ));
    }

    @PutMapping("/users/{id}/roles")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.updateUserRoles(id, request),
                "Roles de usuario actualizados correctamente."
        ));
    }

    @GetMapping("/roles")
    @PreAuthorize("@gdrAccessPolicyService.canManageUsers(authentication)")
    public ResponseEntity<ApiResponse<List<RoleOptionResponse>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.listAssignableRoles(),
                "Roles consultados correctamente."
        ));
    }
}
