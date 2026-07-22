package pe.gob.gdr.access.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.CreateAssignmentRequest;
import pe.gob.gdr.access.application.dto.request.UpdateAssignmentRequest;
import pe.gob.gdr.access.application.dto.request.UpdateAssignmentStatusRequest;
import pe.gob.gdr.access.application.dto.response.AssignmentDetailResponse;
import pe.gob.gdr.access.application.dto.response.AssignmentListItemResponse;
import pe.gob.gdr.access.application.dto.response.AssignmentPersonOptionResponse;
import pe.gob.gdr.access.application.dto.response.AssignmentPersonRefResponse;
import pe.gob.gdr.access.application.dto.response.AssignmentSummaryByPersonResponse;
import pe.gob.gdr.access.application.dto.response.CycleOptionResponse;
import pe.gob.gdr.access.application.port.SisrhDirectoryPort;
import pe.gob.gdr.access.application.port.SisrhDirectoryUser;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.GdrSegment;
import pe.gob.gdr.access.domain.model.HrOrgUnit;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.GdrSegmentRepository;
import pe.gob.gdr.access.domain.repository.HrPersonRepository;

@Service
public class AdminAssignmentService {

    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";
    private static final Set<String> VALID_ASSIGNMENT_STATUSES = Set.of(ACTIVE, INACTIVE);
    private static final Set<String> VALID_FILTER_STATUSES = Set.of(ACTIVE, INACTIVE, "ALL");

    public static final String ACTOR_EVALUADOR = "EVALUADOR";
    public static final String ACTOR_EVALUADO = "EVALUADO";
    public static final String ACTOR_EVALUADOR_Y_EVALUADO = "EVALUADOR_Y_EVALUADO";
    public static final String ACTOR_SIN_ROL_FUNCIONAL = "SIN_ROL_FUNCIONAL_GDR";

    private static final String DEFAULT_SEGMENT_CODE = "GENERAL";

    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final ActiveCycleRepository activeCycleRepository;
    private final HrPersonRepository hrPersonRepository;
    private final GdrSegmentRepository segmentRepository;
    private final SisrhDirectoryPort sisrhDirectoryPort;
    private final GdrCycleContextProvisioningService cycleContextProvisioningService;

    public AdminAssignmentService(
            GdrEvaluationAssignmentRepository assignmentRepository,
            ActiveCycleRepository activeCycleRepository,
            HrPersonRepository hrPersonRepository,
            GdrSegmentRepository segmentRepository,
            SisrhDirectoryPort sisrhDirectoryPort,
            GdrCycleContextProvisioningService cycleContextProvisioningService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.activeCycleRepository = activeCycleRepository;
        this.hrPersonRepository = hrPersonRepository;
        this.segmentRepository = segmentRepository;
        this.sisrhDirectoryPort = sisrhDirectoryPort;
        this.cycleContextProvisioningService = cycleContextProvisioningService;
    }

