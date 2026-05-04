package pe.gob.gdr.access.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.ReviewOrhReceptionRequest;
import pe.gob.gdr.access.application.dto.response.OrhGoalChangeRequestItemResponse;
import pe.gob.gdr.access.application.dto.response.OrhGoalSubmissionItemResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrGoalChangeRequest;
import pe.gob.gdr.access.domain.model.GdrGoalOrhSubmission;
import pe.gob.gdr.access.domain.model.GoalChangeRequestStatus;
import pe.gob.gdr.access.domain.model.GoalOrhSubmissionStatus;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.GdrGoalChangeRequestRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalOrhSubmissionRepository;

@Service
public class OrhReceptionService {

    private final GdrGoalChangeRequestRepository goalChangeRequestRepository;
    private final GdrGoalOrhSubmissionRepository goalOrhSubmissionRepository;
    private final GdrAccessPolicyService gdrAccessPolicyService;

    public OrhReceptionService(
            GdrGoalChangeRequestRepository goalChangeRequestRepository,
            GdrGoalOrhSubmissionRepository goalOrhSubmissionRepository,
            GdrAccessPolicyService gdrAccessPolicyService
    ) {
        this.goalChangeRequestRepository = goalChangeRequestRepository;
        this.goalOrhSubmissionRepository = goalOrhSubmissionRepository;
        this.gdrAccessPolicyService = gdrAccessPolicyService;
    }

    @Transactional(readOnly = true)
    public List<OrhGoalChangeRequestItemResponse> listChangeRequests() {
        return goalChangeRequestRepository.findActiveReceptionItemsInActiveCycle().stream()
                .map(this::mapChangeRequest)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrhGoalSubmissionItemResponse> listSubmissions() {
        return goalOrhSubmissionRepository.findActiveReceptionItemsInActiveCycle().stream()
                .map(this::mapSubmission)
                .toList();
    }

    @Transactional
    public OrhGoalChangeRequestItemResponse reviewChangeRequest(
            Long id,
            ReviewOrhReceptionRequest request,
            String username
    ) {
        User reviewer = gdrAccessPolicyService.loadUserWithContext(username);
        GdrGoalChangeRequest changeRequest = goalChangeRequestRepository.findActiveByIdInActiveCycle(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la solicitud de modificacion."));

        if (changeRequest.getStatus() != GoalChangeRequestStatus.PENDIENTE) {
            throw new DomainException("El registro ya fue revisado por ORH.");
        }

        changeRequest.setStatus(GoalChangeRequestStatus.REVISADO);
        changeRequest.setReviewedAt(java.time.LocalDateTime.now());
        changeRequest.setReviewedByUser(reviewer);
        changeRequest.setReviewedByUsername(reviewer.getUsername());
        changeRequest.setOrhReviewComment(normalizeOptionalText(request.comment()));
        changeRequest.setUpdatedByUsername(reviewer.getUsername());

        return mapChangeRequest(goalChangeRequestRepository.save(changeRequest));
    }

    @Transactional
    public OrhGoalSubmissionItemResponse reviewSubmission(
            Long id,
            ReviewOrhReceptionRequest request,
            String username
    ) {
        User reviewer = gdrAccessPolicyService.loadUserWithContext(username);
        GdrGoalOrhSubmission submission = goalOrhSubmissionRepository.findActiveByIdInActiveCycle(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el envio a ORH."));

        if (submission.getStatus() != GoalOrhSubmissionStatus.ENVIADO) {
            throw new DomainException("El registro ya fue revisado por ORH.");
        }

        submission.setStatus(GoalOrhSubmissionStatus.REVISADO);
        submission.setReviewedAt(java.time.LocalDateTime.now());
        submission.setReviewedByUser(reviewer);
        submission.setReviewedByUsername(reviewer.getUsername());
        submission.setOrhReviewComment(normalizeOptionalText(request.comment()));
        submission.setUpdatedByUsername(reviewer.getUsername());

        return mapSubmission(goalOrhSubmissionRepository.save(submission));
    }

    private OrhGoalChangeRequestItemResponse mapChangeRequest(GdrGoalChangeRequest request) {
        return new OrhGoalChangeRequestItemResponse(
                request.getId(),
                request.getGoal().getId(),
                request.getAssignment().getId(),
                request.getGoal().getTitle(),
                request.getAssignment().getEvaluatedPerson().getDisplayName(),
                request.getGoal().getIndicator().getName(),
                request.getRequestType(),
                request.getRequestType().getDisplayName(),
                request.getReason(),
                request.getRequestedByUsername(),
                request.getStatus(),
                request.getStatus().getDisplayName(),
                request.getCreatedAt(),
                request.getReviewedAt(),
                request.getReviewedByUsername(),
                request.getOrhReviewComment()
        );
    }

    private OrhGoalSubmissionItemResponse mapSubmission(GdrGoalOrhSubmission submission) {
        return new OrhGoalSubmissionItemResponse(
                submission.getId(),
                submission.getGoal().getId(),
                submission.getAssignment().getId(),
                submission.getGoal().getTitle(),
                submission.getAssignment().getEvaluatedPerson().getDisplayName(),
                submission.getGoal().getIndicator().getName(),
                submission.getSubmittedByUsername(),
                submission.getSubmittedFunctionalActor(),
                submission.getComment(),
                submission.getStatus(),
                submission.getStatus().getDisplayName(),
                submission.getSubmittedAt(),
                submission.getReviewedAt(),
                submission.getReviewedByUsername(),
                submission.getOrhReviewComment()
        );
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
