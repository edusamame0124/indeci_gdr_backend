package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.application.dto.response.ActiveCycleContextResponse;
import pe.gob.gdr.access.application.dto.response.FeatureAccessResponse;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.HrOrgUnit;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.model.Role;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.model.UserContextAssignment;
import pe.gob.gdr.access.domain.model.UserRole;
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

@ExtendWith(MockitoExtension.class)
class GdrAccessPolicyServiceTest {

    @Mock UserRepository userRepository;
    @Mock ActiveCycleRepository activeCycleRepository;
    @Mock UserContextAssignmentRepository userContextAssignmentRepository;
    @Mock GdrEvaluationAssignmentRepository assignmentRepository;
    @Mock DocSignedFileRepository docSignedFileRepository;
    @Mock DocSignatureRequestRepository docSignatureRequestRepository;
    @Mock GdrImprovementOpportunityRepository improvementOpportunityRepository;
    @Mock GdrGoalRepository goalRepository;
    @Mock GdrEvidenceRepository evidenceRepository;
    @Mock GdrFinalEvaluationRepository finalEvaluationRepository;

    private GdrAccessPolicyService sut;

    @BeforeEach
    void setUp() {
        sut = new GdrAccessPolicyService(
                userRepository, activeCycleRepository, userContextAssignmentRepository,
                assignmentRepository, docSignedFileRepository, docSignatureRequestRepository,
                improvementOpportunityRepository, goalRepository, evidenceRepository, finalEvaluationRepository
        );
    }

    // ── isCie / isTitular / isAuditor ──────────────────────────────────────

    @Test
    void isCie_returnsTrueWhenUserHasGdrCieRole() {
        assertThat(sut.isCie(userWithRole("GDR_CIE"))).isTrue();
    }

    @Test
    void isCie_returnsFalseForUnrelatedRoles() {
        assertThat(sut.isCie(userWithRole("GDR_ORH"))).isFalse();
        assertThat(sut.isCie(userWithRole("GDR_USUARIO"))).isFalse();
        assertThat(sut.isCie(userWithRole("GDR_TITULAR"))).isFalse();
    }

    @Test
    void isTitular_returnsTrueOnlyForGdrTitularRole() {
        assertThat(sut.isTitular(userWithRole("GDR_TITULAR"))).isTrue();
        assertThat(sut.isTitular(userWithRole("GDR_CIE"))).isFalse();
    }

    @Test
    void isAuditor_returnsTrueOnlyForGdrAuditorRole() {
        assertThat(sut.isAuditor(userWithRole("GDR_AUDITOR"))).isTrue();
        assertThat(sut.isAuditor(userWithRole("GDR_CONSULTA"))).isFalse();
    }

    @Test
    void inactiveRole_doesNotGrantCieAccess() {
        Role role = Role.builder().code("GDR_CIE").status("INACTIVE").build();
        UserRole userRole = UserRole.builder().role(role).status("ACTIVE").build();
        User user = userWithUserRoles(Set.of(userRole));
        assertThat(sut.isCie(user)).isFalse();
    }

    // ── resolveFunctionalActor via resolveContext ───────────────────────────

    @Test
    void resolveContext_cieUserGetsCieActorWithInstitutionalScope() {
        User user = userWithPersonAndRole("cie_test", "GDR_CIE");
        ActiveCycle cycle = cycle("C-2025");
        when(activeCycleRepository.findActiveCycle()).thenReturn(Optional.of(cycle));
        when(userContextAssignmentRepository.findActiveByUsernameAndCycleId("cie_test", 1L))
                .thenReturn(Optional.empty());
        mockInstitutionalContextProvisioning(user, cycle);

        ActiveCycleContextResponse ctx = sut.resolveContext(user);

        assertThat(ctx.functionalActor()).isEqualTo(GdrAccessPolicyService.ACTOR_CIE);
        assertThat(ctx.operationalScope()).isEqualTo(GdrAccessPolicyService.SCOPE_INSTITUTIONAL);
        assertThat(ctx.gdrOperational()).isTrue();
        assertThat(ctx.assigned()).isTrue();
        assertThat(ctx.contextCode()).isEqualTo("CTX-GDR-C-2025");
    }

