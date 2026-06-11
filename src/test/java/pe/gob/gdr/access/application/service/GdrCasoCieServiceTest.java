package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.application.dto.request.ResolverCasoCieRequest;
import pe.gob.gdr.access.application.dto.response.CasoCieResponse;
import pe.gob.gdr.access.application.mapper.GdrConfirmacionMapper;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrCasoCie;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrPublicHolidayRepository;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;
import pe.gob.gdr.access.domain.repository.GdrSolicitudConfirmacionRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

/**
 * P4 — Resolución de casos CIE (decisión definitiva, RPE 068-2020 Art. 42).
 */
@ExtendWith(MockitoExtension.class)
class GdrCasoCieServiceTest {

    @Mock GdrCasoCieRepository casoCieRepository;
    @Mock GdrSolicitudConfirmacionRepository solicitudRepository;
    @Mock GdrFinalEvaluationRepository finalEvaluationRepository;
    @Mock GdrGoalRepository goalRepository;
    @Mock GdrEvaluationAssignmentRepository assignmentRepositoryForValidacion;
    @Mock GdrPublicHolidayRepository publicHolidayRepository;
    @Mock GdrResultService resultService;
    @Mock NotificacionesService notificacionesService;
    @Mock UserRepository userRepository;
    @Mock GdrResultConfirmacionSyncService resultConfirmacionSyncService;
    @Mock GdrResultRepository resultRepository;
    @Mock ActaCiePdfExporter actaCiePdfExporter;
    @Mock DocumentManagementService documentManagementService;

    private GdrCasoCieService sut;

    @BeforeEach
    void setUp() {
        sut = new GdrCasoCieService(
                casoCieRepository, solicitudRepository, finalEvaluationRepository,
                publicHolidayRepository, new GdrValidacionNormativaService(
                        goalRepository, finalEvaluationRepository,
                        assignmentRepositoryForValidacion, casoCieRepository),
                resultService, new GdrConfirmacionMapper(), notificacionesService, userRepository,
                resultConfirmacionSyncService, resultRepository, actaCiePdfExporter, documentManagementService);
    }

    private void stubActaPersistencia(GdrCasoCie caso) {
        GdrResult result = GdrResult.builder()
                .id(100L)
                .assignment(caso.getSolicitud().getFinalEvaluation().getAssignment())
                .finalEvaluation(caso.getSolicitud().getFinalEvaluation())
                .consolidatedScore(caso.getSolicitud().getFinalEvaluation().getConsolidatedScore())
                .qualitativeRatingCode("SUJETO_OBSERVACION")
                .build();
        when(resultRepository.findByAssignmentIdInActiveCycle(1L)).thenReturn(Optional.of(result));
        when(actaCiePdfExporter.exportPdf(any())).thenReturn(new byte[] {37, 80, 68, 70});
        when(documentManagementService.persistGeneratedActaDocument(
                any(), any(), any(), any(), any(), any())).thenReturn(501L);
    }

    // ── caso feliz: CONFIRMA mantiene la calificación ────────────────────────

    @Test
    void resolver_confirma_cierraCasoYSolicitudSinTocarCalificacion() {
        GdrCasoCie caso = casoRecibido();
        when(casoCieRepository.findById(10L)).thenReturn(Optional.of(caso));
        when(casoCieRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findActiveGdrUsersByPersonId(any())).thenReturn(List.of());
        stubActaPersistencia(caso);

        CasoCieResponse resp = sut.resolver(10L,
                new ResolverCasoCieRequest("CONFIRMA", null, "El CIE ratifica la calificación."),
                "cie.orh");

        assertThat(resp.estado()).isEqualTo(GdrCasoCie.ESTADO_RESUELTO);
        assertThat(resp.decision()).isEqualTo(GdrCasoCie.DECISION_CONFIRMA);
        assertThat(caso.getSolicitud().getEstado()).isEqualTo(GdrSolicitudConfirmacion.ESTADO_RESUELTA);
        verify(resultService, never()).syncResult(any(), any(), any(), any());
        verify(resultConfirmacionSyncService).marcarResuelta(caso.getSolicitud().getFinalEvaluation());
        verify(finalEvaluationRepository, never()).save(any());
    }

    // ── caso feliz: MODIFICA actualiza calificación y sincroniza resultado ──

