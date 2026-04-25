package pe.gob.gdr.access.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.GuardarEvidenciaRequest;
import pe.gob.gdr.access.application.dto.request.RevisionEvidenciaRequest;
import pe.gob.gdr.access.application.dto.response.DetalleEvidenciaResponse;
import pe.gob.gdr.access.application.dto.response.ResumenEvidenciaResponse;
import pe.gob.gdr.access.application.dto.response.RevisionEvidenciaResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrCorrectiveAction;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.model.GdrEvidenceReview;
import pe.gob.gdr.access.domain.model.GdrEvidenceStatus;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.repository.GdrCorrectiveActionRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceReviewRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceStatusRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;

@Service
public class GdrEvidenceService {

    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final String STATUS_OBSERVED = "OBSERVED";
    private static final String STATUS_SUBSANATED = "SUBSANATED";
    private static final String STATUS_APPROVED = "APPROVED";

    private final GdrGoalRepository goalRepository;
    private final GdrEvidenceRepository evidenceRepository;
    private final GdrEvidenceStatusRepository evidenceStatusRepository;
    private final GdrEvidenceReviewRepository evidenceReviewRepository;
    private final GdrCorrectiveActionRepository correctiveActionRepository;

    public GdrEvidenceService(
            GdrGoalRepository goalRepository,
            GdrEvidenceRepository evidenceRepository,
            GdrEvidenceStatusRepository evidenceStatusRepository,
            GdrEvidenceReviewRepository evidenceReviewRepository,
            GdrCorrectiveActionRepository correctiveActionRepository
    ) {
        this.goalRepository = goalRepository;
        this.evidenceRepository = evidenceRepository;
        this.evidenceStatusRepository = evidenceStatusRepository;
        this.evidenceReviewRepository = evidenceReviewRepository;
        this.correctiveActionRepository = correctiveActionRepository;
    }

    public List<ResumenEvidenciaResponse> listGoalEvidences(Long goalId) {
        ensureActiveGoal(goalId);
        return evidenceRepository.findActiveByGoalIdInActiveCycle(goalId).stream()
                .map(this::mapSummary)
                .toList();
    }

    public DetalleEvidenciaResponse getEvidence(Long evidenceId) {
        GdrEvidence evidence = getActiveEvidence(evidenceId);
        return mapDetail(evidence);
    }

    @Transactional
    public DetalleEvidenciaResponse createEvidence(Long goalId, GuardarEvidenciaRequest request) {
        GdrGoal goal = ensureActiveGoal(goalId);
        GdrEvidence evidence = new GdrEvidence();
        evidence.setGoal(goal);
        evidence.setEvidenceStatus(resolveStatus(STATUS_REGISTERED));
        evidence.setStatus("ACTIVE");
        applyEvidenceRequest(evidence, request);
        return mapDetail(evidenceRepository.save(evidence));
    }

    @Transactional
    public DetalleEvidenciaResponse updateEvidence(Long evidenceId, GuardarEvidenciaRequest request) {
        GdrEvidence evidence = getActiveEvidence(evidenceId);
        applyEvidenceRequest(evidence, request);

        if (STATUS_OBSERVED.equalsIgnoreCase(evidence.getEvidenceStatus().getStatusCode())) {
            evidence.setEvidenceStatus(resolveStatus(STATUS_SUBSANATED));
            closeOpenCorrectiveActions(evidenceId);
        }

        return mapDetail(evidenceRepository.save(evidence));
    }

    @Transactional
    public DetalleEvidenciaResponse reviewEvidence(Long evidenceId, RevisionEvidenciaRequest request) {
        GdrEvidence evidence = getActiveEvidence(evidenceId);
        String decisionCode = normalizeRequired(request.decisionCode(), "La decision de revision es obligatoria.");
        if (!STATUS_APPROVED.equalsIgnoreCase(decisionCode) && !STATUS_OBSERVED.equalsIgnoreCase(decisionCode)) {
            throw new DomainException("La decision de revision debe ser APPROVED u OBSERVED.");
        }

        GdrEvidenceStatus evidenceStatus = resolveStatus(decisionCode);
        String reviewComment = normalizeOptional(request.comment());
        String correctiveActionDetail = normalizeOptional(request.correctiveActionDetail());

        if (STATUS_OBSERVED.equalsIgnoreCase(decisionCode)) {
            if (reviewComment == null) {
                throw new DomainException("La revision observada requiere un comentario.");
            }
            if (correctiveActionDetail == null) {
                throw new DomainException("La revision observada requiere una accion correctiva.");
            }
        }

        GdrEvidenceReview savedReview = evidenceReviewRepository.save(GdrEvidenceReview.builder()
                .evidence(evidence)
                .evidenceStatus(evidenceStatus)
                .reviewComment(reviewComment)
                .build());

        evidence.setEvidenceStatus(evidenceStatus);
        evidenceRepository.save(evidence);

        if (STATUS_OBSERVED.equalsIgnoreCase(decisionCode)) {
            closeOpenCorrectiveActions(evidenceId);
            correctiveActionRepository.save(GdrCorrectiveAction.builder()
                    .evidenceReview(savedReview)
                    .evidence(evidence)
                    .actionDetail(correctiveActionDetail)
                    .actionStatus("OPEN")
                    .build());
        } else {
            closeOpenCorrectiveActions(evidenceId);
        }

        return mapDetail(getActiveEvidence(evidenceId));
    }