    @Test
    void resolveContext_titularUserGetsTitularActorWithConsultaScope() {
        User user = userWithPersonAndRole("titular_test", "GDR_TITULAR");
        ActiveCycle cycle = cycle("C-2025");
        when(activeCycleRepository.findActiveCycle()).thenReturn(Optional.of(cycle));
        when(userContextAssignmentRepository.findActiveByUsernameAndCycleId("titular_test", 1L))
                .thenReturn(Optional.empty());
        mockInstitutionalContextProvisioning(user, cycle);

        ActiveCycleContextResponse ctx = sut.resolveContext(user);

        assertThat(ctx.functionalActor()).isEqualTo(GdrAccessPolicyService.ACTOR_TITULAR);
        assertThat(ctx.operationalScope()).isEqualTo(GdrAccessPolicyService.SCOPE_CONSULTA);
        assertThat(ctx.gdrOperational()).isFalse();
        assertThat(ctx.assigned()).isTrue();
    }

    @Test
    void resolveContext_auditorUserGetsAuditorActorWithConsultaScope() {
        User user = userWithPersonAndRole("auditor_test", "GDR_AUDITOR");
        ActiveCycle cycle = cycle("C-2025");
        when(activeCycleRepository.findActiveCycle()).thenReturn(Optional.of(cycle));
        when(userContextAssignmentRepository.findActiveByUsernameAndCycleId("auditor_test", 1L))
                .thenReturn(Optional.empty());
        mockInstitutionalContextProvisioning(user, cycle);

        ActiveCycleContextResponse ctx = sut.resolveContext(user);

        assertThat(ctx.functionalActor()).isEqualTo(GdrAccessPolicyService.ACTOR_AUDITOR);
        assertThat(ctx.operationalScope()).isEqualTo(GdrAccessPolicyService.SCOPE_CONSULTA);
        assertThat(ctx.gdrOperational()).isFalse();
        assertThat(ctx.assigned()).isTrue();
    }

    @Test
    void resolveContext_orhAutoProvisionsContextWhenMissing() {
        User user = userWithPersonAndRole("orh_test", "GDR_ORH");
        ActiveCycle cycle = cycle("2026");
        when(activeCycleRepository.findActiveCycle()).thenReturn(Optional.of(cycle));
        when(userContextAssignmentRepository.findActiveByUsernameAndCycleId("orh_test", 1L))
                .thenReturn(Optional.empty());
        mockInstitutionalContextProvisioning(user, cycle);

        ActiveCycleContextResponse ctx = sut.resolveContext(user);

        assertThat(ctx.functionalActor()).isEqualTo(GdrAccessPolicyService.ACTOR_ORH);
        assertThat(ctx.assigned()).isTrue();
        assertThat(ctx.contextCode()).isEqualTo("CTX-GDR-2026");
        assertThat(ctx.contextName()).isEqualTo("Participacion GDR - Ciclo 2026");
    }

    // ── buildFeatureAccess: capacidades normativas CIE ─────────────────────

    @Test
    void buildFeatureAccess_cieCanResolverCasosCie() {
        User user = userWithPersonAndRole("cie_feat", "GDR_CIE");
        ActiveCycleContextResponse ctx = contextFor(user, GdrAccessPolicyService.ACTOR_CIE,
                GdrAccessPolicyService.SCOPE_INSTITUTIONAL, true);

        FeatureAccessResponse features = sut.buildFeatureAccess(user, ctx);

        assertThat(features.canResolverCasosCie()).isTrue();
        assertThat(features.canViewCie()).isTrue();
        assertThat(features.canViewConfirmacion()).isTrue();
    }

    @Test
    void buildFeatureAccess_cieCannotEditCronogramaOrGenerateInformeCierre() {
        User user = userWithPersonAndRole("cie_feat", "GDR_CIE");
        ActiveCycleContextResponse ctx = contextFor(user, GdrAccessPolicyService.ACTOR_CIE,
                GdrAccessPolicyService.SCOPE_INSTITUTIONAL, true);

        FeatureAccessResponse features = sut.buildFeatureAccess(user, ctx);

        assertThat(features.canEditCronograma()).isFalse();
        assertThat(features.canGenerarInformeCierre()).isFalse();
    }

    @Test
    void buildFeatureAccess_auditorCanViewInformeCierreButNotGenerate() {
        User user = userWithPersonAndRole("auditor_feat", "GDR_AUDITOR");
        ActiveCycleContextResponse ctx = contextFor(user, GdrAccessPolicyService.ACTOR_AUDITOR,
                GdrAccessPolicyService.SCOPE_CONSULTA, false);

        FeatureAccessResponse features = sut.buildFeatureAccess(user, ctx);

        assertThat(features.canViewInformeCierre()).isTrue();
        assertThat(features.canGenerarInformeCierre()).isFalse();
        assertThat(features.canViewAuditoria()).isTrue();
        assertThat(features.canResolverCasosCie()).isFalse();
    }

