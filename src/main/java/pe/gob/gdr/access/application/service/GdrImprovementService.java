package pe.gob.gdr.access.application.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.RegistrarOportunidadMejoraRequest;
import pe.gob.gdr.access.application.dto.request.RegistrarSeguimientoMejoraRequest;
import pe.gob.gdr.access.application.dto.response.OportunidadMejoraDetalleResponse;
import pe.gob.gdr.access.application.dto.response.OportunidadMejoraResumenResponse;
import pe.gob.gdr.access.application.dto.response.SeguimientoMejoraResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrImprovementFollowup;
import pe.gob.gdr.access.domain.model.GdrImprovementOpportunity;
import pe.gob.gdr.access.domain.model.GdrImprovementStatus;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.repository.GdrImprovementFollowupRepository;
import pe.gob.gdr.access.domain.repository.GdrImprovementOpportunityRepository;
import pe.gob.gdr.access.domain.repository.GdrImprovementStatusRepository;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;

@Service
public class GdrImprovementService {

    private static final String ACTIVE_RECORD_STATUS = "ACTIVO";
    private static final Logger LOGGER = LoggerFactory.getLogger(GdrImprovementService.class);

    private final GdrImprovementStatusRepository improvementStatusRepository;
    private final GdrImprovementOpportunityRepository improvementOpportunityRepository;
    private final GdrImprovementFollowupRepository improvementFollowupRepository;
    private final GdrResultRepository resultRepository;
    private final NotificacionesService notificacionesService;

    public GdrImprovementService(
            GdrImprovementStatusRepository improvementStatusRepository,
            GdrImprovementOpportunityRepository improvementOpportunityRepository,
            GdrImprovementFollowupRepository improvementFollowupRepository,
            GdrResultRepository resultRepository,
            NotificacionesService notificacionesService
    ) {
        this.improvementStatusRepository = improvementStatusRepository;
        this.improvementOpportunityRepository = improvementOpportunityRepository;
        this.improvementFollowupRepository = improvementFollowupRepository;
        this.resultRepository = resultRepository;
        this.notificacionesService = notificacionesService;
    }

    public List<OportunidadMejoraResumenResponse> listImprovements(Long evaluatedId, Long cycleId) {
        return improvementOpportunityRepository.findActiveByEvaluatedIdAndCycle(evaluatedId, cycleId).stream()
                .map(this::mapSummary)
                .toList();
    }

    public OportunidadMejoraDetalleResponse getImprovement(Long opportunityId) {
        GdrImprovementOpportunity opportunity = resolveOpportunity(opportunityId);
        return mapDetail(opportunity, loadFollowups(opportunity.getId()));
    }

    @Transactional
    public OportunidadMejoraDetalleResponse createImprovement(
            RegistrarOportunidadMejoraRequest request,
            String username,
            Long cycleId
    ) {
        String stage = "validar estado inicial";
        if (normalizeStateCode(request.estadoCodigo()) != null && !"OPEN".equalsIgnoreCase(request.estadoCodigo())) {
            throw new DomainException("La oportunidad de mejora solo puede registrarse inicialmente en estado OPEN.");
        }

        try {
            stage = "resolver resultado consolidado";
            GdrResult result = resultRepository.findByEvaluatedPersonIdAndCycle(request.evaluatedId(), cycleId)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontro el resultado consolidado del evaluado."));

            stage = "resolver estado OPEN";
            GdrImprovementStatus openStatus = resolveStatus("OPEN");

            stage = "construir oportunidad de mejora";
            GdrImprovementOpportunity opportunity = GdrImprovementOpportunity.builder()
                    .result(result)
                    .improvementStatus(openStatus)
                    .description(normalizeRequired(request.descripcion(), "La descripcion es obligatoria."))
                    .responsible(normalizeRequired(request.responsable(), "El responsable es obligatorio."))
                    .targetDate(request.plazoCompromiso())
                    .registeredUser(username)
                    .recordStatus("ACTIVO")
                    .build();

            stage = "persistir oportunidad de mejora";
            GdrImprovementOpportunity savedOpportunity = improvementOpportunityRepository.save(opportunity);
            notificacionesService.emitForUser(
                    username,
                    NotificacionesService.OPORTUNIDAD_MEJORA_REGISTRADA,
                    "OM-" + savedOpportunity.getId()
            );

            stage = "armar respuesta de alta";
            return mapDetail(savedOpportunity, Collections.emptyList());
        } catch (ResourceNotFoundException | DomainException exception) {
            LOGGER.error(
                    "Error controlado al registrar oportunidad de mejora. stage={}, evaluatedId={}, username={}",
                    stage,
                    request.evaluatedId(),
                    username,
                    exception
            );
            throw exception;
        } catch (Exception exception) {
            LOGGER.error(
                    "Error no controlado al registrar oportunidad de mejora. stage={}, evaluatedId={}, username={}",
                    stage,
                    request.evaluatedId(),
                    username,
                    exception
            );
            throw new DomainException(buildDiagnosticMessage(stage, exception));
        }
    }

