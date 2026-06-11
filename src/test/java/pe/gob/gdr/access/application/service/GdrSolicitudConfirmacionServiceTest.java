package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.application.calendar.PeruBusinessDayCalendar;
import pe.gob.gdr.access.application.dto.request.SolicitudConfirmacionRequest;
import pe.gob.gdr.access.application.dto.response.ActiveCycleContextResponse;
import pe.gob.gdr.access.application.dto.response.SolicitudConfirmacionResponse;
import pe.gob.gdr.access.application.mapper.GdrConfirmacionMapper;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrCasoCie;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrPublicHolidayRepository;
import pe.gob.gdr.access.domain.repository.GdrSolicitudConfirmacionRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

/**
 * P4 — Solicitud de confirmación de calificación y derivación al CIE
 * (VAL-04 bloqueo + convocatoria CIE 3 días hábiles, RPE 068-2020 Art. 41-42).
 */
@ExtendWith(MockitoExtension.class)
class GdrSolicitudConfirmacionServiceTest {

    private static final Long EVALUADO_PERSON_ID = 2L;
    private static final String USERNAME_EVALUADO = "evaluado_test";

    @Mock GdrSolicitudConfirmacionRepository solicitudRepository;
    @Mock GdrCasoCieRepository casoCieRepository;
    @Mock GdrFinalEvaluationRepository finalEvaluationRepository;
    @Mock GdrGoalRepository goalRepository;
    @Mock GdrEvaluationAssignmentRepository assignmentRepositoryForValidacion;
    @Mock ActiveCycleRepository activeCycleRepository;
    @Mock GdrPublicHolidayRepository publicHolidayRepository;
    @Mock GdrAccessPolicyService accessPolicyService;
    @Mock NotificacionesService notificacionesService;
    @Mock UserRepository userRepository;
    @Mock GdrResultConfirmacionSyncService resultConfirmacionSyncService;

    private GdrSolicitudConfirmacionService sut;

    @BeforeEach
    void setUp() {
        sut = new GdrSolicitudConfirmacionService(
                solicitudRepository, casoCieRepository, finalEvaluationRepository,
                activeCycleRepository, publicHolidayRepository,
                new GdrValidacionNormativaService(
                        goalRepository, finalEvaluationRepository,
                        assignmentRepositoryForValidacion, casoCieRepository),
                accessPolicyService,
                new GdrConfirmacionMapper(), notificacionesService, userRepository,
                resultConfirmacionSyncService);
    }

    // ── caso feliz: dentro de plazo crea solicitud y deriva al CIE ──────────

