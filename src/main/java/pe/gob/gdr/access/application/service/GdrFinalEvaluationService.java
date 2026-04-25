package pe.gob.gdr.access.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.GuardarEvaluacionFinalRequest;
import pe.gob.gdr.access.application.dto.response.ActiveCycleContextResponse;
import pe.gob.gdr.access.application.dto.response.DetalleEvaluacionFinalResponse;
import pe.gob.gdr.access.application.dto.response.DetallePuntajeResponse;
import pe.gob.gdr.access.application.dto.response.ResumenEvaluacionFinalResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.model.GdrScoreDetail;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrScoreDetailRepository;

@Service
public class GdrFinalEvaluationService {

    private static final int LOT3_PROVISIONAL_SCALE = 4;
    private static final RoundingMode LOT3_PROVISIONAL_ROUNDING = RoundingMode.DOWN;

    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final GdrGoalRepository goalRepository;
    private final GdrEvidenceRepository evidenceRepository;
    private final GdrFinalEvaluationRepository finalEvaluationRepository;
    private final GdrScoreDetailRepository scoreDetailRepository;
    private final GdrResultService resultService;
    private final GdrAccessPolicyService accessPolicyService;

    public GdrFinalEvaluationService(
            GdrEvaluationAssignmentRepository assignmentRepository,
            GdrGoalRepository goalRepository,
            GdrEvidenceRepository evidenceRepository,
            GdrFinalEvaluationRepository finalEvaluationRepository,
            GdrScoreDetailRepository scoreDetailRepository,
            GdrResultService resultService,
            GdrAccessPolicyService accessPolicyService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.goalRepository = goalRepository;
        this.evidenceRepository = evidenceRepository;
        this.finalEvaluationRepository = finalEvaluationRepository;
        this.scoreDetailRepository = scoreDetailRepository;
        this.resultService = resultService;
        this.accessPolicyService = accessPolicyService;
    }

    public List<ResumenEvaluacionFinalResponse> listFinalEvaluations() {
        return listFinalEvaluations(null);
    }

