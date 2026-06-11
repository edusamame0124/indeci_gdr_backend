package pe.gob.gdr.access.application.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.GdrSeguimientoRequest;
import pe.gob.gdr.access.application.dto.response.GdrSeguimientoResponse;
import pe.gob.gdr.access.application.dto.response.ResumenSeguimientoResponse;
import pe.gob.gdr.access.application.mapper.GdrSeguimientoMapper;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrSeguimiento;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrSeguimientoRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

@Service
public class GdrSeguimientoService {

    private static final Set<String> TIPOS_VALIDOS = Set.of(
            "SEGUIMIENTO_PERIODICO", "RETROALIMENTACION_PERIODICA");

    private static final long DIAS_MINIMOS_SEGUIMIENTO = 180L;

    private final GdrSeguimientoRepository seguimientoRepository;
    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final GdrSeguimientoMapper mapper;

    public GdrSeguimientoService(
            GdrSeguimientoRepository seguimientoRepository,
            GdrEvaluationAssignmentRepository assignmentRepository,
            UserRepository userRepository,
            GdrSeguimientoMapper mapper) {
        this.seguimientoRepository = seguimientoRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ResumenSeguimientoResponse getResumen(Long assignmentId) {
        GdrEvaluationAssignment assignment = loadAssignment(assignmentId);
        List<GdrSeguimiento> reuniones =
                seguimientoRepository.findByAssignmentIdOrderByFechaReunion(assignmentId);

        List<GdrSeguimientoResponse> respuestas = reuniones.stream()
                .map(mapper::toResponse)
                .toList();

        LocalDate primera = reuniones.isEmpty() ? null : reuniones.get(0).getFechaReunion();
        LocalDate ultima  = reuniones.isEmpty() ? null : reuniones.get(reuniones.size() - 1).getFechaReunion();
        long dias = (primera != null && ultima != null)
                ? ChronoUnit.DAYS.between(primera, ultima)
                : 0L;
        boolean cumple = dias >= DIAS_MINIMOS_SEGUIMIENTO;
        String alerta = cumple ? null
                : String.format("El seguimiento lleva %d días. Mínimo normativo: %d días (6 meses). "
                        + "Referencia: RPE 068-2020-SERVIR-PE Art. 26.", dias, DIAS_MINIMOS_SEGUIMIENTO);

        return new ResumenSeguimientoResponse(
                assignmentId,
                assignment.getCycle().getId(),
                reuniones.size(),
                primera,
                ultima,
                dias,
                cumple,
                alerta,
                respuestas
        );
    }

    @Transactional(readOnly = true)
    public List<GdrSeguimientoResponse> listarPorCiclo(Long cycleId) {
        return seguimientoRepository.findByCycleIdOrderByFechaReunion(cycleId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public GdrSeguimientoResponse registrar(GdrSeguimientoRequest request, String username) {
        if (request.fechaReunion().isAfter(LocalDate.now())) {
            throw new DomainException("No se puede registrar una reunión con fecha futura.");
        }
        String tipo = request.tipoReunion() != null
                ? request.tipoReunion().toUpperCase()
                : "SEGUIMIENTO_PERIODICO";
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new DomainException("Tipo de reunión no válido: " + tipo
                    + ". Valores permitidos: " + TIPOS_VALIDOS);
        }

        GdrEvaluationAssignment assignment = loadAssignment(request.assignmentId());
        Long evaluadorId = resolveUserId(username);

        GdrSeguimiento seguimiento = GdrSeguimiento.builder()
                .assignment(assignment)
                .cycle(assignment.getCycle())
                .tipoReunion(tipo)
                .fechaReunion(request.fechaReunion())
                .descripcionAvance(request.descripcionAvance())
                .compromisos(request.compromisos())
                .estado("REALIZADA")
                .evaluadorId(evaluadorId)
                .build();

        return mapper.toResponse(seguimientoRepository.save(seguimiento));
    }

    @Transactional
    public GdrSeguimientoResponse registrarConsentimiento(Long seguimientoId, String username) {
        GdrSeguimiento s = seguimientoRepository.findById(seguimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión no encontrada: " + seguimientoId));
        s.setConsentimientoEvaluado(1);
        s.setEvaluadoId(resolveUserId(username));
        s.setEstado("CONFIRMADA");
        return mapper.toResponse(seguimientoRepository.save(s));
    }

    @Transactional
    public void eliminar(Long seguimientoId) {
        seguimientoRepository.findById(seguimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión no encontrada: " + seguimientoId));
        seguimientoRepository.deleteById(seguimientoId);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private Long resolveUserId(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new DomainException("Usuario no encontrado: " + username));
    }

    private GdrEvaluationAssignment loadAssignment(Long assignmentId) {
        return assignmentRepository.findByIdForAdministration(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asignación no encontrada: " + assignmentId));
    }
}