    @Test
    void solicitar_dentroDePlazo_creaSolicitudYDerivaAlCie() {
        GdrFinalEvaluation evaluation = evaluacionConPlazo(LocalDate.now().plusDays(10));
        stubEvaluacion(evaluation);
        stubUsuarioEvaluado();
        when(solicitudRepository.findByFinalEvaluationId(50L)).thenReturn(Optional.empty());
        when(solicitudRepository.save(any())).thenAnswer(inv -> {
            GdrSolicitudConfirmacion s = inv.getArgument(0);
            s.setId(7L);
            return s;
        });
        when(casoCieRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(publicHolidayRepository.findHolidayDatesBetween(any(), any())).thenReturn(Set.of());
        when(userRepository.findActiveUsernamesByRoleCode("GDR_CIE")).thenReturn(List.of("cie_user"));

        SolicitudConfirmacionResponse resp = sut.solicitar(
                new SolicitudConfirmacionRequest(50L, "No estoy conforme con la calificación."),
                USERNAME_EVALUADO);

        assertThat(resp.estado()).isEqualTo(GdrSolicitudConfirmacion.ESTADO_EN_CIE);
        assertThat(resp.numeroCaso()).isEqualTo(String.format("CIE-%d-0007", LocalDate.now().getYear()));
        assertThat(resp.casoEstado()).isEqualTo(GdrCasoCie.ESTADO_RECIBIDO);
        verify(notificacionesService).emitForUser(
                eq("cie_user"), eq(NotificacionesService.CASO_CIE_DERIVADO), any());
        verify(resultConfirmacionSyncService).marcarPendiente(evaluation);
    }

    // ── caso borde: plazo de convocatoria CIE = +3 días hábiles ─────────────

    @Test
    void solicitar_calculaPlazoConvocatoriaCieTresDiasHabiles() {
        GdrFinalEvaluation evaluation = evaluacionConPlazo(LocalDate.now().plusDays(10));
        stubEvaluacion(evaluation);
        stubUsuarioEvaluado();
        when(solicitudRepository.findByFinalEvaluationId(50L)).thenReturn(Optional.empty());
        when(solicitudRepository.save(any())).thenAnswer(inv -> {
            GdrSolicitudConfirmacion s = inv.getArgument(0);
            s.setId(8L);
            return s;
        });
        when(casoCieRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(publicHolidayRepository.findHolidayDatesBetween(any(), any())).thenReturn(Set.of());
        when(userRepository.findActiveUsernamesByRoleCode("GDR_CIE")).thenReturn(List.of());

        sut.solicitar(new SolicitudConfirmacionRequest(50L, "Sustento."), USERNAME_EVALUADO);

        LocalDate plazoEsperado = PeruBusinessDayCalendar.nthBusinessDayAfter(LocalDate.now(), 3, Set.of());
        verify(casoCieRepository).save(org.mockito.ArgumentMatchers.argThat(caso ->
                plazoEsperado.equals(caso.getPlazoConvocatoria())));
    }

    // ── error normativo VAL-04: fuera de plazo bloquea ──────────────────────

    @Test
    void solicitar_fueraDePlazo_lanzaBloqueoVal04() {
        GdrFinalEvaluation evaluation = evaluacionConPlazo(LocalDate.now().minusDays(1));
        stubEvaluacion(evaluation);
        stubUsuarioEvaluado();
        when(solicitudRepository.findByFinalEvaluationId(50L)).thenReturn(Optional.empty());

        SolicitudConfirmacionRequest req = new SolicitudConfirmacionRequest(50L, "Sustento fuera de plazo.");

        assertThatThrownBy(() -> sut.solicitar(req, USERNAME_EVALUADO))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("venció");
    }

    // ── error: sin reunión retro final registrada no hay plazo activo ───────

    @Test
    void solicitar_sinReunionRetroFinal_lanzaError() {
        GdrFinalEvaluation evaluation = evaluacionConPlazo(null);
        stubEvaluacion(evaluation);
        stubUsuarioEvaluado();
        when(solicitudRepository.findByFinalEvaluationId(50L)).thenReturn(Optional.empty());

        SolicitudConfirmacionRequest req = new SolicitudConfirmacionRequest(50L, "Sustento.");

        assertThatThrownBy(() -> sut.solicitar(req, USERNAME_EVALUADO))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("retroalimentación final");
    }

    // ── error: solicitud duplicada ──────────────────────────────────────────

    @Test
    void solicitar_duplicada_lanzaError() {
        GdrFinalEvaluation evaluation = evaluacionConPlazo(LocalDate.now().plusDays(10));
        stubEvaluacion(evaluation);
        stubUsuarioEvaluado();
        when(solicitudRepository.findByFinalEvaluationId(50L))
                .thenReturn(Optional.of(GdrSolicitudConfirmacion.builder().id(1L).build()));

        SolicitudConfirmacionRequest req = new SolicitudConfirmacionRequest(50L, "Sustento.");

        assertThatThrownBy(() -> sut.solicitar(req, USERNAME_EVALUADO))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Ya existe");
    }

    // ── seguridad: solo el evaluado titular puede solicitar ─────────────────

    @Test
    void solicitar_usuarioNoEvaluado_lanzaError() {
        GdrFinalEvaluation evaluation = evaluacionConPlazo(LocalDate.now().plusDays(10));
        stubEvaluacion(evaluation);
        User otro = User.builder().username("otro_user").build();
        when(accessPolicyService.loadUserWithContext("otro_user")).thenReturn(otro);
        when(accessPolicyService.isAdminSistema(otro)).thenReturn(false);
        when(accessPolicyService.resolveContext(otro)).thenReturn(contextWithPersonId(99L));

        SolicitudConfirmacionRequest req = new SolicitudConfirmacionRequest(50L, "Sustento.");

        assertThatThrownBy(() -> sut.solicitar(req, "otro_user"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("evaluado titular");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private void stubEvaluacion(GdrFinalEvaluation evaluation) {
        when(finalEvaluationRepository.findByIdInActiveCycle(50L)).thenReturn(Optional.of(evaluation));
    }

    private void stubUsuarioEvaluado() {
        User evaluado = User.builder().username(USERNAME_EVALUADO).build();
        when(accessPolicyService.loadUserWithContext(USERNAME_EVALUADO)).thenReturn(evaluado);
        when(accessPolicyService.isAdminSistema(evaluado)).thenReturn(false);
        when(accessPolicyService.resolveContext(evaluado)).thenReturn(contextWithPersonId(EVALUADO_PERSON_ID));
    }

    private GdrFinalEvaluation evaluacionConPlazo(LocalDate plazoSolicitud) {
        HrPerson evaluado = HrPerson.builder().id(EVALUADO_PERSON_ID).displayName("EVALUADO PRUEBA").build();
        HrPerson evaluador = HrPerson.builder().id(3L).displayName("EVALUADOR PRUEBA").build();
        ActiveCycle cycle = ActiveCycle.builder()
                .id(1L).code("C-2026").name("Ciclo 2026")
                .startDate(LocalDate.of(2026, 1, 1)).status("ACTIVE")
                .build();
        GdrEvaluationAssignment assignment = GdrEvaluationAssignment.builder()
                .id(1L).cycle(cycle).evaluatedPerson(evaluado).evaluatorPerson(evaluador)
                .build();
        return GdrFinalEvaluation.builder()
                .id(50L)
                .assignment(assignment)
                .status("ACTIVE")
                .plazoSolicitudConfirmacion(plazoSolicitud)
                .build();
    }

    private ActiveCycleContextResponse contextWithPersonId(Long personId) {
        return new ActiveCycleContextResponse(
                1L, "C-2026", "Ciclo 2026",
                null, null,
                true, false, true,
                personId, "12345678", "PERSONA PRUEBA",
                1L, "ORG-001", "Unidad Org",
                GdrAccessPolicyService.ACTOR_EVALUADO,
                GdrAccessPolicyService.SCOPE_SELF,
                true, null
        );
    }
}
