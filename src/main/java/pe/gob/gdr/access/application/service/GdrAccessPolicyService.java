package pe.gob.gdr.access.application.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.application.dto.response.ActiveCycleContextResponse;
import pe.gob.gdr.access.application.dto.response.FeatureAccessResponse;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.DocSignatureRequest;
import pe.gob.gdr.access.domain.model.DocSignedFile;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrEvidence;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrGoal;
import pe.gob.gdr.access.domain.model.GdrImprovementOpportunity;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.model.UserContextAssignment;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.DocSignatureRequestRepository;
import pe.gob.gdr.access.domain.repository.DocSignedFileRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrEvidenceRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrImprovementOpportunityRepository;
import pe.gob.gdr.access.domain.repository.UserContextAssignmentRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

@Service("gdrAccessPolicyService")
public class GdrAccessPolicyService {

    public static final String ACTOR_ORH = "ORH";
    public static final String ACTOR_JUNTA_DIRECTIVOS = "JUNTA_DIRECTIVOS";
    public static final String ACTOR_EVALUADOR = "EVALUADOR";
    public static final String ACTOR_EVALUADO = "EVALUADO";
    public static final String ACTOR_EVALUADOR_Y_EVALUADO = "EVALUADOR_Y_EVALUADO";
    public static final String ACTOR_CONSULTA = "CONSULTA";
    public static final String ACTOR_SIN_ROL_FUNCIONAL = "SIN_ROL_FUNCIONAL_GDR";
    public static final String SCOPE_ADMIN = "ADMIN_TECNICO";
    public static final String SCOPE_INSTITUTIONAL = "INSTITUCIONAL_GDR";
    public static final String SCOPE_EVALUATOR = "ASIGNACIONES_A_CARGO";
    public static final String SCOPE_SELF = "PROPIO";
    public static final String SCOPE_MIXED = "MIXTO";
    public static final String SCOPE_CONSULTA = "CONSULTA_RESTRINGIDA";
    public static final String SCOPE_NO_HR_IDENTITY = "SIN_IDENTIDAD_LABORAL";
    public static final String SCOPE_NO_CYCLE = "SIN_CICLO_ACTIVO";
    public static final String SCOPE_NO_GDR_ACCESS = "SIN_ACCESO_GDR";

    private final UserRepository userRepository;
    private final ActiveCycleRepository activeCycleRepository;
    private final UserContextAssignmentRepository userContextAssignmentRepository;
    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final DocSignedFileRepository docSignedFileRepository;
    private final DocSignatureRequestRepository docSignatureRequestRepository;
    private final GdrImprovementOpportunityRepository improvementOpportunityRepository;
    private final GdrGoalRepository goalRepository;
    private final GdrEvidenceRepository evidenceRepository;
    private final GdrFinalEvaluationRepository finalEvaluationRepository;

    public GdrAccessPolicyService(
            UserRepository userRepository,
            ActiveCycleRepository activeCycleRepository,
            UserContextAssignmentRepository userContextAssignmentRepository,
            GdrEvaluationAssignmentRepository assignmentRepository,
            DocSignedFileRepository docSignedFileRepository,
            DocSignatureRequestRepository docSignatureRequestRepository,
            GdrImprovementOpportunityRepository improvementOpportunityRepository,
            GdrGoalRepository goalRepository,
            GdrEvidenceRepository evidenceRepository,
            GdrFinalEvaluationRepository finalEvaluationRepository
    ) {
        this.userRepository = userRepository;
        this.activeCycleRepository = activeCycleRepository;
        this.userContextAssignmentRepository = userContextAssignmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.docSignedFileRepository = docSignedFileRepository;
        this.docSignatureRequestRepository = docSignatureRequestRepository;
        this.improvementOpportunityRepository = improvementOpportunityRepository;
        this.goalRepository = goalRepository;
        this.evidenceRepository = evidenceRepository;
        this.finalEvaluationRepository = finalEvaluationRepository;
    }