    @Transactional
    public OportunidadMejoraDetalleResponse updateImprovement(
            Long opportunityId,
            RegistrarOportunidadMejoraRequest request,
            String username
    ) {
        GdrImprovementOpportunity opportunity = resolveOpportunity(opportunityId);
        boolean wasClosed = "CLOSED".equalsIgnoreCase(opportunity.getImprovementStatus().getCode());
        if (!opportunity.getResult().getAssignment().getEvaluatedPerson().getId().equals(request.evaluatedId())) {
            throw new DomainException("La oportunidad de mejora no corresponde al evaluado indicado.");
        }

        opportunity.setDescription(normalizeRequired(request.descripcion(), "La descripcion es obligatoria."));
        opportunity.setResponsible(normalizeRequired(request.responsable(), "El responsable es obligatorio."));
        opportunity.setTargetDate(request.plazoCompromiso());

        String requestedState = normalizeStateCode(request.estadoCodigo());
        if (requestedState == null || "OPEN".equals(requestedState)) {
            if ("CLOSED".equalsIgnoreCase(opportunity.getImprovementStatus().getCode())) {
                throw new DomainException("Una oportunidad cerrada no puede reabrirse en este lote.");
            }
            opportunity.setImprovementStatus(resolveStatus("OPEN"));
            opportunity.setClosureComment(null);
            opportunity.setClosedAt(null);
        } else if ("CLOSED".equals(requestedState)) {
            opportunity.setImprovementStatus(resolveStatus("CLOSED"));
            opportunity.setClosureComment(normalizeRequired(
                    request.comentarioCierre(),
                    "El comentario de cierre es obligatorio para cerrar la oportunidad."
            ));
            opportunity.setClosedAt(LocalDateTime.now());
        } else {
            throw new DomainException("El estado solicitado no es valido para el lote actual.");
        }

        if (opportunity.getRegisteredUser() == null || opportunity.getRegisteredUser().isBlank()) {
            opportunity.setRegisteredUser(username);
        }

        GdrImprovementOpportunity savedOpportunity = improvementOpportunityRepository.save(opportunity);
        if (!wasClosed && "CLOSED".equalsIgnoreCase(savedOpportunity.getImprovementStatus().getCode())) {
            notificacionesService.emitForUser(
                    username,
                    NotificacionesService.OPORTUNIDAD_MEJORA_CERRADA,
                    "OM-" + savedOpportunity.getId()
            );
        }
        return mapDetail(savedOpportunity, loadFollowups(savedOpportunity.getId()));
    }

    @Transactional
    public OportunidadMejoraDetalleResponse registerFollowup(
            Long opportunityId,
            RegistrarSeguimientoMejoraRequest request,
            String username
    ) {
        GdrImprovementOpportunity opportunity = resolveOpportunity(opportunityId);
        if ("CLOSED".equalsIgnoreCase(opportunity.getImprovementStatus().getCode())) {
            throw new DomainException("La oportunidad de mejora ya se encuentra cerrada.");
        }

        improvementFollowupRepository.save(GdrImprovementFollowup.builder()
                .opportunity(opportunity)
                .followupComment(normalizeRequired(
                        request.comentarioSeguimiento(),
                        "El comentario de seguimiento es obligatorio."
                ))
                .registeredUser(username)
                .registeredAt(LocalDateTime.now())
                .build());
        notificacionesService.emitForUser(
                username,
                NotificacionesService.SEGUIMIENTO_MEJORA_REGISTRADO,
                "OM-" + opportunityId
        );

        GdrImprovementOpportunity refreshedOpportunity = resolveOpportunity(opportunityId);
        return mapDetail(refreshedOpportunity, loadFollowups(opportunityId));
    }

