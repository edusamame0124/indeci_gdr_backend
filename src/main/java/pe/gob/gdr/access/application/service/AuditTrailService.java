package pe.gob.gdr.access.application.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.domain.model.AuditEvent;
import pe.gob.gdr.access.domain.repository.AuditEventRepository;

@Service
public class AuditTrailService {

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final String SESSION_CONTEXT_VIEW = "SESSION_CONTEXT_VIEW";

    private final AuditEventRepository auditEventRepository;

    public AuditTrailService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void recordLoginSuccess(String principal, HttpServletRequest request) {
        persist(LOGIN_SUCCESS, principal, "Autenticacion exitosa.", request);
    }

    public void recordLoginFailed(String principal, String detail, HttpServletRequest request) {
        persist(LOGIN_FAILED, principal, detail, request);
    }

    public void recordLogout(String principal, HttpServletRequest request) {
        persist(LOGOUT, principal, "Sesion cerrada.", request);
    }

    public void recordRefreshToken(String principal, HttpServletRequest request) {
        persist(REFRESH_TOKEN, principal, "Token renovado.", request);
    }

    public void recordSessionContextView(String principal, String detail, HttpServletRequest request) {
        persist(SESSION_CONTEXT_VIEW, principal, detail, request);
    }

    public void recordEvent(String eventCode, String principal, String detail, HttpServletRequest request) {
        persist(eventCode, principal, detail, request);
    }

    private void persist(String eventCode, String principal, String detail, HttpServletRequest request) {
        auditEventRepository.save(AuditEvent.builder()
                .eventCode(eventCode)
                .principal(limit(principal, 120))
                .detail(limit(detail, 500))
                .clientIp(resolveClientIp(request))
                .userAgent(limit(resolveUserAgent(request), 500))
                .requestPath(limit(resolveRequestPath(request), 250))
                .build());
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

    private String resolveRequestPath(HttpServletRequest request) {
        return request == null ? null : request.getRequestURI();
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
