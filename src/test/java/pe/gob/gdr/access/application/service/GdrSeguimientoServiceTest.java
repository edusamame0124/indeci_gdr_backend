package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.gdr.access.application.dto.request.GdrSeguimientoRequest;
import pe.gob.gdr.access.application.dto.response.GdrSeguimientoResponse;
import pe.gob.gdr.access.application.dto.response.ResumenSeguimientoResponse;
import pe.gob.gdr.access.application.mapper.GdrSeguimientoMapper;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrSeguimiento;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrSeguimientoRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GdrSeguimientoServiceTest {

    @Mock GdrSeguimientoRepository seguimientoRepository;
    @Mock GdrEvaluationAssignmentRepository assignmentRepository;
    @Mock UserRepository userRepository;

    private GdrSeguimientoService sut;

    @BeforeEach
    void setUp() {
        sut = new GdrSeguimientoService(
                seguimientoRepository, assignmentRepository, userRepository,
                new GdrSeguimientoMapper());
    }

    // ── registrar — caso feliz ──────────────────────────────────────────

    @Test
    void registrar_casoFeliz_guardaReunion() {
        GdrEvaluationAssignment assignment = assignmentConCiclo(1L, LocalDate.of(2025, 1, 1));
        User evaluador = userConId(10L);
        when(assignmentRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByUsername("eval1")).thenReturn(Optional.of(evaluador));
        when(seguimientoRepository.save(any())).thenAnswer(inv -> {
            GdrSeguimiento s = inv.getArgument(0);
            s.setId(99L);
            return s;
        });

        GdrSeguimientoRequest req = new GdrSeguimientoRequest(
                1L, "SEGUIMIENTO_PERIODICO",
                LocalDate.of(2025, 3, 15),
                "Avance 30%", "Completar módulo A");

        GdrSeguimientoResponse resp = sut.registrar(req, "eval1");

        assertThat(resp.id()).isEqualTo(99L);
        assertThat(resp.tipoReunion()).isEqualTo("SEGUIMIENTO_PERIODICO");
        assertThat(resp.estado()).isEqualTo("REALIZADA");
        assertThat(resp.evaluadorId()).isEqualTo(10L);
    }

    @Test
    void registrar_fechaFutura_lanzaError() {
        GdrSeguimientoRequest req = new GdrSeguimientoRequest(
                1L, "SEGUIMIENTO_PERIODICO",
                LocalDate.now().plusDays(1),
                null, null);

        assertThatThrownBy(() -> sut.registrar(req, "eval1"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("fecha futura");
    }

    @Test
    void registrar_assignmentInexistente_lanzaResourceNotFound() {
        when(assignmentRepository.findByIdForAdministration(99L)).thenReturn(Optional.empty());
        GdrSeguimientoRequest req = new GdrSeguimientoRequest(
                99L, null, LocalDate.of(2025, 4, 1), null, null);

        assertThatThrownBy(() -> sut.registrar(req, "eval1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getResumen — VAL-01 ─────────────────────────────────────────────

    @Test
    void getResumen_sinReuniones_cumpleMinimo_false() {
        GdrEvaluationAssignment assignment = assignmentConCiclo(1L, LocalDate.of(2025, 1, 1));
        when(assignmentRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(assignment));
        when(seguimientoRepository.findByAssignmentIdOrderByFechaReunion(1L)).thenReturn(List.of());

        ResumenSeguimientoResponse resumen = sut.getResumen(1L);

        assertThat(resumen.cumpleMinimo6Meses()).isFalse();
        assertThat(resumen.totalReuniones()).isZero();
        assertThat(resumen.alertaVAL01()).isNotNull();
    }

    @Test
    void getResumen_conSeguimientoMayor180Dias_cumpleMinimo_true() {
        GdrEvaluationAssignment assignment = assignmentConCiclo(1L, LocalDate.of(2025, 1, 1));
        when(assignmentRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(assignment));

        GdrSeguimiento r1 = reunionEnFecha(assignment, LocalDate.of(2025, 1, 10));
        GdrSeguimiento r2 = reunionEnFecha(assignment, LocalDate.of(2025, 7, 20));
        when(seguimientoRepository.findByAssignmentIdOrderByFechaReunion(1L))
                .thenReturn(List.of(r1, r2));

        ResumenSeguimientoResponse resumen = sut.getResumen(1L);

        assertThat(resumen.cumpleMinimo6Meses()).isTrue();
        assertThat(resumen.alertaVAL01()).isNull();
        assertThat(resumen.totalReuniones()).isEqualTo(2);
    }

    @Test
    void getResumen_seguimientoMenor180Dias_alertaVAL01_presente() {
        GdrEvaluationAssignment assignment = assignmentConCiclo(1L, LocalDate.of(2025, 1, 1));
        when(assignmentRepository.findByIdForAdministration(1L)).thenReturn(Optional.of(assignment));

        GdrSeguimiento r1 = reunionEnFecha(assignment, LocalDate.of(2025, 2, 1));
        GdrSeguimiento r2 = reunionEnFecha(assignment, LocalDate.of(2025, 4, 1));
        when(seguimientoRepository.findByAssignmentIdOrderByFechaReunion(1L))
                .thenReturn(List.of(r1, r2));

        ResumenSeguimientoResponse resumen = sut.getResumen(1L);

        assertThat(resumen.cumpleMinimo6Meses()).isFalse();
        assertThat(resumen.alertaVAL01()).contains("180");
    }

    // ── helpers ────────────────────────────────────────────────────────

    private GdrEvaluationAssignment assignmentConCiclo(Long id, LocalDate startDate) {
        ActiveCycle cycle = ActiveCycle.builder()
                .id(1L).code("C-2025").name("Ciclo 2025")
                .startDate(startDate).status("ACTIVE")
                .estadoEtapa("EN_SEGUIMIENTO")
                .build();
        GdrEvaluationAssignment a = new GdrEvaluationAssignment();
        a.setId(id);
        a.setCycle(cycle);
        return a;
    }

    private User userConId(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private GdrSeguimiento reunionEnFecha(GdrEvaluationAssignment assignment, LocalDate fecha) {
        return GdrSeguimiento.builder()
                .id((long) (Math.random() * 1000))
                .assignment(assignment)
                .cycle(assignment.getCycle())
                .tipoReunion("SEGUIMIENTO_PERIODICO")
                .fechaReunion(fecha)
                .estado("REALIZADA")
                .consentimientoEvaluado(0)
                .build();
    }
}
