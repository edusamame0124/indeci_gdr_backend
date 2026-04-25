package pe.gob.gdr.access.application.service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.RegistrarAceptacionConsentimientoRequest;
import pe.gob.gdr.access.application.dto.response.ConsentimientoHistorialResponse;
import pe.gob.gdr.access.application.dto.response.ConsentimientoResumenResponse;
import pe.gob.gdr.access.application.dto.response.ConsentimientoTipoResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ConsentRegistro;
import pe.gob.gdr.access.domain.model.ConsentTipo;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.ConsentRegistroRepository;
import pe.gob.gdr.access.domain.repository.ConsentTipoRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

@Service
public class ConsentimientosService {

    private final ConsentTipoRepository consentTipoRepository;
    private final ConsentRegistroRepository consentRegistroRepository;
    private final UserRepository userRepository;
    private final AuditTrailService auditTrailService;

    public ConsentimientosService(
            ConsentTipoRepository consentTipoRepository,
            ConsentRegistroRepository consentRegistroRepository,
            UserRepository userRepository,
            AuditTrailService auditTrailService
    ) {
        this.consentTipoRepository = consentTipoRepository;
        this.consentRegistroRepository = consentRegistroRepository;
        this.userRepository = userRepository;
        this.auditTrailService = auditTrailService;
    }

    public List<ConsentimientoTipoResponse> listTypes() {
        return consentTipoRepository.findActiveTypes().stream()
                .map(this::mapType)
                .toList();
    }

    public List<ConsentimientoResumenResponse> listOwnConsents(String username) {
        Map<Long, ConsentRegistro> acceptedByType = consentRegistroRepository.findActiveByUsername(username).stream()
                .collect(Collectors.toMap(
                        registro -> registro.getTipoConsentimiento().getId(),
                        Function.identity(),
                        (left, right) -> left.getFechaAceptacion().isAfter(right.getFechaAceptacion()) ? left : right
                ));

        return consentTipoRepository.findActiveTypes().stream()
                .map(type -> {
                    ConsentRegistro acceptance = acceptedByType.get(type.getId());
                    return new ConsentimientoResumenResponse(
                            type.getId(),
                            type.getCodigoConsentimiento(),
                            type.getNombreConsentimiento(),
                            type.getTextoConsentimiento(),
                            type.getVersionConsentimiento(),
                            "S".equalsIgnoreCase(type.getRequerido()),
                            acceptance != null,
                            acceptance != null ? acceptance.getFechaAceptacion() : null
                    );
                })
                .toList();
    }

    public List<ConsentimientoHistorialResponse> listOwnHistory(String username) {
        return consentRegistroRepository.findActiveByUsername(username).stream()
                .map(this::mapHistory)
                .toList();
    }

    @Transactional
    public ConsentimientoHistorialResponse acceptConsent(
            String username,
            RegistrarAceptacionConsentimientoRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario autenticado."));
        ConsentTipo type = consentTipoRepository.findActiveById(request.idTipoConsentimiento())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el consentimiento solicitado."));

        consentRegistroRepository.findActiveByUsernameAndTypeVersion(
                username,
                type.getId(),
                type.getVersionConsentimiento()
        ).ifPresent(existing -> {
            throw new DomainException("El consentimiento ya fue aceptado para la version vigente.");
        });

        ConsentRegistro saved = consentRegistroRepository.save(ConsentRegistro.builder()
                .tipoConsentimiento(type)
                .usuario(user)
                .versionConsentimiento(type.getVersionConsentimiento())
                .indAceptado("S")
                .fechaAceptacion(LocalDateTime.now())
                .ipAceptacion(resolveClientIp(httpRequest))
                .userAgent(resolveUserAgent(httpRequest))
                .detalleAceptacion(normalizeDetail(request.detalleAceptacion()))
                .estadoRegistro("ACTIVO")
                .build());

        auditTrailService.recordEvent(
                "CONSENTIMIENTO_ACEPTADO",
                username,
                "Consentimiento aceptado: " + type.getCodigoConsentimiento() + " v" + type.getVersionConsentimiento(),
                httpRequest
        );

        return mapHistory(saved);
    }

    private String normalizeDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return "Aceptacion registrada desde el flujo self-service del usuario autenticado.";
        }
        return detail.trim();
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

    private ConsentimientoTipoResponse mapType(ConsentTipo type) {
        return new ConsentimientoTipoResponse(
                type.getId(),
                type.getCodigoConsentimiento(),
                type.getNombreConsentimiento(),
                type.getTextoConsentimiento(),
                type.getVersionConsentimiento(),
                "S".equalsIgnoreCase(type.getRequerido())
        );
    }

    private ConsentimientoHistorialResponse mapHistory(ConsentRegistro record) {
        return new ConsentimientoHistorialResponse(
                record.getId(),
                record.getTipoConsentimiento().getId(),
                record.getTipoConsentimiento().getCodigoConsentimiento(),
                record.getTipoConsentimiento().getNombreConsentimiento(),
                record.getVersionConsentimiento(),
                record.getFechaAceptacion(),
                record.getDetalleAceptacion(),
                "S".equalsIgnoreCase(record.getIndAceptado())
        );
    }
}
