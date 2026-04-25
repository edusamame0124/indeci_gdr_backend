package pe.gob.gdr.access.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.GoalUpsertRequest;
import pe.gob.gdr.access.application.dto.response.GoalDetailResponse;
import pe.gob.gdr.access.application.dto.response.GoalSummaryResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.model.GdrIndicator;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrIndicatorRepository;

@Service
public class GdrGoalService {

    private final ActiveCycleRepository activeCycleRepository;
    private final GdrGoalRepository goalRepository;
    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final GdrIndicatorRepository indicatorRepository;
    private final GdrAccessPolicyService gdrAccessPolicyService;

    public GdrGoalService(
            ActiveCycleRepository activeCycleRepository,
            GdrGoalRepository goalRepository,
            GdrEvaluationAssignmentRepository assignmentRepository,
            GdrIndicatorRepository indicatorRepository,
            GdrAccessPolicyService gdrAccessPolicyService
    ) {
        this.activeCycleRepository = activeCycleRepository;
        this.goalRepository = goalRepository;
        this.assignmentRepository = assignmentRepository;
        this.indicatorRepository = indicatorRepository;
        this.gdrAccessPolicyService = gdrAccessPolicyService;
    }

    public List<GoalSummaryResponse> listGoals(String username) {
        User user = gdrAccessPolicyService.loadUserWithContext(username);
        List<GdrGoal> goals = gdrAccessPolicyService.isAdminSistema(user) || gdrAccessPolicyService.isOrh(user)
                ? goalRepository.findActiveGoalsForActiveCycle()
                : goalRepository.findActiveGoalsByPersonIdInActiveCycle(user.getPerson().getId());

        return goals.stream()
                .map(this::mapSummary)
                .toList();
    }

    public GoalDetailResponse getGoal(String username, Long goalId) {
        User user = gdrAccessPolicyService.loadUserWithContext(username);
        GdrGoal goal = goalRepository.findActiveByIdInActiveCycle(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la meta solicitada."));
        ensureCanViewGoal(user, goal);
        return mapDetail(goal);
    }

    @Transactional
    public GoalDetailResponse createGoal(String username, GoalUpsertRequest request) {
        User user = gdrAccessPolicyService.loadUserWithContext(username);
        ensureActiveCycleExists();
        GdrGoal goal = new GdrGoal();
        applyRequest(user, goal, request, null);
        return mapDetail(goalRepository.save(goal));
    }

    @Transactional
    public GoalDetailResponse updateGoal(String username, Long goalId, GoalUpsertRequest request) {
        User user = gdrAccessPolicyService.loadUserWithContext(username);
        ensureActiveCycleExists();
        GdrGoal goal = goalRepository.findActiveByIdInActiveCycle(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la meta solicitada."));
        ensureCanManageGoal(user, goal);
        applyRequest(user, goal, request, goalId);
        return mapDetail(goalRepository.save(goal));
    }

    private void ensureActiveCycleExists() {
        if (activeCycleRepository.findActiveCycle().isEmpty()) {
            throw new DomainException("No existe un ciclo activo para registrar metas.");
        }
    }

    private void applyRequest(User user, GdrGoal goal, GoalUpsertRequest request, Long goalId) {
        GdrEvaluationAssignment assignment = assignmentRepository.findActiveByIdInActiveCycle(request.assignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró una asignación activa para la meta."));
        ensureCanManageAssignment(user, assignment);

        GdrIndicator indicator = indicatorRepository.findActiveById(request.indicatorId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró un indicador activo para la meta."));

        BigDecimal cumulativeWeight = goalRepository.sumWeightByAssignmentExcludingGoal(assignment.getId(), goalId)
                .add(request.weight());
        if (cumulativeWeight.compareTo(new BigDecimal("100.00")) > 0) {
            throw new DomainException("La suma de pesos de la asignación no puede exceder 100.");
        }

        goal.setAssignment(assignment);
        goal.setIndicator(indicator);
        goal.setTitle(request.title().trim());
        goal.setDescription(normalizeOptionalText(request.description()));
        goal.setExpectedValue(request.expectedValue().stripTrailingZeros());
        goal.setWeight(request.weight().setScale(2, RoundingMode.HALF_UP));
        goal.setStatus("ACTIVE");
    }

    private void ensureCanViewGoal(User user, GdrGoal goal) {
        if (gdrAccessPolicyService.isAdminSistema(user) || gdrAccessPolicyService.isOrh(user)) {
            return;
        }
        Long linkedPersonId = user.getPerson() != null ? user.getPerson().getId() : null;
        boolean linkedToAssignment = linkedPersonId != null
                && (Objects.equals(goal.getAssignment().getEvaluatorPerson().getId(), linkedPersonId)
                || Objects.equals(goal.getAssignment().getEvaluatedPerson().getId(), linkedPersonId));
        if (!linkedToAssignment) {
            throw new AccessDeniedException("No tiene permisos para consultar la meta solicitada.");
        }
    }

    private void ensureCanManageGoal(User user, GdrGoal goal) {
        if (gdrAccessPolicyService.isAdminSistema(user) || gdrAccessPolicyService.isOrh(user)) {
            return;
        }
        ensureCanManageAssignment(user, goal.getAssignment());
    }

    private void ensureCanManageAssignment(User user, GdrEvaluationAssignment assignment) {
        if (gdrAccessPolicyService.isAdminSistema(user) || gdrAccessPolicyService.isOrh(user)) {
            return;
        }
        Long linkedPersonId = user.getPerson() != null ? user.getPerson().getId() : null;
        boolean evaluatorScope = linkedPersonId != null
                && Objects.equals(assignment.getEvaluatorPerson().getId(), linkedPersonId);
        if (!evaluatorScope) {
            throw new AccessDeniedException("No tiene permisos para gestionar metas fuera de su alcance operativo.");
        }
    }

    private GoalSummaryResponse mapSummary(GdrGoal goal) {
        return new GoalSummaryResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getExpectedValue(),
                goal.getWeight(),
                goal.getStatus(),
                goal.getAssignment().getId(),
                goal.getAssignment().getEvaluatedPerson().getDisplayName(),
                goal.getIndicator().getId(),
                goal.getIndicator().getName()
        );
    }

    private GoalDetailResponse mapDetail(GdrGoal goal) {
        return new GoalDetailResponse(
                goal.getId(),
                goal.getAssignment().getId(),
                goal.getAssignment().getCycle().getName(),
                goal.getAssignment().getEvaluatorPerson().getDisplayName(),
                goal.getAssignment().getEvaluatedPerson().getDisplayName(),
                goal.getIndicator().getId(),
                goal.getIndicator().getCode(),
                goal.getIndicator().getName(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getExpectedValue(),
                goal.getWeight(),
                goal.getStatus()
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