    public List<ResumenEvaluacionFinalResponse> listFinalEvaluations(String username) {
        Map<Long, GdrFinalEvaluation> evaluationsByAssignment = finalEvaluationRepository.findActiveFinalEvaluationsForActiveCycle()
                .stream()
                .collect(Collectors.toMap(
                        evaluation -> evaluation.getAssignment().getId(),
                        evaluation -> evaluation,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<GdrEvaluationAssignment> assignments = resolveAccessibleAssignments(username);
        ensureUniqueEvaluatedAssignments(assignments);

        return assignments.stream()
                .map((assignment) -> mapSummary(assignment, evaluationsByAssignment.get(assignment.getId())))
                .toList();
    }

    public DetalleEvaluacionFinalResponse getFinalEvaluation(Long evaluatedId) {
        return getFinalEvaluation(null, evaluatedId);
    }

    public DetalleEvaluacionFinalResponse getFinalEvaluation(String username, Long evaluatedId) {
        if (username != null) {
            validateEvaluatedScope(username, evaluatedId);
        }
        GdrEvaluationAssignment assignment = resolveUniqueAssignmentByEvaluatedId(evaluatedId);
        Optional<GdrFinalEvaluation> evaluation = finalEvaluationRepository.findByAssignmentIdInActiveCycle(assignment.getId());
        List<GdrGoal> goals = goalRepository.findActiveGoalsByAssignmentIdInActiveCycle(assignment.getId());

        if (evaluation.isPresent()) {
            List<GdrScoreDetail> details = scoreDetailRepository.findByFinalEvaluationId(evaluation.get().getId());
            return mapDetail(assignment, evaluation.get(), details);
        }

            List<DetallePuntajeResponse> detailResponses = goals.stream()
                .map((goal) -> new DetallePuntajeResponse(
                        goal.getId(),
                        goal.getTitle(),
                        goal.getIndicator().getName(),
                        goal.getExpectedValue(),
                        goal.getWeight(),
                        null,
                        null,
                        null
                ))
                .toList();

        return new DetalleEvaluacionFinalResponse(
                null,
                assignment.getId(),
                assignment.getEvaluatedPerson().getId(),
                assignment.getEvaluatedPerson().getDisplayName(),
                assignment.getEvaluatorPerson().getDisplayName(),
                assignment.getCycle().getName(),
                null,
                "PENDIENTE",
                null,
                detailResponses
        );
    }

    @Transactional
    public DetalleEvaluacionFinalResponse createFinalEvaluation(GuardarEvaluacionFinalRequest request) {
        GdrEvaluationAssignment assignment = resolveAssignment(request.assignmentId());
        if (finalEvaluationRepository.findByAssignmentIdInActiveCycle(assignment.getId()).isPresent()) {
            throw new DomainException("Ya existe una evaluacion final registrada para la asignacion.");
        }

        GdrFinalEvaluation evaluation = GdrFinalEvaluation.builder()
                .assignment(assignment)
                .status("ACTIVE")
                .build();
        return saveEvaluation(evaluation, request);
    }

    @Transactional
    public DetalleEvaluacionFinalResponse updateFinalEvaluation(Long evaluationId, GuardarEvaluacionFinalRequest request) {
        GdrFinalEvaluation evaluation = finalEvaluationRepository.findByIdInActiveCycle(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la evaluacion final solicitada."));
        if (!evaluation.getAssignment().getId().equals(request.assignmentId())) {
            throw new DomainException("La asignacion de la evaluacion no coincide con la solicitud.");
        }
        return saveEvaluation(evaluation, request);
    }

    private DetalleEvaluacionFinalResponse saveEvaluation(GdrFinalEvaluation evaluation, GuardarEvaluacionFinalRequest request) {
        GdrEvaluationAssignment assignment = resolveAssignment(request.assignmentId());
        List<GdrGoal> goals = goalRepository.findActiveGoalsByAssignmentIdInActiveCycle(assignment.getId());
        if (goals.isEmpty()) {
            throw new DomainException("La asignacion no tiene metas activas para evaluar.");
        }

        List<GdrEvidence> evidences = evidenceRepository.findActiveByGoalAssignmentIdInActiveCycle(assignment.getId());
        if (evidences.isEmpty()) {
            throw new DomainException("La evaluacion final requiere evidencias registradas para la asignacion.");
        }

        Map<Long, GdrGoal> goalsById = goals.stream()
                .collect(java.util.stream.Collectors.toMap(GdrGoal::getId, goal -> goal));
        Map<Long, Long> evidenceCountByGoalId = evidences.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        evidence -> evidence.getGoal().getId(),
                        java.util.stream.Collectors.counting()
                ));

        if (request.details().size() != goalsById.size()) {
            throw new DomainException("La evaluacion final debe considerar todas las metas activas de la asignacion.");
        }

        evaluation.setAssignment(assignment);
        evaluation.setEvaluationComment(normalizeOptional(request.evaluationComment()));
        evaluation.setStatus("ACTIVE");
        GdrFinalEvaluation savedEvaluation = finalEvaluationRepository.save(evaluation);
        final GdrFinalEvaluation persistedEvaluation = savedEvaluation;

        scoreDetailRepository.deleteByFinalEvaluationId(persistedEvaluation.getId());

        List<GdrScoreDetail> details = request.details().stream().map((input) -> {
            GdrGoal goal = goalsById.get(input.goalId());
            if (goal == null) {
                throw new DomainException("La meta indicada no pertenece a la asignacion evaluada.");
            }
            if (!evidenceCountByGoalId.containsKey(goal.getId())) {
                throw new DomainException("Cada meta evaluada debe contar con al menos una evidencia registrada.");
            }

            BigDecimal achievedValue = normalizeProvisionalAchievedValue(input.achievedValue());
            return GdrScoreDetail.builder()
                    .finalEvaluation(persistedEvaluation)
                    .goal(goal)
                    .achievedValue(achievedValue)
                    .scoreValue(calculateProvisionalLot3Score(goal, achievedValue))
                    .detailComment(normalizeOptional(input.detailComment()))
                    .build();
        }).toList();

        BigDecimal consolidatedScore = details.stream()
                .map(GdrScoreDetail::getScoreValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(LOT3_PROVISIONAL_SCALE, LOT3_PROVISIONAL_ROUNDING);

        savedEvaluation.setConsolidatedScore(consolidatedScore);
        savedEvaluation = finalEvaluationRepository.save(savedEvaluation);
        List<GdrScoreDetail> savedDetails = scoreDetailRepository.saveAll(details);
        resultService.syncResult(assignment, savedEvaluation, consolidatedScore);
        return mapDetail(assignment, savedEvaluation, savedDetails);
    }

    /**
     * Provisional Lote 3 scoring policy:
     * keeps the calculation local to this service, avoids categories/tramos,
     * and uses a conservative bounded proportional score over the goal weight.
     * This method must be replaced when the official policy is approved.
     */
    private BigDecimal calculateProvisionalLot3Score(GdrGoal goal, BigDecimal achievedValue) {
        if (goal.getExpectedValue() == null || goal.getExpectedValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("La meta evaluada no tiene un valor esperado valido.");
        }

        BigDecimal boundedAchievedValue = achievedValue.max(BigDecimal.ZERO);
        BigDecimal proportionalScore = boundedAchievedValue
                .multiply(goal.getWeight())
                .divide(goal.getExpectedValue(), LOT3_PROVISIONAL_SCALE, LOT3_PROVISIONAL_ROUNDING);

        return proportionalScore
                .min(goal.getWeight())
                .setScale(LOT3_PROVISIONAL_SCALE, LOT3_PROVISIONAL_ROUNDING);
    }

    private GdrEvaluationAssignment resolveAssignment(Long assignmentId) {
        return assignmentRepository.findActiveByIdInActiveCycle(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro una asignacion activa para la evaluacion."));
    }

    private GdrEvaluationAssignment resolveUniqueAssignmentByEvaluatedId(Long evaluatedId) {
        List<GdrEvaluationAssignment> assignments = assignmentRepository.findActiveByEvaluatedIdInActiveCycle(evaluatedId);
        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("No se encontro una asignacion activa para el evaluado.");
        }
        if (assignments.size() > 1) {
            throw new DomainException(
                    "El Lote 3 requiere una unica asignacion activa por evaluado para registrar la evaluacion final."
            );
        }
        return assignments.getFirst();
    }

    private ResumenEvaluacionFinalResponse mapSummary(GdrEvaluationAssignment assignment, GdrFinalEvaluation evaluation) {
        return new ResumenEvaluacionFinalResponse(
                assignment.getId(),
                assignment.getEvaluatedPerson().getId(),
                assignment.getEvaluatedPerson().getDisplayName(),
                assignment.getEvaluatorPerson().getDisplayName(),
                assignment.getCycle().getName(),
                evaluation != null ? evaluation.getId() : null,
                evaluation != null ? evaluation.getConsolidatedScore() : null,
                evaluation != null ? evaluation.getStatus() : "PENDIENTE"
        );
    }

    private DetalleEvaluacionFinalResponse mapDetail(
            GdrEvaluationAssignment assignment,
            GdrFinalEvaluation evaluation,
            List<GdrScoreDetail> details
    ) {
        List<DetallePuntajeResponse> detailResponses = details.stream()
                .map((detail) -> new DetallePuntajeResponse(
                        detail.getGoal().getId(),
                        detail.getGoal().getTitle(),
                        detail.getGoal().getIndicator().getName(),
                        detail.getGoal().getExpectedValue(),
                        detail.getGoal().getWeight(),
                        detail.getAchievedValue(),
                        detail.getScoreValue(),
                        detail.getDetailComment()
                ))
                .toList();

        return new DetalleEvaluacionFinalResponse(
                evaluation.getId(),
                assignment.getId(),
                assignment.getEvaluatedPerson().getId(),
                assignment.getEvaluatedPerson().getDisplayName(),
                assignment.getEvaluatorPerson().getDisplayName(),
                assignment.getCycle().getName(),
                evaluation.getConsolidatedScore(),
                evaluation.getStatus(),
                evaluation.getEvaluationComment(),
                detailResponses
        );
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal normalizeProvisionalAchievedValue(BigDecimal achievedValue) {
        return achievedValue.setScale(LOT3_PROVISIONAL_SCALE, LOT3_PROVISIONAL_ROUNDING);
    }

    private void ensureUniqueEvaluatedAssignments(List<GdrEvaluationAssignment> assignments) {
        Map<Long, Long> assignmentsByEvaluated = assignments.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        assignment -> assignment.getEvaluatedPerson().getId(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));

        boolean hasDuplicates = assignmentsByEvaluated.values().stream().anyMatch((count) -> count > 1);
        if (hasDuplicates) {
            throw new DomainException(
                    "El Lote 3 requiere una unica asignacion activa por evaluado para consultar la evaluacion final."
            );
        }
    }

    private List<GdrEvaluationAssignment> resolveAccessibleAssignments(String username) {
        if (username == null || username.isBlank()) {
            return assignmentRepository.findActiveAssignmentsForActiveCycle();
        }

        User user = accessPolicyService.loadUserWithContext(username);
        ActiveCycleContextResponse context = accessPolicyService.resolveContext(user);

        if (accessPolicyService.isAdminSistema(user) || accessPolicyService.isOrh(user)) {
            return assignmentRepository.findActiveAssignmentsForActiveCycle();
        }

        if (!context.hrPersonLinked() || !context.cycleActive() || context.personId() == null) {
            return List.of();
        }

        Long personId = context.personId();
        List<GdrEvaluationAssignment> assignments = assignmentRepository.findActiveByPersonIdInActiveCycle(personId);
        return assignments.stream()
                .filter(assignment -> canViewAssignmentForActor(context.functionalActor(), personId, assignment))
                .collect(Collectors.toMap(
                        GdrEvaluationAssignment::getId,
                        assignment -> assignment,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    private void validateEvaluatedScope(String username, Long evaluatedId) {
        User user = accessPolicyService.loadUserWithContext(username);
        ActiveCycleContextResponse context = accessPolicyService.resolveContext(user);
        if (!accessPolicyService.isAdminSistema(user)
                && !accessPolicyService.isOrh(user)
                && !isEvaluatedAllowedForContext(context, evaluatedId, user)) {
            throw new DomainException("No tiene acceso al evaluado solicitado dentro de su alcance operativo.");
        }
    }

    private boolean isEvaluatedAllowedForContext(
            ActiveCycleContextResponse context,
            Long evaluatedId,
            User user
    ) {
        if (!context.hrPersonLinked() || !context.cycleActive() || context.personId() == null) {
            return false;
        }
        if (accessPolicyService.isOrh(user)) {
            return true;
        }
        Long personId = context.personId();
        return switch (context.functionalActor()) {
            case GdrAccessPolicyService.ACTOR_EVALUADOR ->
                    assignmentRepository.findActiveByPersonIdInActiveCycle(personId).stream()
                            .anyMatch(assignment ->
                                    Objects.equals(assignment.getEvaluatorPerson().getId(), personId)
                                            && Objects.equals(assignment.getEvaluatedPerson().getId(), evaluatedId));
            case GdrAccessPolicyService.ACTOR_EVALUADO -> Objects.equals(personId, evaluatedId);
            case GdrAccessPolicyService.ACTOR_EVALUADOR_Y_EVALUADO ->
                    Objects.equals(personId, evaluatedId)
                            || assignmentRepository.findActiveByPersonIdInActiveCycle(personId).stream()
                            .anyMatch(assignment ->
                                    Objects.equals(assignment.getEvaluatorPerson().getId(), personId)
                                            && Objects.equals(assignment.getEvaluatedPerson().getId(), evaluatedId));
            default -> false;
        };
    }

    private boolean canViewAssignmentForActor(
            String functionalActor,
            Long personId,
            GdrEvaluationAssignment assignment
    ) {
        return switch (functionalActor) {
            case GdrAccessPolicyService.ACTOR_EVALUADOR ->
                    Objects.equals(assignment.getEvaluatorPerson().getId(), personId);
            case GdrAccessPolicyService.ACTOR_EVALUADO ->
                    Objects.equals(assignment.getEvaluatedPerson().getId(), personId);
            case GdrAccessPolicyService.ACTOR_EVALUADOR_Y_EVALUADO ->
                    Objects.equals(assignment.getEvaluatorPerson().getId(), personId)
                            || Objects.equals(assignment.getEvaluatedPerson().getId(), personId);
            default -> false;
        };
    }
}