    private GdrGoal ensureActiveGoal(Long goalId) {
        return goalRepository.findActiveByIdInActiveCycle(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro una meta activa para la evidencia."));
    }

    private GdrEvidence getActiveEvidence(Long evidenceId) {
        return evidenceRepository.findActiveByIdInActiveCycle(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la evidencia solicitada."));
    }

    private GdrEvidenceStatus resolveStatus(String statusCode) {
        return evidenceStatusRepository.findActiveByCode(statusCode)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el estado de evidencia requerido."));
    }

    private void applyEvidenceRequest(GdrEvidence evidence, GuardarEvidenciaRequest request) {
        evidence.setTitle(normalizeRequired(request.title(), "El titulo de la evidencia es obligatorio."));
        evidence.setDetail(normalizeOptional(request.detail()));
        evidence.setExpectedDate(request.expectedDate());
    }

    private ResumenEvidenciaResponse mapSummary(GdrEvidence evidence) {
        Optional<GdrEvidenceReview> latestReview = evidenceReviewRepository.findLatestByEvidenceId(evidence.getId());
        return new ResumenEvidenciaResponse(
                evidence.getId(),
                evidence.getGoal().getId(),
                evidence.getTitle(),
                evidence.getDetail(),
                evidence.getExpectedDate(),
                evidence.getEvidenceStatus().getStatusCode(),
                evidence.getEvidenceStatus().getStatusName(),
                latestReview.map(GdrEvidenceReview::getReviewComment).orElse(null)
        );
    }

    private DetalleEvidenciaResponse mapDetail(GdrEvidence evidence) {
        List<RevisionEvidenciaResponse> reviews = evidenceReviewRepository.findByEvidenceId(evidence.getId()).stream()
                .map(this::mapReview)
                .toList();
        Optional<GdrCorrectiveAction> openAction = correctiveActionRepository.findOpenByEvidenceId(evidence.getId()).stream()
                .findFirst();

        return new DetalleEvidenciaResponse(
                evidence.getId(),
                evidence.getGoal().getId(),
                evidence.getGoal().getTitle(),
                evidence.getGoal().getAssignment().getEvaluatedPerson().getDisplayName(),
                evidence.getGoal().getIndicator().getName(),
                evidence.getTitle(),
                evidence.getDetail(),
                evidence.getExpectedDate(),
                evidence.getEvidenceStatus().getStatusCode(),
                evidence.getEvidenceStatus().getStatusName(),
                openAction.map(GdrCorrectiveAction::getActionDetail).orElse(null),
                openAction.map(GdrCorrectiveAction::getActionStatus).orElse(null),
                reviews
        );
    }

    private RevisionEvidenciaResponse mapReview(GdrEvidenceReview review) {
        Optional<GdrCorrectiveAction> action = correctiveActionRepository.findByReviewId(review.getId());
        return new RevisionEvidenciaResponse(
                review.getId(),
                review.getEvidenceStatus().getStatusCode(),
                review.getEvidenceStatus().getStatusName(),
                review.getReviewComment(),
                action.map(GdrCorrectiveAction::getActionDetail).orElse(null),
                action.map(GdrCorrectiveAction::getActionStatus).orElse(null),
                review.getReviewedAt()
        );
    }

    private void closeOpenCorrectiveActions(Long evidenceId) {
        List<GdrCorrectiveAction> openActions = correctiveActionRepository.findOpenByEvidenceId(evidenceId);
        if (openActions.isEmpty()) {
            return;
        }

        LocalDateTime completedAt = LocalDateTime.now();
        openActions.forEach((action) -> {
            action.setActionStatus("COMPLETED");
            action.setCompletedAt(completedAt);
            correctiveActionRepository.save(action);
        });
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new DomainException(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
