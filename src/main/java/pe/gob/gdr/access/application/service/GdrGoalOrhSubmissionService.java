package pe.gob.gdr.access.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.CreateGoalOrhSubmissionRequest;
import pe.gob.gdr.access.application.dto.response.ActiveCycleContextResponse;
import pe.gob.gdr.access.application.dto.response.GoalOrhSubmissionResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.model.GdrGoalOrhSubmission;
import pe.gob.gdr.access.domain.model.GoalOrhSubmissionStatus;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.GdrGoalOrhSubmissionRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;

@Service
public class GdrGoalOrhSubmissionService {

    private static final String ACTIVE_RECORD_STATUS = "ACTIVO";

    private final GdrGoalRepository goalRepository;
    private final GdrGoalOrhSubmissionRepository goalOrhSubmissionRepository;
    private final GdrAccessPolicyService gdrAccessPolicyService;

    public GdrGoalOrhSubmissionService(
            GdrGoalRepository goalRepository,
            GdrGoalOrhSubmissionRepository goalOrhSubmissionRepository,
            GdrAccessPolicyService gdrAccessPolicyService
    ) {
        this.goalRepository = goalRepository;
        this.goalOrhSubmissionRepository = goalOrhSubmissionRepository;
        this.gdrAccessPolicyService = gdrAccessPolicyService;
    }

    @Transactional
    public GoalOrhSubmissionResponse createGoalOrhSubmission(
            Long goalId,
            CreateGoalOrhSubmissionRequest request,
            String username
    ) {
        User user = gdrAccessPolicyService.loadUserWithContext(username);
        ActiveCycleContextResponse context = gdrAccessPolicyService.resolveContext(user);
        GdrGoal goal = goalRepository.findActiveByIdInActiveCycle(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la meta solicitada."));

        if (!gdrAccessPolicyService.canSubmitGoalToOrh(user, context, goal)) {
            throw new AccessDeniedException("No tiene permisos para enviar la meta indicada a ORH.");
        }

        if (goalOrhSubmissionRepository.existsActiveSubmission(goal.getId(), GoalOrhSubmissionStatus.ENVIADO)) {
            throw new DomainException("La meta ya tiene un envio activo a ORH.");
        }

        GdrGoalOrhSubmission submission = GdrGoalOrhSubmission.builder()
                .goal(goal)
                .assignment(goal.getAssignment())
                .submittedByUser(user)
                .submittedByUsername(user.getUsername())
                .submittedFunctionalActor(context.functionalActor())
                .comment(normalizeOptionalText(request.comment()))
                .status(GoalOrhSubmissionStatus.ENVIADO)
                .recordStatus(ACTIVE_RECORD_STATUS)
                .updatedByUsername(user.getUsername())
                .build();

        return mapResponse(goalOrhSubmissionRepository.save(submission));
    }

    private GoalOrhSubmissionResponse mapResponse(GdrGoalOrhSubmission submission) {
        return new GoalOrhSubmissionResponse(
                submission.getId(),
                submission.getGoal().getId(),
                submission.getAssignment().getId(),
                submission.getGoal().getTitle(),
                submission.getStatus(),
                submission.getStatus().getDisplayName(),
                submission.getSubmittedByUsername(),
                submission.getSubmittedFunctionalActor(),
                submission.getComment(),
                submission.getSubmittedAt(),
                submission.getCreatedAt(),
                submission.getUpdatedAt()
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