    @Test
    void resolver_modifica_actualizaCalificacionYSincronizaResultado() {
        GdrCasoCie caso = casoRecibido();
        GdrFinalEvaluation evaluation = caso.getSolicitud().getFinalEvaluation();
        when(casoCieRepository.findById(10L)).thenReturn(Optional.of(caso));
        when(casoCieRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(finalEvaluationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findActiveGdrUsersByPersonId(any())).thenReturn(List.of());
        stubActaPersistencia(caso);

        CasoCieResponse resp = sut.resolver(10L,
                new ResolverCasoCieRequest("MODIFICA", "BUEN_RENDIMIENTO", "Se acoge el sustento del evaluado."),
                "cie.orh");

        assertThat(evaluation.getQualitativeRatingCode()).isEqualTo("BUEN_RENDIMIENTO");
        assertThat(resp.calificacionResultado()).isEqualTo("BUEN_RENDIMIENTO");
        verify(resultService).syncResult(
                eq(evaluation.getAssignment()), eq(evaluation),
                eq(evaluation.getConsolidatedScore()), eq("BUEN_RENDIMIENTO"));
    }

    // ── error normativo: caso ya resuelto (decisión definitiva) ─────────────

    @Test
    void resolver_casoYaResuelto_lanzaError() {
        GdrCasoCie caso = casoRecibido();
        caso.setEstado(GdrCasoCie.ESTADO_RESUELTO);
        when(casoCieRepository.findById(10L)).thenReturn(Optional.of(caso));

        ResolverCasoCieRequest req = new ResolverCasoCieRequest("CONFIRMA", null, "Sustento.");

        assertThatThrownBy(() -> sut.resolver(10L, req, "cie.orh"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("definitiva");
    }

    // ── caso borde: MODIFICA sin calificación o con calificación inválida ───

    @Test
    void resolver_modificaSinCalificacion_lanzaError() {
        when(casoCieRepository.findById(10L)).thenReturn(Optional.of(casoRecibido()));

        ResolverCasoCieRequest req = new ResolverCasoCieRequest("MODIFICA", "  ", "Sustento.");

        assertThatThrownBy(() -> sut.resolver(10L, req, "cie.orh"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("nueva calificación");
    }

    @Test
    void resolver_modificaConCalificacionInvalida_lanzaError() {
        when(casoCieRepository.findById(10L)).thenReturn(Optional.of(casoRecibido()));

        ResolverCasoCieRequest req = new ResolverCasoCieRequest("MODIFICA", "EXCELENTE", "Sustento.");

        assertThatThrownBy(() -> sut.resolver(10L, req, "cie.orh"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("no es válida");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private GdrCasoCie casoRecibido() {
        HrPerson evaluado = HrPerson.builder().id(2L).displayName("EVALUADO PRUEBA").build();
        HrPerson evaluador = HrPerson.builder().id(3L).displayName("EVALUADOR PRUEBA").build();
        ActiveCycle cycle = ActiveCycle.builder()
                .id(1L).code("C-2026").name("Ciclo 2026")
                .startDate(LocalDate.of(2026, 1, 1)).status("ACTIVE")
                .build();
        GdrEvaluationAssignment assignment = GdrEvaluationAssignment.builder()
                .id(1L).cycle(cycle).evaluatedPerson(evaluado).evaluatorPerson(evaluador)
                .build();
        GdrFinalEvaluation evaluation = GdrFinalEvaluation.builder()
                .id(50L)
                .assignment(assignment)
                .consolidatedScore(new BigDecimal("78.5000"))
                .qualitativeRatingCode("SUJETO_OBSERVACION")
                .status("ACTIVE")
                .build();
        GdrSolicitudConfirmacion solicitud = GdrSolicitudConfirmacion.builder()
                .id(7L)
                .finalEvaluation(evaluation)
                .evaluado(evaluado)
                .cycle(cycle)
                .fechaSolicitud(LocalDateTime.now().minusDays(1))
                .sustentoEvaluado("No estoy conforme.")
                .estado(GdrSolicitudConfirmacion.ESTADO_EN_CIE)
                .build();
        return GdrCasoCie.builder()
                .id(10L)
                .solicitud(solicitud)
                .numeroCaso("CIE-2026-0007")
                .fechaIngresoCie(LocalDateTime.now().minusDays(1))
                .plazoConvocatoria(LocalDate.now().plusDays(3))
                .estado(GdrCasoCie.ESTADO_RECIBIDO)
                .build();
    }
}