    @Test
    void buildFeatureAccess_titularCanViewReportsNotOperate() {
        User user = userWithPersonAndRole("titular_feat", "GDR_TITULAR");
        ActiveCycleContextResponse ctx = contextFor(user, GdrAccessPolicyService.ACTOR_TITULAR,
                GdrAccessPolicyService.SCOPE_CONSULTA, false);

        FeatureAccessResponse features = sut.buildFeatureAccess(user, ctx);

        assertThat(features.canViewInformeCierre()).isTrue();
        assertThat(features.canGenerarInformeCierre()).isFalse();
        assertThat(features.canResolverCasosCie()).isFalse();
        assertThat(features.canRegistrarSeguimiento()).isFalse();
    }

    // ── P4-07: segregación de funciones CIE / Junta (RPE 068-2020) ─────────

    @Test
    void buildFeatureAccess_cieCannotManageGoalsNorFinalEvaluations() {
        User user = userWithPersonAndRole("cie_seg", "GDR_CIE");
        ActiveCycleContextResponse ctx = contextFor(user, GdrAccessPolicyService.ACTOR_CIE,
                GdrAccessPolicyService.SCOPE_INSTITUTIONAL, true);

        FeatureAccessResponse features = sut.buildFeatureAccess(user, ctx);

        assertThat(features.canManageGoals()).isFalse();
        assertThat(features.canManageFinalEvaluations()).isFalse();
        assertThat(features.canManageEvidences()).isFalse();
        assertThat(features.canManageIndicators()).isFalse();
    }

    @Test
    void buildFeatureAccess_juntaCannotResolveNorViewCieCases() {
        User user = userWithPersonAndRole("junta_seg", "GDR_JUNTA_DIRECTIVOS");
        ActiveCycleContextResponse ctx = contextFor(user, GdrAccessPolicyService.ACTOR_JUNTA_DIRECTIVOS,
                GdrAccessPolicyService.SCOPE_INSTITUTIONAL, true);

        FeatureAccessResponse features = sut.buildFeatureAccess(user, ctx);

        assertThat(features.canResolverCasosCie()).isFalse();
        assertThat(features.canViewCie()).isFalse();
        assertThat(features.canSolicitarConfirmacion()).isFalse();
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private User userWithRole(String roleCode) {
        return userWithUserRoles(Set.of(activeUserRole(roleCode)));
    }

    private User userWithUserRoles(Set<UserRole> roles) {
        return User.builder().username("test").userRoles(new LinkedHashSet<>(roles)).build();
    }

    private User userWithPersonAndRole(String username, String roleCode) {
        HrOrgUnit orgUnit = new HrOrgUnit();
        orgUnit.setId(1L);
        orgUnit.setCode("ORG-001");
        orgUnit.setName("Unidad Org");
        HrPerson person = HrPerson.builder()
                .id(10L)
                .documentNumber("12345678")
                .displayName("Test User")
                .orgUnit(orgUnit)
                .build();
        Set<UserRole> roles = new LinkedHashSet<>();
        roles.add(activeUserRole(roleCode));
        return User.builder().id(100L).username(username).person(person).userRoles(roles).build();
    }

    private void mockInstitutionalContextProvisioning(User user, ActiveCycle cycle) {
        when(userContextAssignmentRepository.findByUserIdAndCycleId(user.getId(), cycle.getId()))
                .thenReturn(Optional.empty());
        when(userContextAssignmentRepository.save(any(UserContextAssignment.class)))
                .thenAnswer(invocation -> {
                    UserContextAssignment saved = invocation.getArgument(0);
                    if (saved.getId() == null) {
                        saved.setId(99L);
                    }
                    return saved;
                });
    }

    private UserRole activeUserRole(String roleCode) {
        Role role = Role.builder().code(roleCode).status("ACTIVE").build();
        return UserRole.builder().role(role).status("ACTIVE").build();
    }

    private ActiveCycle cycle(String code) {
        return ActiveCycle.builder().id(1L).code(code).name("Ciclo " + code).build();
    }

    private ActiveCycleContextResponse contextFor(User user,
            String actor, String scope, boolean gdrOperational) {
        return new ActiveCycleContextResponse(
                1L, "C-2025", "Ciclo 2025",
                null, null,
                true, false,
                user.getPerson() != null,
                user.getPerson() != null ? user.getPerson().getId() : null,
                user.getPerson() != null ? user.getPerson().getDocumentNumber() : null,
                user.getPerson() != null ? user.getPerson().getDisplayName() : null,
                user.getPerson() != null ? user.getPerson().getOrgUnit().getId() : null,
                user.getPerson() != null ? user.getPerson().getOrgUnit().getCode() : null,
                user.getPerson() != null ? user.getPerson().getOrgUnit().getName() : null,
                actor, scope, gdrOperational,
                null
        );
    }
}