    @Transactional(readOnly = true)
    public List<CycleOptionResponse> listCycles() {
        Long activeCycleId = activeCycleRepository.findActiveCycle()
                .map(ActiveCycle::getId)
                .orElse(null);
        return activeCycleRepository.findAllOrderedForAdministration().stream()
                .map(cycle -> toCycleOption(cycle, activeCycleId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentListItemResponse> listByCycle(Long cycleId, String search, String statusFilter) {
        ensureCycleExists(cycleId);
        String normalizedSearch = normalizeSearch(search);
        String normalizedStatus = normalizeStatusFilter(statusFilter);

        List<GdrEvaluationAssignment> assignments = assignmentRepository.findByCycleIdForAdministration(cycleId);

        return assignments.stream()
                .filter(assignment -> matchesStatus(assignment, normalizedStatus))
                .filter(assignment -> matchesSearch(assignment, normalizedSearch))
                .map(this::toListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentDetailResponse getById(Long id) {
        return toDetail(loadForAdministration(id));
    }

    @Transactional(readOnly = true)
    public List<AssignmentSummaryByPersonResponse> summaryByCycle(Long cycleId) {
        ensureCycleExists(cycleId);
        List<GdrEvaluationAssignment> activeAssignments = assignmentRepository.findByCycleIdForAdministration(cycleId).stream()
                .filter(assignment -> ACTIVE.equalsIgnoreCase(assignment.getStatus()))
                .toList();

        Map<Long, PersonAggregation> aggregations = new HashMap<>();
        hrPersonRepository.findAllEligibleForAssignment()
                .forEach(person -> aggregations.put(person.getId(), new PersonAggregation(person)));

        for (GdrEvaluationAssignment assignment : activeAssignments) {
            HrPerson evaluator = assignment.getEvaluatorPerson();
            HrPerson evaluated = assignment.getEvaluatedPerson();
            if (evaluator != null) {
                aggregations.computeIfAbsent(evaluator.getId(), key -> new PersonAggregation(evaluator))
                        .incrementEvaluator();
            }
            if (evaluated != null) {
                aggregations.computeIfAbsent(evaluated.getId(), key -> new PersonAggregation(evaluated))
                        .incrementEvaluated();
            }
        }

        return aggregations.values().stream()
                .sorted((a, b) -> safeCompare(a.person.getDisplayName(), b.person.getDisplayName()))
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentPersonOptionResponse> searchablePersons(String search) {
        String normalized = normalizeSearch(search);
        List<HrPerson> persons = (normalized == null)
                ? hrPersonRepository.findAllEligibleForAssignment()
                : hrPersonRepository.findEligibleForAssignment(normalized);

        List<AssignmentPersonOptionResponse> result = new ArrayList<>();
        Set<String> localDocuments = new HashSet<>();
        for (HrPerson person : persons) {
            result.add(toPersonOption(person));
            String doc = person.getDocumentNumber();
            if (doc != null && !doc.isBlank()) {
                localDocuments.add(doc.trim());
            }
        }

        // Candidatos del SISRH (usuarios GDR_USUARIO aun no aprovisionados en GDR).
        // El directorio solo responde con termino de busqueda (q >= 2); ante error
        // o integracion apagada devuelve vacio y la busqueda queda solo local.
        if (normalized != null) {
            Map<String, AssignmentPersonOptionResponse> sisrhByDoc = new LinkedHashMap<>();
            for (SisrhDirectoryUser user : sisrhDirectoryPort.searchGdrUsers(normalized)) {
                String doc = user.dni() == null ? null : user.dni().trim();
                if (doc == null || doc.isBlank() || localDocuments.contains(doc)) {
                    continue; // sin DNI util, o ya presente como candidato local
                }
                sisrhByDoc.putIfAbsent(doc, toSisrhOption(user));
            }
            result.addAll(sisrhByDoc.values());
        }

        return result;
    }

    @Transactional
    public AssignmentDetailResponse create(CreateAssignmentRequest request) {
        Long cycleId = requireId(request.cycleId(), "El ciclo es obligatorio.");
        Long evaluatorPersonId = requireId(request.evaluatorPersonId(), "La persona evaluadora es obligatoria.");
        Long evaluatedPersonId = requireId(request.evaluatedPersonId(), "La persona evaluada es obligatoria.");

        ensureDistinctPersons(evaluatorPersonId, evaluatedPersonId);
        ActiveCycle cycle = loadCycleEditable(cycleId);
        HrPerson evaluator = loadEligiblePerson(evaluatorPersonId, "evaluadora");
        HrPerson evaluated = loadEligiblePerson(evaluatedPersonId, "evaluada");
        ensurePairIsUnique(cycleId, evaluatorPersonId, evaluatedPersonId, null);
        GdrSegment segment = resolveSegmentOrDefault(request.segmentId());

        GdrEvaluationAssignment assignment = GdrEvaluationAssignment.builder()
                .cycle(cycle)
                .evaluatorPerson(evaluator)
                .evaluatedPerson(evaluated)
                .segment(segment)
                .status(ACTIVE)
                .build();

        GdrEvaluationAssignment saved = assignmentRepository.save(assignment);
        ensureCycleContextForAssignment(saved);
        return toDetail(saved);
    }

    @Transactional
    public AssignmentDetailResponse update(Long id, UpdateAssignmentRequest request) {
        GdrEvaluationAssignment assignment = loadForAdministration(id);
        Long evaluatorPersonId = requireId(request.evaluatorPersonId(), "La persona evaluadora es obligatoria.");
        Long evaluatedPersonId = requireId(request.evaluatedPersonId(), "La persona evaluada es obligatoria.");

        ensureDistinctPersons(evaluatorPersonId, evaluatedPersonId);
        ensureCycleEditable(assignment.getCycle());

        boolean evaluatorChanged = !Objects.equals(assignment.getEvaluatorPerson().getId(), evaluatorPersonId);
        boolean evaluatedChanged = !Objects.equals(assignment.getEvaluatedPerson().getId(), evaluatedPersonId);

        if (evaluatorChanged) {
            assignment.setEvaluatorPerson(loadEligiblePerson(evaluatorPersonId, "evaluadora"));
        }
        if (evaluatedChanged) {
            assignment.setEvaluatedPerson(loadEligiblePerson(evaluatedPersonId, "evaluada"));
        }

        if (evaluatorChanged || evaluatedChanged) {
            ensurePairIsUnique(assignment.getCycle().getId(), evaluatorPersonId, evaluatedPersonId, id);
        }

        if (request.segmentId() != null) {
            GdrSegment currentSegment = assignment.getSegment();
            if (currentSegment == null || !Objects.equals(currentSegment.getId(), request.segmentId())) {
                assignment.setSegment(loadSegment(request.segmentId()));
            }
        }

        GdrEvaluationAssignment saved = assignmentRepository.save(assignment);
        if (ACTIVE.equalsIgnoreCase(saved.getStatus())) {
            ensureCycleContextForAssignment(saved);
        }
        return toDetail(saved);
    }

    @Transactional
    public AssignmentDetailResponse updateSegment(Long id, Long segmentId) {
        GdrEvaluationAssignment assignment = loadForAdministration(id);
        ensureCycleEditable(assignment.getCycle());
        Long resolvedSegmentId = requireId(segmentId, "El segmento es obligatorio.");
        GdrSegment current = assignment.getSegment();
        if (current == null || !Objects.equals(current.getId(), resolvedSegmentId)) {
            assignment.setSegment(loadSegment(resolvedSegmentId));
            assignmentRepository.save(assignment);
        }
        return toDetail(assignment);
    }

    @Transactional
    public AssignmentDetailResponse updateStatus(Long id, UpdateAssignmentStatusRequest request) {
        GdrEvaluationAssignment assignment = loadForAdministration(id);
        String requestedStatus = request.status() == null ? "" : request.status().trim().toUpperCase(Locale.ROOT);
        if (!VALID_ASSIGNMENT_STATUSES.contains(requestedStatus)) {
            throw new DomainException("El estado de la relacion debe ser ACTIVE o INACTIVE.");
        }

        if (requestedStatus.equals(assignment.getStatus())) {
            return toDetail(assignment);
        }

        ensureCycleEditable(assignment.getCycle());

        if (INACTIVE.equals(requestedStatus) && assignmentRepository.hasActiveGoals(id)) {
            throw new DomainException("No se puede inactivar la relacion porque existen metas activas asociadas.");
        }

        if (ACTIVE.equals(requestedStatus)) {
            ensurePairIsUnique(
                    assignment.getCycle().getId(),
                    assignment.getEvaluatorPerson().getId(),
                    assignment.getEvaluatedPerson().getId(),
                    id
            );
        }

        assignment.setStatus(requestedStatus);
        GdrEvaluationAssignment saved = assignmentRepository.save(assignment);
        if (ACTIVE.equalsIgnoreCase(saved.getStatus())) {
            ensureCycleContextForAssignment(saved);
        }
        return toDetail(saved);
    }

    private void ensureCycleContextForAssignment(GdrEvaluationAssignment assignment) {
        if (assignment == null || assignment.getCycle() == null) {
            return;
        }
        cycleContextProvisioningService.ensureContextForPerson(assignment.getEvaluatorPerson(), assignment.getCycle());
        cycleContextProvisioningService.ensureContextForPerson(assignment.getEvaluatedPerson(), assignment.getCycle());
    }

    private GdrEvaluationAssignment loadForAdministration(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("No se encontro la relacion solicitada.");
        }
        return assignmentRepository.findByIdForAdministration(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la relacion solicitada."));
    }

    private void ensureCycleExists(Long cycleId) {
        if (cycleId == null) {
            throw new DomainException("El ciclo es obligatorio.");
        }
        activeCycleRepository.findByIdForAdministration(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el ciclo solicitado."));
    }

    private ActiveCycle loadCycleEditable(Long cycleId) {
        ActiveCycle cycle = activeCycleRepository.findByIdForAdministration(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el ciclo solicitado."));
        ensureCycleEditable(cycle);
        return cycle;
    }

    private void ensureCycleEditable(ActiveCycle cycle) {
        if (cycle == null || !ACTIVE.equalsIgnoreCase(cycle.getStatus())) {
            throw new DomainException("Solo se pueden administrar relaciones de ciclos activos.");
        }
    }

    private HrPerson loadEligiblePerson(Long personId, String label) {
        return hrPersonRepository.findEligibleById(personId)
                .orElseThrow(() -> new DomainException(
                        "La persona " + label + " no es elegible. Debe tener usuario activo y rol GDR_USUARIO."));
    }

    private GdrSegment loadSegment(Long segmentId) {
        return segmentRepository.findActiveById(segmentId)
                .orElseThrow(() -> new DomainException("El segmento indicado no es valido o esta inactivo."));
    }

    private GdrSegment resolveSegmentOrDefault(Long segmentId) {
        if (segmentId != null) {
            return loadSegment(segmentId);
        }
        return segmentRepository.findActiveByCode(DEFAULT_SEGMENT_CODE)
                .orElseThrow(() -> new DomainException(
                        "El catalogo de segmentos no contiene el segmento por defecto '" + DEFAULT_SEGMENT_CODE + "'."));
    }

    private void ensureDistinctPersons(Long evaluatorPersonId, Long evaluatedPersonId) {
        if (Objects.equals(evaluatorPersonId, evaluatedPersonId)) {
            throw new DomainException("La persona evaluadora y la evaluada no pueden ser la misma.");
        }
    }

    private void ensurePairIsUnique(Long cycleId, Long evaluatorPersonId, Long evaluatedPersonId, Long excludedId) {
        boolean exists = (excludedId == null)
                ? assignmentRepository.existsActivePairInCycle(cycleId, evaluatorPersonId, evaluatedPersonId)
                : assignmentRepository.existsActivePairInCycleExcludingId(
                cycleId, evaluatorPersonId, evaluatedPersonId, excludedId);
        if (exists) {
            throw new DomainException("Ya existe una relacion activa con el mismo evaluador y evaluado en este ciclo.");
        }
    }

    private Long requireId(Long value, String message) {
        if (value == null) {
            throw new DomainException(message);
        }
        return value;
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatusFilter(String statusFilter) {
        if (statusFilter == null || statusFilter.trim().isEmpty()) {
            return ACTIVE;
        }
        String upper = statusFilter.trim().toUpperCase(Locale.ROOT);
        if (!VALID_FILTER_STATUSES.contains(upper)) {
            throw new DomainException("El filtro de estado debe ser ACTIVE, INACTIVE o ALL.");
        }
        return upper;
    }

    private boolean matchesStatus(GdrEvaluationAssignment assignment, String filter) {
        if ("ALL".equals(filter)) {
            return true;
        }
        return filter.equalsIgnoreCase(assignment.getStatus());
    }

    private boolean matchesSearch(GdrEvaluationAssignment assignment, String search) {
        if (search == null) {
            return true;
        }
        String lowered = search.toLowerCase(Locale.ROOT);
        HrPerson evaluator = assignment.getEvaluatorPerson();
        HrPerson evaluated = assignment.getEvaluatedPerson();
        return matchesPerson(evaluator, lowered, search) || matchesPerson(evaluated, lowered, search);
    }

    private boolean matchesPerson(HrPerson person, String loweredSearch, String rawSearch) {
        if (person == null) {
            return false;
        }
        String displayName = person.getDisplayName() == null ? "" : person.getDisplayName().toLowerCase(Locale.ROOT);
        String document = person.getDocumentNumber() == null ? "" : person.getDocumentNumber();
        String unitName = (person.getOrgUnit() == null || person.getOrgUnit().getName() == null)
                ? ""
                : person.getOrgUnit().getName().toLowerCase(Locale.ROOT);
        return displayName.contains(loweredSearch)
                || document.startsWith(rawSearch)
                || unitName.contains(loweredSearch);
    }

    private String resolveFunctionalActor(long asEvaluatorCount, long asEvaluatedCount) {
        if (asEvaluatorCount > 0 && asEvaluatedCount > 0) {
            return ACTOR_EVALUADOR_Y_EVALUADO;
        }
        if (asEvaluatorCount > 0) {
            return ACTOR_EVALUADOR;
        }
        if (asEvaluatedCount > 0) {
            return ACTOR_EVALUADO;
        }
        return ACTOR_SIN_ROL_FUNCIONAL;
    }

    private int safeCompare(String left, String right) {
        String safeLeft = left == null ? "" : left;
        String safeRight = right == null ? "" : right;
        return safeLeft.compareToIgnoreCase(safeRight);
    }

    private CycleOptionResponse toCycleOption(ActiveCycle cycle, Long activeCycleId) {
        return new CycleOptionResponse(
                cycle.getId(),
                cycle.getCode(),
                cycle.getName(),
                cycle.getStatus(),
                cycle.getStartDate(),
                cycle.getEndDate(),
                Objects.equals(cycle.getId(), activeCycleId)
        );
    }

    private AssignmentListItemResponse toListItem(GdrEvaluationAssignment assignment) {
        GdrSegment segment = assignment.getSegment();
        return new AssignmentListItemResponse(
                assignment.getId(),
                assignment.getCycle().getId(),
                assignment.getCycle().getCode(),
                assignment.getCycle().getName(),
                toPersonRef(assignment.getEvaluatorPerson()),
                toPersonRef(assignment.getEvaluatedPerson()),
                segment != null ? segment.getId() : null,
                segment != null ? segment.getCode() : null,
                segment != null ? segment.getName() : null,
                assignment.getStatus(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }

    private AssignmentDetailResponse toDetail(GdrEvaluationAssignment assignment) {
        GdrSegment segment = assignment.getSegment();
        return new AssignmentDetailResponse(
                assignment.getId(),
                assignment.getCycle().getId(),
                assignment.getCycle().getCode(),
                assignment.getCycle().getName(),
                assignment.getCycle().getStatus(),
                toPersonRef(assignment.getEvaluatorPerson()),
                toPersonRef(assignment.getEvaluatedPerson()),
                segment != null ? segment.getId() : null,
                segment != null ? segment.getCode() : null,
                segment != null ? segment.getName() : null,
                assignment.getStatus(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }

    private AssignmentPersonRefResponse toPersonRef(HrPerson person) {
        if (person == null) {
            return null;
        }
        HrOrgUnit orgUnit = person.getOrgUnit();
        return new AssignmentPersonRefResponse(
                person.getId(),
                person.getDocumentNumber(),
                person.getDisplayName(),
                orgUnit == null ? null : orgUnit.getId(),
                orgUnit == null ? null : orgUnit.getCode(),
                orgUnit == null ? null : orgUnit.getName()
        );
    }

    private AssignmentSummaryByPersonResponse toSummary(PersonAggregation aggregation) {
        HrPerson person = aggregation.person;
        HrOrgUnit orgUnit = person.getOrgUnit();
        return new AssignmentSummaryByPersonResponse(
                person.getId(),
                person.getDocumentNumber(),
                person.getDisplayName(),
                orgUnit == null ? null : orgUnit.getId(),
                orgUnit == null ? null : orgUnit.getCode(),
                orgUnit == null ? null : orgUnit.getName(),
                aggregation.asEvaluatorCount,
                aggregation.asEvaluatedCount,
                resolveFunctionalActor(aggregation.asEvaluatorCount, aggregation.asEvaluatedCount)
        );
    }

    private AssignmentPersonOptionResponse toPersonOption(HrPerson person) {
        HrOrgUnit orgUnit = person.getOrgUnit();
        return new AssignmentPersonOptionResponse(
                person.getId(),
                person.getDocumentNumber(),
                person.getDisplayName(),
                orgUnit == null ? null : orgUnit.getId(),
                orgUnit == null ? null : orgUnit.getCode(),
                orgUnit == null ? null : orgUnit.getName(),
                null,
                AssignmentPersonOptionResponse.ORIGIN_LOCAL
        );
    }

    private AssignmentPersonOptionResponse toSisrhOption(SisrhDirectoryUser user) {
        return new AssignmentPersonOptionResponse(
                null,
                user.dni(),
                user.nombreCompleto(),
                null,
                user.areaCodigo(),
                user.areaNombre(),
                user.username(),
                AssignmentPersonOptionResponse.ORIGIN_SISRH
        );
    }

    private static final class PersonAggregation {
        private final HrPerson person;
        private long asEvaluatorCount;
        private long asEvaluatedCount;

        private PersonAggregation(HrPerson person) {
            this.person = person;
        }

        private void incrementEvaluator() {
            asEvaluatorCount++;
        }

        private void incrementEvaluated() {
            asEvaluatedCount++;
        }
    }
}
