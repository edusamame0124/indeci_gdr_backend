package pe.gob.gdr.access.application.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.application.dto.request.LoginRequest;
import pe.gob.gdr.access.application.dto.request.RefreshTokenRequest;
import pe.gob.gdr.access.application.dto.response.ActiveCycleContextResponse;
import pe.gob.gdr.access.application.dto.response.FeatureAccessResponse;
import pe.gob.gdr.access.application.dto.response.TokenResponse;
import pe.gob.gdr.access.application.dto.response.UserSessionResponse;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.RefreshToken;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.RefreshTokenRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;
import pe.gob.gdr.access.infrastructure.config.JwtProperties;
import pe.gob.gdr.access.infrastructure.security.JwtTokenProvider;

@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserContextService userContextService;
    private final AuditTrailService auditTrailService;
    private final GdrAccessPolicyService gdrAccessPolicyService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            UserContextService userContextService,
            AuditTrailService auditTrailService,
            GdrAccessPolicyService gdrAccessPolicyService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userContextService = userContextService;
        this.auditTrailService = auditTrailService;
        this.gdrAccessPolicyService = gdrAccessPolicyService;
    }

    @Transactional(dontRollbackOn = {BadCredentialsException.class, LockedException.class, DisabledException.class})
    public TokenResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String normalizedLoginId = normalizeLoginId(request.loginId());
        Optional<User> candidateUser = userRepository.findByLoginId(normalizedLoginId);

        Authentication authentication;
        try {
            candidateUser.ifPresent(this::ensureAccountCanAttemptLogin);
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedLoginId, request.password())
            );
        } catch (LockedException exception) {
            auditTrailService.recordLoginFailed(normalizedLoginId, exception.getMessage(), httpRequest);
            throw new LockedException("La cuenta se encuentra bloqueada temporalmente.");
        } catch (DisabledException exception) {
            auditTrailService.recordLoginFailed(normalizedLoginId, exception.getMessage(), httpRequest);
            throw new DisabledException("La cuenta se encuentra inactiva.");
        } catch (BadCredentialsException exception) {
            registerFailedAttempt(candidateUser.orElse(null));
            String message = buildInvalidCredentialsMessage(candidateUser.orElse(null));
            auditTrailService.recordLoginFailed(normalizedLoginId, message, httpRequest);
            throw new BadCredentialsException(message);
        }

        User authenticatedUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario autenticado."));

        authenticatedUser.setFailedAttempts(0);
        authenticatedUser.setLockedUntil(null);
        authenticatedUser.setLastLoginAt(LocalDateTime.now());
        userRepository.save(authenticatedUser);

        List<String> roles = authoritiesFrom(authentication);
        String accessToken = jwtTokenProvider.generateAccessToken(authenticatedUser.getUsername(), roles);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(authenticatedUser.getUsername());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(authenticatedUser)
                .tokenValue(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpirationMs() / 1000))
                .clientIp(resolveClientIp(httpRequest))
                .userAgent(resolveUserAgent(httpRequest))
                .build();
        refreshTokenRepository.save(refreshToken);
        auditTrailService.recordLoginSuccess(authenticatedUser.getUsername(), httpRequest);

        return new TokenResponse(
                accessToken,
                refreshTokenValue,
                "Bearer",
                jwtProperties.getAccessTokenExpirationMs() / 1000,
                authenticatedUser.getUsername(),
                authenticatedUser.getDisplayName(),
                roles
        );
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String refreshTokenValue = request.refreshToken().trim();
        if (!jwtTokenProvider.validateToken(refreshTokenValue) || !jwtTokenProvider.isRefreshToken(refreshTokenValue)) {
            throw new BadCredentialsException("El refresh token es inválido o expiró.");
        }

        RefreshToken persistedToken = refreshTokenRepository.findActiveByTokenValue(refreshTokenValue)
                .orElseThrow(() -> new BadCredentialsException("El refresh token fue revocado o no existe."));

        if (persistedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            persistedToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(persistedToken);
            throw new BadCredentialsException("El refresh token expiró.");
        }

        persistedToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(persistedToken);

        User user = userRepository.findByUsername(jwtTokenProvider.extractSubject(refreshTokenValue))
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario del refresh token."));

        List<String> roles = buildRoles(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        RefreshToken rotatedToken = RefreshToken.builder()
                .user(user)
                .tokenValue(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpirationMs() / 1000))
                .clientIp(resolveClientIp(httpRequest))
                .userAgent(resolveUserAgent(httpRequest))
                .build();
        refreshTokenRepository.save(rotatedToken);
        auditTrailService.recordRefreshToken(user.getUsername(), httpRequest);

        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtProperties.getAccessTokenExpirationMs() / 1000,
                user.getUsername(),
                user.getDisplayName(),
                roles
        );
    }

    @Transactional
    public void logout(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String refreshTokenValue = request.refreshToken().trim();
        refreshTokenRepository.findActiveByTokenValue(refreshTokenValue)
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
        auditTrailService.recordLogout(resolvePrincipalFromRefreshToken(refreshTokenValue), httpRequest);
    }

    @Transactional
    public UserSessionResponse me(String username, HttpServletRequest httpRequest) {
        User user = userRepository.findByUsernameWithPerson(username)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la sesión del usuario."));
        ActiveCycleContextResponse context = userContextService.resolveCurrentContext(user.getUsername());
        FeatureAccessResponse featureAccess = gdrAccessPolicyService.buildFeatureAccess(user, context);
        auditTrailService.recordSessionContextView(
                user.getUsername(),
                buildSessionContextDetail(context),
                httpRequest
        );
        return new UserSessionResponse(
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                buildRoles(user),
                context,
                featureAccess
        );
    }

    private void ensureAccountCanAttemptLogin(User user) {
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException("La cuenta está bloqueada hasta " + user.getLockedUntil() + ".");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new DisabledException("La cuenta se encuentra inactiva.");
        }
    }

    private void registerFailedAttempt(User user) {
        if (user == null) {
            return;
        }
        int attempts = Optional.ofNullable(user.getFailedAttempts()).orElse(0) + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        userRepository.save(user);
    }

    private String buildInvalidCredentialsMessage(User user) {
        if (user == null) {
            return "Credenciales inválidas.";
        }
        if (user.getFailedAttempts() != null && user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            return "La cuenta fue bloqueada temporalmente por múltiples intentos fallidos.";
        }
        int remainingAttempts = Math.max(0, MAX_FAILED_ATTEMPTS - Optional.ofNullable(user.getFailedAttempts()).orElse(0));
        return "Credenciales inválidas. Intentos restantes antes del bloqueo: " + remainingAttempts + ".";
    }

    private List<String> authoritiesFrom(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    private List<String> buildRoles(User user) {
        return user.getUserRoles().stream()
                .filter(userRole -> "ACTIVE".equalsIgnoreCase(userRole.getStatus()))
                .filter(userRole -> userRole.getRole() != null)
                .filter(userRole -> "ACTIVE".equalsIgnoreCase(userRole.getRole().getStatus()))
                .map(userRole -> "ROLE_" + userRole.getRole().getCode())
                .sorted()
                .collect(Collectors.toList());
    }

    private String normalizeLoginId(String loginId) {
        return loginId == null ? "" : loginId.trim().toLowerCase();
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveUserAgent(HttpServletRequest request) {
        return request == null ? null : request.getHeader("User-Agent");
    }

    private String resolvePrincipalFromRefreshToken(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            return null;
        }
        if (!jwtTokenProvider.validateToken(refreshTokenValue) || !jwtTokenProvider.isRefreshToken(refreshTokenValue)) {
            return null;
        }
        return jwtTokenProvider.extractSubject(refreshTokenValue);
    }

    private String buildSessionContextDetail(ActiveCycleContextResponse context) {
        if (context == null || !context.cycleActive()) {
            return "Sesión consultada sin ciclo activo.";
        }
        if (!context.hrPersonLinked()) {
            return "Sesión consultada sin vínculo laboral a persona HR.";
        }
        if (!context.assigned()) {
            return "Sesión consultada con ciclo activo y sin asignación de contexto.";
        }
        return "Sesión consultada en ciclo "
                + context.cycleCode()
                + " con contexto "
                + context.contextName()
                + ".";
    }
}