    public ActiveCycleContextResponse resolveContext(User user) {
        Optional<ActiveCycle> activeCycle = activeCycleRepository.findActiveCycle();
        Optional<UserContextAssignment> assignedContext = activeCycle
                .flatMap(cycle -> userContextAssignmentRepository.findActiveByUsernameAndCycleId(user.getUsername(), cycle.getId()));
        HrPerson person = user.getPerson();
        boolean hrPersonLinked = person != null;
        String functionalActor = resolveFunctionalActor(user, activeCycle.orElse(null), person);
        String operationalScope = resolveOperationalScope(user, activeCycle.isPresent(), hrPersonLinked, functionalActor);
        boolean gdrOperational = resolveGdrOperational(user, activeCycle.isPresent(), hrPersonLinked, functionalActor);

        return new ActiveCycleContextResponse(
                activeCycle.map(ActiveCycle::getId).orElse(null),
                activeCycle.map(ActiveCycle::getCode).orElse(null),
                activeCycle.map(ActiveCycle::getName).orElse(null),
                assignedContext.map(UserContextAssignment::getContextCode).orElse(null),
                assignedContext.map(UserContextAssignment::getContextName).orElse(null),
                activeCycle.isPresent(),
                assignedContext.isPresent(),
                hrPersonLinked,
                hrPersonLinked ? person.getId() : null,
                hrPersonLinked ? person.getDocumentNumber() : null,
                hrPersonLinked ? person.getDisplayName() : null,
                hrPersonLinked ? person.getOrgUnit().getId() : null,
                hrPersonLinked ? person.getOrgUnit().getCode() : null,
                hrPersonLinked ? person.getOrgUnit().getName() : null,
                functionalActor,
                operationalScope,
                gdrOperational
        );
    }

