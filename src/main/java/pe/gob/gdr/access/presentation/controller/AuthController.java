package pe.gob.gdr.access.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.gdr.access.application.dto.request.LoginRequest;
import pe.gob.gdr.access.application.dto.request.RefreshTokenRequest;
import pe.gob.gdr.access.application.dto.response.ApiResponse;
import pe.gob.gdr.access.application.dto.response.TokenResponse;
import pe.gob.gdr.access.application.dto.response.UserSessionResponse;
import pe.gob.gdr.access.application.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                authService.login(request, httpServletRequest),
                "Autenticación exitosa."
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                authService.refresh(request, httpServletRequest),
                "Token renovado."
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest
    ) {
        authService.logout(request, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.ok(null, "Sesión cerrada."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSessionResponse>> me(
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                authService.me(authentication.getName(), httpServletRequest),
                "Sesión consultada correctamente."
        ));
    }
}