    private GdrImprovementOpportunity resolveOpportunity(Long opportunityId) {
        return improvementOpportunityRepository.findActiveById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la oportunidad de mejora solicitada."));
    }

    private GdrImprovementStatus resolveStatus(String statusCode) {
        return improvementStatusRepository.findActiveByCode(statusCode)
                .orElseGet(() -> provisionBaseStatus(statusCode));
    }

    private GdrImprovementStatus provisionBaseStatus(String statusCode) {
        if ("OPEN".equalsIgnoreCase(statusCode)) {
            return improvementStatusRepository.save(GdrImprovementStatus.builder()
                    .code("OPEN")
                    .name("Abierta")
                    .description("Oportunidad de mejora abierta y en seguimiento.")
                    .recordStatus(ACTIVE_RECORD_STATUS)
                    .build());
        }
        if ("CLOSED".equalsIgnoreCase(statusCode)) {
            return improvementStatusRepository.save(GdrImprovementStatus.builder()
                    .code("CLOSED")
                    .name("Cerrada")
                    .description("Oportunidad de mejora cerrada en el lote actual.")
                    .recordStatus(ACTIVE_RECORD_STATUS)
                    .build());
        }
        throw new ResourceNotFoundException("No se encontro el estado solicitado para oportunidad de mejora.");
    }

    private OportunidadMejoraResumenResponse mapSummary(GdrImprovementOpportunity opportunity) {
        return new OportunidadMejoraResumenResponse(
                opportunity.getId(),
                opportunity.getResult().getId(),
                opportunity.getResult().getAssignment().getEvaluatedPerson().getId(),
                opportunity.getResult().getAssignment().getEvaluatedPerson().getDisplayName(),
                opportunity.getDescription(),
                opportunity.getResponsible(),
                opportunity.getTargetDate(),
                opportunity.getImprovementStatus().getCode(),
                opportunity.getImprovementStatus().getName(),
                opportunity.getCreatedAt()
        );
    }

    private List<SeguimientoMejoraResponse> loadFollowups(Long opportunityId) {
        return improvementFollowupRepository.findByOpportunityId(opportunityId).stream()
                .map((followup) -> new SeguimientoMejoraResponse(
                        followup.getId(),
                        followup.getFollowupComment(),
                        followup.getRegisteredUser(),
                        followup.getRegisteredAt()
                ))
                .toList();
    }

    private OportunidadMejoraDetalleResponse mapDetail(
            GdrImprovementOpportunity opportunity,
            List<SeguimientoMejoraResponse> followups
    ) {
        return new OportunidadMejoraDetalleResponse(
                opportunity.getId(),
                opportunity.getResult().getId(),
                opportunity.getResult().getAssignment().getEvaluatedPerson().getId(),
                opportunity.getResult().getAssignment().getEvaluatedPerson().getDisplayName(),
                opportunity.getResult().getAssignment().getEvaluatorPerson().getDisplayName(),
                opportunity.getResult().getAssignment().getCycle().getName(),
                opportunity.getDescription(),
                opportunity.getResponsible(),
                opportunity.getTargetDate(),
                opportunity.getImprovementStatus().getCode(),
                opportunity.getImprovementStatus().getName(),
                opportunity.getClosureComment(),
                opportunity.getClosedAt(),
                opportunity.getCreatedAt(),
                followups
        );
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException(message);
        }
        return value.trim();
    }

    private String normalizeStateCode(String stateCode) {
        if (stateCode == null || stateCode.trim().isEmpty()) {
            return null;
        }
        return stateCode.trim().toUpperCase();
    }

    private String buildDiagnosticMessage(String stage, Exception exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        String rootMessage = rootCause.getMessage();
        if (rootMessage == null || rootMessage.isBlank()) {
            rootMessage = rootCause.getClass().getSimpleName();
        }

        return "Error al registrar la oportunidad de mejora en la etapa '"
                + stage
                + "'. Causa raiz: "
                + rootMessage;
    }
}
