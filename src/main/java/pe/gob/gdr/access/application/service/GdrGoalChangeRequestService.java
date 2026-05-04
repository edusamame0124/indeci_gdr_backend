package pe.gob.gdr.access.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.CreateGoalChangeRequestRequest;
import pe.gob.gdr.access.application.dto.response.ActiveCycleContextResponse;
import pe.gob.gdr.access.application.dto.response.GoalChangeRequestResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.model.GdrGoalChangeRequest;
import pe.gob.gdr.access.domain.model.GoalChangeRequestStatus;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.GdrGoalChangeRequestRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;

@Service
public class GdrGoalChangeRequestService {

    private static final String ACTIVE_RECORD_STATUS = "ACTIVO";

    private final GdrGoalRepository goalRepository;
    private final GdrGoalChangeRequestRepository goalChangeRequestRepository;
    private final GdrAccessPolicyService gdrAccessPolicyService;

    public GdrGoalChangeRequestService(
            GdrGoalRepository goalRepository,
            GdrGoalChangeRequestRepository goalChangeRequestRepository,
            GdrAccessPolicyService gdrAccessPolicyService
    ) {
        this.goalRepository = goalRepository;
        this.goalChangeRequestRepository = goalChangeRequestRepository;
        this.gdrAccessPolicyService = gdrAccessPolicyService;
    }

    @Transactional
    public GoalChangeRequestResponse createGoalChangeRequest(
            Long goalId,
            CreateGoalChangeRequestRequest request,
            String username
    ) {
        User user = gdrAccessPolicyService.loadUserWithContext(username);
        ActiveCycleContextResponse context = gdrAccessPolicyService.resolveContext(user);
        GdrGoal goal = goalRepository.findActiveByIdInActiveCycle(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la meta solicitada."));

        if (!gdrAccessPolicyService.canCreateGoalChangeRequest(user, context, goal)) {
            throw new AccessDeniedException("No tiene permisos para solicitar modificacion sobre la meta indicada.");
        }

        String reason = normalizeRequired(request.reason(), "El motivo de la solicitud es obligatorio.");
        String comment = normalizeOptionalText(request.comment());

        if (goalChangeRequestRepository.existsActiveRequest(
                goal.getId(),
                user.getId(),
                request.requestType(),
                GoalChangeRequestStatus.PENDIENTE
        )) {
            throw new DomainException("Ya existe una solicitud pendiente para la misma meta, usuario y tipo de modificacion.");
        }

        GdrGoalChangeRequest changeRequest = GdrGoalChangeRequest.builder()
                .goal(goal)
                .assignment(goal.getAssignment())
                .requestedByUser(user)
                .requestedByUsername(user.getUsername())
                .requestType(request.requestType())
                .reason(reason)
                .comment(comment)
                .status(GoalChangeRequestStatus.PENDIENTE)
                .recordStatus(ACTIVE_RECORD_STATUS)
                .updatedByUsername(user.getUsername())
                .build();

        return mapResponse(goalChangeRequestRepository.save(changeRequest));
    }

    private GoalChangeRequestResponse mapResponse(GdrGoalChangeRequest request) {
        return new GoalChangeRequestResponse(
                request.getId(),
                request.getGoal().getId(),
                request.getAssignment().getId(),
                request.getGoal().getTitle(),
                request.getRequestType(),
                request.getRequestType().getDisplayName(),
                request.getReason(),
                request.getComment(),
                request.getStatus(),
                request.getStatus().getDisplayName(),
                request.getRequestedByUsername(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException(message);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
