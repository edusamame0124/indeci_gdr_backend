package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.application.dto.request.AsignarDistinguidoRequest;
import pe.gob.gdr.access.application.dto.response.DistinguidoCandidatosResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrFinalEvaluation;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.policy.QualitativeRating;
import pe.gob.gdr.access.domain.repository.GdrCasoCieRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrFinalEvaluationRepository;
import pe.gob.gdr.access.domain.repository.GdrGoalRepository;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;

/**
 * P5 — VAL-08 bloqueo distinguido con confirmación pendiente (RPE 068-2020 Art. 50).
 */
@ExtendWith(MockitoExtension.class)
class GdrDistinguidoGovernanceServiceTest {

    private static final Long EVALUATION_ID = 50L;
    private static final Long ASSIGNMENT_ID = 1L;

    @Mock GdrResultRepository resultRepository;
    @Mock GdrFinalEvaluationRepository finalEvaluationRepository;
    @Mock GdrGoalRepository goalRepository;
    @Mock GdrEvaluationAssignmentRepository assignmentRepositoryForValidacion;
    @Mock GdrCasoCieRepository casoCieRepositoryForValidacion;
    @Mock GdrResultService resultService;
    @Mock ActaJuntaDistinguidoPdfExporter actaJuntaPdfExporter;

    private GdrDistinguidoGovernanceService sut;

    @BeforeEach
    void setUp() {
        sut = new GdrDistinguidoGovernanceService(
                resultRepository,
                finalEvaluationRepository,
                resultService,
                new GdrValidacionNormativaService(
                        finalEvaluationRepository,
                        assignmentRepositoryForValidacion, casoCieRepositoryForValidacion),
                actaJuntaPdfExporter);
    }

    @Test
    void listCandidatos_conConfirmacionPendiente_activaBloqueoVal08() {
        GdrResult elegible = resultadoElegible(GdrResult.ESTADO_CONF_PENDIENTE);
        when(resultRepository.findAllByCycleId(1L)).thenReturn(List.of(elegible));

        DistinguidoCandidatosResponse resp = sut.listCandidatos(1L);

        assertThat(resp.bloqueoVal08Activo()).isTrue();
        assertThat(resp.candidatosConConfirmacionPendiente()).isEqualTo(1);
        assertThat(resp.rows().getFirst().confirmacionPendiente()).isTrue();
        assertThat(resp.rows().getFirst().bloqueadoPorVal08()).isTrue();
        assertThat(resp.rows().getFirst().estadoConfirmacionLabel())
                .isEqualTo("Confirmación pendiente (CIE)");
    }

    @Test
    void asignar_conConfirmacionPendiente_lanzaBloqueoVal08() {
        GdrFinalEvaluation evaluation = evaluacionActiva();
        GdrResult result = resultadoElegible(GdrResult.ESTADO_CONF_PENDIENTE);
        when(resultRepository.findAllByCycleId(1L)).thenReturn(List.of(result));
        when(finalEvaluationRepository.findByIdAndCycle(EVALUATION_ID, 1L)).thenReturn(Optional.of(evaluation));
        when(resultRepository.findByAssignmentIdAndCycle(ASSIGNMENT_ID, 1L)).thenReturn(Optional.of(result));

        AsignarDistinguidoRequest req = new AsignarDistinguidoRequest(List.of(EVALUATION_ID));

        assertThatThrownBy(() -> sut.asignar(req, 1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("confirmación de calificación pendiente")
                .hasMessageContaining("Art. 50");
        verify(resultService, never()).syncResult(any(), any(), any(), any());
    }

    @Test
    void asignar_sinConfirmacionPendiente_permiteAsignacion() {
        GdrFinalEvaluation evaluation = evaluacionActiva();
        GdrResult result = resultadoElegible(GdrResult.ESTADO_CONF_SIN_SOLICITUD);
        when(resultRepository.findAllByCycleId(1L))
                .thenReturn(List.of(result))
                .thenReturn(List.of(resultConDistinguido()));
        when(finalEvaluationRepository.findByIdAndCycle(EVALUATION_ID, 1L)).thenReturn(Optional.of(evaluation));
        when(resultRepository.findByAssignmentIdAndCycle(ASSIGNMENT_ID, 1L)).thenReturn(Optional.of(result));
        when(finalEvaluationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(resultService.syncResult(any(), any(), any(), any())).thenReturn(result);

        var resp = sut.asignar(new AsignarDistinguidoRequest(List.of(EVALUATION_ID)), 1L);

        assertThat(resp.assignedCount()).isEqualTo(1);
        verify(resultService).syncResult(
                evaluation.getAssignment(), evaluation, BigDecimal.valueOf(92), QualitativeRating.DISTINGUIDO.code());
    }

    private GdrResult resultadoElegible(String estadoConfirmacion) {
        return GdrResult.builder()
                .id(10L)
                .assignment(asignacion())
                .finalEvaluation(evaluacionActiva())
                .consolidatedScore(BigDecimal.valueOf(92))
                .qualitativeRatingCode(QualitativeRating.BUEN_RENDIMIENTO.code())
                .qualRatingNotified("Y")
                .directive82Compliance("Y")
                .estadoConfirmacion(estadoConfirmacion)
                .build();
    }

    private GdrResult resultConDistinguido() {
        GdrResult r = resultadoElegible(GdrResult.ESTADO_CONF_SIN_SOLICITUD);
        r.setQualitativeRatingCode(QualitativeRating.DISTINGUIDO.code());
        return r;
    }

    private GdrFinalEvaluation evaluacionActiva() {
        return GdrFinalEvaluation.builder()
                .id(EVALUATION_ID)
                .assignment(asignacion())
                .status("ACTIVE")
                .build();
    }

    private GdrEvaluationAssignment asignacion() {
        HrPerson evaluado = HrPerson.builder().id(2L).displayName("MARIA LOPEZ").build();
        HrPerson evaluador = HrPerson.builder().id(3L).displayName("JUAN PEREZ").build();
        ActiveCycle cycle = ActiveCycle.builder().id(1L).name("Ciclo 2026").status("ACTIVE").build();
        return GdrEvaluationAssignment.builder()
                .id(ASSIGNMENT_ID)
                .cycle(cycle)
                .evaluatedPerson(evaluado)
                .evaluatorPerson(evaluador)
                .build();
    }
}