    public FeatureAccessResponse buildFeatureAccess(User user, ActiveCycleContextResponse context) {
        boolean admin = isAdminSistema(user);
        boolean institutionalGdr = isOrh(user) && context.hrPersonLinked() && context.cycleActive();
        boolean evaluatorScope = context.hrPersonLinked()
                && context.cycleActive()
                && (ACTOR_EVALUADOR.equals(context.functionalActor()) || ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor()));
        boolean evaluatedScope = context.hrPersonLinked()
                && context.cycleActive()
                && (ACTOR_EVALUADO.equals(context.functionalActor()) || ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor()));
        boolean ownAssignments = evaluatorScope || evaluatedScope;
        boolean hrLinkedOrh = isOrh(user) && context.hrPersonLinked();
        boolean hrLinkedGdrUser = isGdrUsuario(user) && context.hrPersonLinked();
        boolean orhReception = isOrh(user)
                && context.hrPersonLinked()
                && context.cycleActive()
                && ACTOR_ORH.equals(context.functionalActor());
        boolean userManagement = hasAnyTechnicalRole(user, Set.of("ADMIN_SISTEMA"));
        boolean orhDistinguido = isOrh(user)
                && context.hrPersonLinked()
                && context.cycleActive()
                && ACTOR_ORH.equals(context.functionalActor());
        boolean juntaDistinguido = isJuntaDirectivos(user)
                && context.hrPersonLinked()
                && context.cycleActive()
                && ACTOR_JUNTA_DIRECTIVOS.equals(context.functionalActor());
        boolean viewDistinguidoCandidates = admin || orhDistinguido || juntaDistinguido;
        boolean manageDistinguidoRequisites = admin || orhDistinguido;
        boolean assignDistinguido = admin || juntaDistinguido;

        return new FeatureAccessResponse(
                true,
                admin || institutionalGdr || ownAssignments,
                admin || institutionalGdr || ownAssignments,
                admin || institutionalGdr || ownAssignments,
                admin || institutionalGdr || ownAssignments,
                admin || institutionalGdr || ownAssignments,
                admin || institutionalGdr || evaluatorScope,
                admin || institutionalGdr || ownAssignments,
                admin || evaluatedScope,
                admin || institutionalGdr || evaluatorScope,
                admin || institutionalGdr || ownAssignments,
                admin || evaluatorScope,
                admin || institutionalGdr || ownAssignments,
                admin || institutionalGdr || ownAssignments,
                admin || institutionalGdr || evaluatorScope,
                admin || institutionalGdr || evaluatorScope,
                admin || institutionalGdr || evaluatorScope,
                admin || institutionalGdr || evaluatorScope,
                admin || institutionalGdr || ownAssignments,
                admin || institutionalGdr || evaluatorScope,
                admin || institutionalGdr || evaluatorScope,
                admin || institutionalGdr,
                admin || hrLinkedOrh || hrLinkedGdrUser,
                admin || hrLinkedOrh || hrLinkedGdrUser,
                orhReception,
                userManagement,
                viewDistinguidoCandidates,
                manageDistinguidoRequisites,
                assignDistinguido
        );
    }

    public boolean canViewAssignments(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewAssignments();
    }

    public boolean canViewCatalogs(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewCatalogs();
    }

    public boolean canViewIndicators(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewIndicators();
    }

    public boolean canManageIndicators(Authentication authentication) {
        return resolveFeatureAccess(authentication).canManageIndicators();
    }

    public boolean canViewGoals(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewGoals();
    }

    public boolean canManageGoals(Authentication authentication) {
        return resolveFeatureAccess(authentication).canManageGoals();
    }

    public boolean canViewEvidences(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewEvidences();
    }

    public boolean canManageEvidences(Authentication authentication) {
        return resolveFeatureAccess(authentication).canManageEvidences();
    }

    public boolean canReviewEvidences(Authentication authentication) {
        return resolveFeatureAccess(authentication).canReviewEvidences();
    }

    public boolean canViewFinalEvaluations(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewFinalEvaluations();
    }

    public boolean canManageFinalEvaluations(Authentication authentication) {
        return resolveFeatureAccess(authentication).canManageFinalEvaluations();
    }

    public boolean canViewResults(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewResults();
    }

    public boolean canViewDocuments(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewDocuments();
    }

    public boolean canPrepareDocuments(Authentication authentication) {
        return resolveFeatureAccess(authentication).canPrepareDocuments();
    }

    public boolean canStartSignatureFlow(Authentication authentication) {
        return resolveFeatureAccess(authentication).canStartSignatureFlow();
    }

    public boolean canRegisterSignatureReturn(Authentication authentication) {
        return resolveFeatureAccess(authentication).canRegisterSignatureReturn();
    }

    public boolean canRegisterSignedDocuments(Authentication authentication) {
        return resolveFeatureAccess(authentication).canRegisterSignedDocuments();
    }

    public boolean canViewImprovements(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewImprovements();
    }

    public boolean canManageImprovements(Authentication authentication) {
        return resolveFeatureAccess(authentication).canManageImprovements();
    }

    public boolean canFollowupImprovements(Authentication authentication) {
        return resolveFeatureAccess(authentication).canFollowupImprovements();
    }

    public boolean canViewReports(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewReports();
    }

    public boolean canViewNotifications(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewNotifications();
    }

    public boolean canViewConsents(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewConsents();
    }

    public boolean canViewOrhReception(Authentication authentication) {
        return resolveAccess(authentication)
                .map(access -> canViewOrhReception(access.user(), access.context()))
                .orElse(false);
    }

    public boolean canManageUsers(Authentication authentication) {
        return resolveAccess(authentication)
                .map(access -> hasAnyTechnicalRole(access.user(), Set.of("ADMIN_SISTEMA")))
                .orElse(false);
    }

    public boolean canViewDistinguidoCandidates(Authentication authentication) {
        return resolveFeatureAccess(authentication).canViewDistinguidoCandidates();
    }

    public boolean canManageDistinguidoRequisites(Authentication authentication, Long assignmentId) {
        return resolveAccess(authentication)
                .map(access -> canManageDistinguidoRequisitesResource(access.user(), access.context(), assignmentId))
                .orElse(false);
    }

    public boolean canAssignDistinguido(Authentication authentication) {
        return resolveFeatureAccess(authentication).canAssignDistinguido();
    }

    private boolean canManageDistinguidoRequisitesResource(
            User user,
            ActiveCycleContextResponse context,
            Long assignmentId
    ) {
        if (assignmentId == null) {
            return false;
        }
        if (isAdminSistema(user)) {
            return true;
        }
        if (!isOrh(user)
                || !context.hrPersonLinked()
                || !context.cycleActive()
                || !ACTOR_ORH.equals(context.functionalActor())) {
            return false;
        }
        return assignmentRepository.findActiveByIdInActiveCycle(assignmentId).isPresent();
    }

    public boolean canSetAssignmentSegment(Authentication authentication, Long assignmentId) {
        return resolveAccess(authentication)
                .flatMap(access -> assignmentRepository.findActiveByIdInActiveCycle(assignmentId)
                        .map(assignment -> canSetAssignmentSegmentResource(access.user(), access.context(), assignment)))
                .orElse(false);
    }

    private boolean canSetAssignmentSegmentResource(
            User user,
            ActiveCycleContextResponse context,
            GdrEvaluationAssignment assignment
    ) {
        if (user == null || assignment == null) {
            return false;
        }
        if (isAdminSistema(user) || isOrh(user)) {
            return true;
        }
        if (context == null
                || !context.hrPersonLinked()
                || !context.cycleActive()
                || context.personId() == null) {
            return false;
        }
        if (!isGdrUsuario(user)) {
            return false;
        }
        boolean isEvaluator = ACTOR_EVALUADOR.equals(context.functionalActor())
                || ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor());
        return isEvaluator
                && Objects.equals(assignment.getEvaluatorPerson().getId(), context.personId());
    }

    public boolean canViewOrhReception(User user, ActiveCycleContextResponse context) {
        if (user == null || context == null) {
            return false;
        }
        return isOrh(user)
                && context.hrPersonLinked()
                && context.cycleActive()
                && ACTOR_ORH.equals(context.functionalActor());
    }

    public boolean canAccessDocumentsForEvaluated(Authentication authentication, Long evaluatedId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewDocuments())
                .map(access -> canAccessEvaluatedScope(access.user(), access.context(), evaluatedId))
                .orElse(false);
    }

    public boolean canPrepareDocumentsForEvaluated(Authentication authentication, Long evaluatedId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canPrepareDocuments())
                .map(access -> canManageEvaluatedFlow(access.user(), access.context(), evaluatedId))
                .orElse(false);
    }

    public boolean canRegisterSignedDocumentsForEvaluated(Authentication authentication, Long evaluatedId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canRegisterSignedDocuments())
                .map(access -> canManageEvaluatedFlow(access.user(), access.context(), evaluatedId))
                .orElse(false);
    }

    public boolean canAccessDocumentById(Authentication authentication, Long documentId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewDocuments())
                .flatMap(access -> docSignedFileRepository.findActiveById(documentId)
                        .map(document -> canAccessDocumentResource(access.user(), access.context(), document)))
                .orElse(false);
    }

    public boolean canAccessSignatureRequest(Authentication authentication, Long requestId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewDocuments())
                .flatMap(access -> docSignatureRequestRepository.findActiveById(requestId)
                        .map(request -> canAccessSignatureResource(access.user(), access.context(), request)))
                .orElse(false);
    }

    public boolean canStartSignatureRequest(Authentication authentication, Long requestId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canStartSignatureFlow())
                .flatMap(access -> docSignatureRequestRepository.findActiveById(requestId)
                        .map(request -> canManageSignatureResource(access.user(), access.context(), request)))
                .orElse(false);
    }

    public boolean canRegisterSignatureReturn(Authentication authentication, Long requestId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canRegisterSignatureReturn())
                .flatMap(access -> docSignatureRequestRepository.findActiveById(requestId)
                        .map(request -> canManageSignatureResource(access.user(), access.context(), request)))
                .orElse(false);
    }

    public boolean canAccessImprovementsForEvaluated(Authentication authentication, Long evaluatedId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewImprovements())
                .map(access -> canAccessEvaluatedScope(access.user(), access.context(), evaluatedId))
                .orElse(false);
    }

    public boolean canCreateImprovementForEvaluated(Authentication authentication, Long evaluatedId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canManageImprovements())
                .map(access -> canManageEvaluatedFlow(access.user(), access.context(), evaluatedId))
                .orElse(false);
    }

    public boolean canAccessEvaluatedScope(Authentication authentication, Long evaluatedId) {
        return resolveAccess(authentication)
                .map(access -> canAccessEvaluatedScope(access.user(), access.context(), evaluatedId))
                .orElse(false);
    }

    public boolean canAccessImprovementById(Authentication authentication, Long opportunityId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewImprovements())
                .flatMap(access -> improvementOpportunityRepository.findActiveById(opportunityId)
                        .map(opportunity -> canAccessImprovementResource(access.user(), access.context(), opportunity)))
                .orElse(false);
    }

    public boolean canManageImprovementById(Authentication authentication, Long opportunityId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canManageImprovements())
                .flatMap(access -> improvementOpportunityRepository.findActiveById(opportunityId)
                        .map(opportunity -> canManageImprovementResource(access.user(), access.context(), opportunity)))
                .orElse(false);
    }

    public boolean canFollowupImprovementById(Authentication authentication, Long opportunityId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canFollowupImprovements())
                .flatMap(access -> improvementOpportunityRepository.findActiveById(opportunityId)
                        .map(opportunity -> canManageImprovementResource(access.user(), access.context(), opportunity)))
                .orElse(false);
    }

    public boolean canAccessGoalEvidence(Authentication authentication, Long goalId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewEvidences())
                .flatMap(access -> goalRepository.findActiveByIdInActiveCycle(goalId)
                        .map(goal -> canAccessGoalResource(access.user(), access.context(), goal)))
                .orElse(false);
    }

    public boolean canRateGoalAchievement(Authentication authentication, Long goalId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canReviewEvidences())
                .flatMap(access -> goalRepository.findActiveByIdInActiveCycle(goalId)
                        .map(goal -> canReviewGoalAchievementResource(access.user(), access.context(), goal)))
                .orElse(false);
    }

    public boolean canRateGoalAchievement(User user, GdrGoal goal) {
        if (user == null || goal == null) {
            return false;
        }
        ActiveCycleContextResponse context = resolveContext(user);
        if (!buildFeatureAccess(user, context).canReviewEvidences()) {
            return false;
        }
        return canReviewGoalAchievementResource(user, context, goal);
    }

    public boolean canCreateGoalChangeRequest(Authentication authentication, Long goalId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewGoals())
                .flatMap(access -> goalRepository.findActiveByIdInActiveCycle(goalId)
                        .map(goal -> canCreateGoalChangeRequest(access.user(), access.context(), goal)))
                .orElse(false);
    }

    public boolean canCreateGoalChangeRequest(User user, ActiveCycleContextResponse context, GdrGoal goal) {
        if (user == null || context == null || goal == null) {
            return false;
        }
        if (!buildFeatureAccess(user, context).canViewGoals()) {
            return false;
        }
        if (!context.hrPersonLinked() || !context.cycleActive() || context.personId() == null) {
            return false;
        }
        if (!isGdrUsuario(user)) {
            return false;
        }
        if (!ACTOR_EVALUADOR.equals(context.functionalActor())
                && !ACTOR_EVALUADO.equals(context.functionalActor())
                && !ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor())) {
            return false;
        }
        return canAccessGoalResource(user, context, goal);
    }

    public boolean canSubmitGoalToOrh(Authentication authentication, Long goalId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewGoals())
                .flatMap(access -> goalRepository.findActiveByIdInActiveCycle(goalId)
                        .map(goal -> canSubmitGoalToOrh(access.user(), access.context(), goal)))
                .orElse(false);
    }

    public boolean canSubmitGoalToOrh(User user, ActiveCycleContextResponse context, GdrGoal goal) {
        if (user == null || context == null || goal == null) {
            return false;
        }
        if (!buildFeatureAccess(user, context).canViewGoals()) {
            return false;
        }
        if (!context.hrPersonLinked() || !context.cycleActive() || context.personId() == null) {
            return false;
        }
        if (!isGdrUsuario(user)) {
            return false;
        }
        if (!ACTOR_EVALUADOR.equals(context.functionalActor())
                && !ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor())) {
            return false;
        }
        return Objects.equals(goal.getAssignment().getEvaluatorPerson().getId(), context.personId());
    }

    public boolean canAccessEvidenceById(Authentication authentication, Long evidenceId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewEvidences())
                .flatMap(access -> evidenceRepository.findActiveByIdInActiveCycle(evidenceId)
                        .map(evidence -> canAccessEvidenceResource(access.user(), access.context(), evidence)))
                .orElse(false);
    }

    public boolean canReviewEvidence(Authentication authentication, Long evidenceId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canReviewEvidences())
                .flatMap(access -> evidenceRepository.findActiveByIdInActiveCycle(evidenceId)
                        .map(evidence -> canReviewEvidenceResource(access.user(), access.context(), evidence)))
                .orElse(false);
    }

    public boolean canManageEvidenceCreation(Authentication authentication, Long goalId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canManageEvidences())
                .flatMap(access -> goalRepository.findActiveByIdInActiveCycle(goalId)
                        .map(goal -> canManageOwnEvidenceResource(access.user(), access.context(), goal)))
                .orElse(false);
    }

    public boolean canManageEvidenceUpdate(Authentication authentication, Long evidenceId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canManageEvidences())
                .flatMap(access -> evidenceRepository.findActiveByIdInActiveCycle(evidenceId)
                        .map(evidence -> canManageOwnEvidenceResource(access.user(), access.context(), evidence)))
                .orElse(false);
    }

    public boolean canViewFinalEvaluationByEvaluated(Authentication authentication, Long evaluatedId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewFinalEvaluations())
                .map(access -> canAccessEvaluatedScope(access.user(), access.context(), evaluatedId))
                .orElse(false);
    }

    public boolean canManageFinalEvaluationForAssignment(Authentication authentication, Long assignmentId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canManageFinalEvaluations())
                .flatMap(access -> assignmentRepository.findActiveByIdInActiveCycle(assignmentId)
                        .map(assignment -> canManageFinalEvaluationResource(access.user(), access.context(), assignment)))
                .orElse(false);
    }

    public boolean canManageFinalEvaluationById(Authentication authentication, Long evaluationId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canManageFinalEvaluations())
                .flatMap(access -> finalEvaluationRepository.findByIdInActiveCycle(evaluationId)
                        .map(evaluation -> canManageFinalEvaluationResource(
                                access.user(),
                                access.context(),
                                evaluation.getAssignment()
                        )))
                .orElse(false);
    }

    public boolean canViewResultByEvaluated(Authentication authentication, Long evaluatedId) {
        return resolveAccess(authentication)
                .filter(access -> access.featureAccess().canViewResults())
                .map(access -> canAccessEvaluatedScope(access.user(), access.context(), evaluatedId))
                .orElse(false);
    }

    public boolean isAdminSistema(User user) {
        return hasAnyTechnicalRole(user, Set.of("ADMIN", "ADMIN_SISTEMA"));
    }

    public boolean isOrh(User user) {
        return hasAnyTechnicalRole(user, Set.of("GDR_ORH"));
    }

    public boolean isGdrUsuario(User user) {
        return hasAnyTechnicalRole(user, Set.of("GDR_USUARIO"));
    }

    public boolean isGdrConsulta(User user) {
        return hasAnyTechnicalRole(user, Set.of("GDR_CONSULTA"));
    }

    public boolean isJuntaDirectivos(User user) {
        return hasAnyTechnicalRole(user, Set.of("GDR_JUNTA_DIRECTIVOS"));
    }

    public User loadUserWithContext(String username) {
        return userRepository.findByUsernameWithPerson(username)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el usuario autenticado."));
    }

    public boolean canOperateAssignments(User user) {
        return buildFeatureAccess(user, resolveContext(user)).canViewAssignments();
    }

    public boolean canOperateGoals(User user) {
        return buildFeatureAccess(user, resolveContext(user)).canViewGoals();
    }

    public boolean canManageGoals(User user) {
        return buildFeatureAccess(user, resolveContext(user)).canManageGoals();
    }

    public boolean canManageIndicators(User user) {
        return buildFeatureAccess(user, resolveContext(user)).canManageIndicators();
    }

    private FeatureAccessResponse resolveFeatureAccess(Authentication authentication) {
        return resolveAccess(authentication)
                .map(ResolvedAccess::featureAccess)
                .orElseGet(() -> new FeatureAccessResponse(
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                ));
    }

    private Optional<ResolvedAccess> resolveAccess(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }
        User user = loadUserWithContext(authentication.getName());
        ActiveCycleContextResponse context = resolveContext(user);
        return Optional.of(new ResolvedAccess(user, context, buildFeatureAccess(user, context)));
    }

    private String resolveFunctionalActor(User user, ActiveCycle activeCycle, HrPerson person) {
        if (isAdminSistema(user)) {
            return ACTOR_SIN_ROL_FUNCIONAL;
        }
        if (person == null) {
            return ACTOR_SIN_ROL_FUNCIONAL;
        }
        if (isOrh(user)) {
            return ACTOR_ORH;
        }
        if (isJuntaDirectivos(user)) {
            return ACTOR_JUNTA_DIRECTIVOS;
        }
        if (isGdrConsulta(user)) {
            return ACTOR_CONSULTA;
        }
        if (activeCycle == null) {
            return ACTOR_SIN_ROL_FUNCIONAL;
        }

        List<GdrEvaluationAssignment> assignments = assignmentRepository.findActiveByPersonIdInActiveCycle(person.getId());
        boolean evaluator = assignments.stream().anyMatch(item -> Objects.equals(item.getEvaluatorPerson().getId(), person.getId()));
        boolean evaluated = assignments.stream().anyMatch(item -> Objects.equals(item.getEvaluatedPerson().getId(), person.getId()));

        if (evaluator && evaluated) {
            return ACTOR_EVALUADOR_Y_EVALUADO;
        }
        if (evaluator) {
            return ACTOR_EVALUADOR;
        }
        if (evaluated) {
            return ACTOR_EVALUADO;
        }
        return ACTOR_SIN_ROL_FUNCIONAL;
    }

    private String resolveOperationalScope(User user, boolean cycleActive, boolean hrPersonLinked, String functionalActor) {
        if (isAdminSistema(user)) {
            return SCOPE_ADMIN;
        }
        if (requiresHrIdentity(user) && !hrPersonLinked) {
            return SCOPE_NO_HR_IDENTITY;
        }
        if (!cycleActive) {
            return SCOPE_NO_CYCLE;
        }
        if (ACTOR_ORH.equals(functionalActor)) {
            return SCOPE_INSTITUTIONAL;
        }
        if (ACTOR_JUNTA_DIRECTIVOS.equals(functionalActor)) {
            return SCOPE_INSTITUTIONAL;
        }
        if (ACTOR_EVALUADOR_Y_EVALUADO.equals(functionalActor)) {
            return SCOPE_MIXED;
        }
        if (ACTOR_EVALUADOR.equals(functionalActor)) {
            return SCOPE_EVALUATOR;
        }
        if (ACTOR_EVALUADO.equals(functionalActor)) {
            return SCOPE_SELF;
        }
        if (ACTOR_CONSULTA.equals(functionalActor)) {
            return SCOPE_CONSULTA;
        }
        return SCOPE_NO_GDR_ACCESS;
    }

    private boolean resolveGdrOperational(User user, boolean cycleActive, boolean hrPersonLinked, String functionalActor) {
        if (isAdminSistema(user)) {
            return true;
        }
        if (requiresHrIdentity(user) && !hrPersonLinked) {
            return false;
        }
        if (!cycleActive) {
            return false;
        }
        return ACTOR_ORH.equals(functionalActor)
                || ACTOR_JUNTA_DIRECTIVOS.equals(functionalActor)
                || ACTOR_EVALUADOR.equals(functionalActor)
                || ACTOR_EVALUADO.equals(functionalActor)
                || ACTOR_EVALUADOR_Y_EVALUADO.equals(functionalActor);
    }

    private boolean requiresHrIdentity(User user) {
        return isOrh(user) || isJuntaDirectivos(user) || isGdrUsuario(user) || isGdrConsulta(user);
    }

    private boolean hasAnyTechnicalRole(User user, Set<String> expectedCodes) {
        return user.getUserRoles().stream()
                .filter(item -> item.getRole() != null)
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getRole().getStatus()))
                .map(item -> item.getRole().getCode())
                .anyMatch(expectedCodes::contains);
    }

    private boolean canAccessEvaluatedScope(User user, ActiveCycleContextResponse context, Long evaluatedId) {
        if (evaluatedId == null) {
            return false;
        }
        if (isAdminSistema(user)) {
            return true;
        }
        if (!context.hrPersonLinked() || !context.cycleActive() || context.personId() == null) {
            return false;
        }
        if (isOrh(user)) {
            return true;
        }
        if (!isGdrUsuario(user)) {
            return false;
        }

        Long personId = context.personId();
        if ((ACTOR_EVALUADO.equals(context.functionalActor()) || ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor()))
                && Objects.equals(personId, evaluatedId)) {
            return true;
        }

        if (ACTOR_EVALUADOR.equals(context.functionalActor()) || ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor())) {
            return assignmentRepository.findActiveByPersonIdInActiveCycle(personId).stream()
                    .anyMatch(assignment ->
                            Objects.equals(assignment.getEvaluatorPerson().getId(), personId)
                                    && Objects.equals(assignment.getEvaluatedPerson().getId(), evaluatedId)
                    );
        }
        return false;
    }

    private boolean canManageEvaluatedFlow(User user, ActiveCycleContextResponse context, Long evaluatedId) {
        if (evaluatedId == null) {
            return false;
        }
        if (isAdminSistema(user)) {
            return true;
        }
        if (!context.hrPersonLinked() || !context.cycleActive() || context.personId() == null) {
            return false;
        }
        if (isOrh(user)) {
            return true;
        }
        if (!isGdrUsuario(user)) {
            return false;
        }

        Long personId = context.personId();
        return (ACTOR_EVALUADOR.equals(context.functionalActor()) || ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor()))
                && assignmentRepository.findActiveByPersonIdInActiveCycle(personId).stream()
                .anyMatch(assignment ->
                        Objects.equals(assignment.getEvaluatorPerson().getId(), personId)
                                && Objects.equals(assignment.getEvaluatedPerson().getId(), evaluatedId)
                );
    }

    private boolean canManageOwnEvaluatedFlow(User user, ActiveCycleContextResponse context, Long evaluatedId) {
        if (evaluatedId == null) {
            return false;
        }
        if (isAdminSistema(user)) {
            return true;
        }
        if (!context.hrPersonLinked() || !context.cycleActive() || context.personId() == null) {
            return false;
        }
        if (!isGdrUsuario(user)) {
            return false;
        }
        return (ACTOR_EVALUADO.equals(context.functionalActor()) || ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor()))
                && Objects.equals(context.personId(), evaluatedId);
    }

    private boolean canAccessDocumentResource(User user, ActiveCycleContextResponse context, DocSignedFile document) {
        Long evaluatedId = document.getResult().getAssignment().getEvaluatedPerson().getId();
        return canAccessEvaluatedScope(user, context, evaluatedId);
    }

    private boolean canAccessSignatureResource(User user, ActiveCycleContextResponse context, DocSignatureRequest request) {
        Long evaluatedId = request.getResult().getAssignment().getEvaluatedPerson().getId();
        return canAccessEvaluatedScope(user, context, evaluatedId);
    }

    private boolean canManageSignatureResource(User user, ActiveCycleContextResponse context, DocSignatureRequest request) {
        Long evaluatedId = request.getResult().getAssignment().getEvaluatedPerson().getId();
        return canManageEvaluatedFlow(user, context, evaluatedId);
    }

    private boolean canAccessImprovementResource(
            User user,
            ActiveCycleContextResponse context,
            GdrImprovementOpportunity opportunity
    ) {
        Long evaluatedId = opportunity.getResult().getAssignment().getEvaluatedPerson().getId();
        return canAccessEvaluatedScope(user, context, evaluatedId);
    }

    private boolean canManageImprovementResource(
            User user,
            ActiveCycleContextResponse context,
            GdrImprovementOpportunity opportunity
    ) {
        Long evaluatedId = opportunity.getResult().getAssignment().getEvaluatedPerson().getId();
        return canManageEvaluatedFlow(user, context, evaluatedId);
    }

    private boolean canAccessGoalResource(User user, ActiveCycleContextResponse context, GdrGoal goal) {
        Long evaluatedId = goal.getAssignment().getEvaluatedPerson().getId();
        return canAccessEvaluatedScope(user, context, evaluatedId);
    }

    private boolean canAccessEvidenceResource(User user, ActiveCycleContextResponse context, GdrEvidence evidence) {
        Long evaluatedId = evidence.getGoal().getAssignment().getEvaluatedPerson().getId();
        return canAccessEvaluatedScope(user, context, evaluatedId);
    }

    private boolean canReviewGoalAchievementResource(User user, ActiveCycleContextResponse context, GdrGoal goal) {
        if (!canAccessGoalResource(user, context, goal)) {
            return false;
        }
        if (isAdminSistema(user) || isOrh(user)) {
            return true;
        }
        if (!isGdrUsuario(user)) {
            return false;
        }
        return ACTOR_EVALUADOR.equals(context.functionalActor())
                || ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor());
    }

    private boolean canReviewEvidenceResource(User user, ActiveCycleContextResponse context, GdrEvidence evidence) {
        if (!canAccessEvidenceResource(user, context, evidence)) {
            return false;
        }
        if (isAdminSistema(user) || isOrh(user)) {
            return true;
        }
        if (!isGdrUsuario(user)) {
            return false;
        }
        return ACTOR_EVALUADOR.equals(context.functionalActor())
                || ACTOR_EVALUADOR_Y_EVALUADO.equals(context.functionalActor());
    }

    private boolean canManageOwnEvidenceResource(User user, ActiveCycleContextResponse context, GdrGoal goal) {
        Long evaluatedId = goal.getAssignment().getEvaluatedPerson().getId();
        return canManageOwnEvaluatedFlow(user, context, evaluatedId);
    }

    private boolean canManageOwnEvidenceResource(User user, ActiveCycleContextResponse context, GdrEvidence evidence) {
        Long evaluatedId = evidence.getGoal().getAssignment().getEvaluatedPerson().getId();
        return canManageOwnEvaluatedFlow(user, context, evaluatedId);
    }

    private boolean canManageFinalEvaluationResource(
            User user,
            ActiveCycleContextResponse context,
            GdrEvaluationAssignment assignment
    ) {
        Long evaluatedId = assignment.getEvaluatedPerson().getId();
        return canManageEvaluatedFlow(user, context, evaluatedId);
    }

    private record ResolvedAccess(
            User user,
            ActiveCycleContextResponse context,
            FeatureAccessResponse featureAccess
    ) {
    }
}
