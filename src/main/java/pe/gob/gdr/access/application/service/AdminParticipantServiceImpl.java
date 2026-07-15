package pe.gob.gdr.access.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.gdr.access.application.dto.request.CreateParticipantRoleRequest;
import pe.gob.gdr.access.application.dto.response.ParticipantResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrParticipant;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrParticipantRepository;
import pe.gob.gdr.access.domain.repository.HrPersonRepository;

@Service
@Transactional(readOnly = true)
public class AdminParticipantServiceImpl implements AdminParticipantService {

    private static final String ROLE_GDR_USUARIO = "GDR_USUARIO";

    private final GdrParticipantRepository participantRepository;
    private final ActiveCycleRepository activeCycleRepository;
    private final HrPersonRepository hrPersonRepository;
    private final SsoUserProvisioningService ssoUserProvisioningService;

    public AdminParticipantServiceImpl(
            GdrParticipantRepository participantRepository,
            ActiveCycleRepository activeCycleRepository,
            HrPersonRepository hrPersonRepository,
            SsoUserProvisioningService ssoUserProvisioningService) {
        this.participantRepository = participantRepository;
        this.activeCycleRepository = activeCycleRepository;
        this.hrPersonRepository = hrPersonRepository;
        this.ssoUserProvisioningService = ssoUserProvisioningService;
    }

    @Override
    @Transactional
    public ParticipantResponse assignRole(CreateParticipantRoleRequest request) {
        ActiveCycle cycle = activeCycleRepository.findByIdForAdministration(request.cycleId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo activo no encontrado."));

        HrPerson person = resolvePerson(request);

        GdrParticipant participant = participantRepository
                .findByCycleIdAndPersonId(cycle.getId(), person.getId())
                .orElse(null);

        if (participant != null) {
            // Manejo de estado Mixto
            if (!participant.getRole().equals(request.role()) && !participant.getRole().equals("MIXTO")) {
                participant.setRole("MIXTO");
            }
        } else {
            participant = GdrParticipant.builder()
                    .cycle(cycle)
                    .person(person)
                    .role(request.role())
                    .status("ACTIVE")
                    .build();
        }

        participant = participantRepository.save(participant);
        return mapToResponse(participant);
    }

    /**
     * Resuelve la persona destino. Si viene un {@code personId} local, lo usa.
     * Si viene del SISRH ({@code personId} nulo + DNI), aprovisiona la ficha
     * local (HR_PERSON + SEC_USER + rol GDR_USUARIO) reutilizando la misma
     * logica idempotente del JIT de login SSO, y luego la resuelve por DNI.
     */
    private HrPerson resolvePerson(CreateParticipantRoleRequest request) {
        if (request.personId() != null) {
            return hrPersonRepository.findActiveById(request.personId())
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada."));
        }

        String documentNumber = request.documentNumber() == null ? null : request.documentNumber().trim();
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new DomainException("Debe indicar la persona local (id) o el DNI del usuario de SISRH.");
        }

        ssoUserProvisioningService.ensureLocalUser(
                request.username(),
                List.of(ROLE_GDR_USUARIO),
                documentNumber,
                request.displayName(),
                request.orgUnitCode());

        return hrPersonRepository.findActiveByDocumentNumber(documentNumber)
                .orElseThrow(() -> new DomainException(
                        "No se pudo aprovisionar la persona desde SISRH. Verifique que tenga "
                        + "usuario y que su oficina exista y este activa en GDR."));
    }

    @Override
    public List<ParticipantResponse> listParticipantsByCycle(Long cycleId) {
        return participantRepository.findByCycleId(cycleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<pe.gob.gdr.access.application.dto.response.ParticipantSummaryResponse> summaryByCycle(Long cycleId) {
        return participantRepository.findByCycleId(cycleId).stream()
                .map(p -> new pe.gob.gdr.access.application.dto.response.ParticipantSummaryResponse(
                        p.getPerson().getId(),
                        p.getPerson().getDisplayName(),
                        p.getPerson().getDocumentNumber(),
                        p.getRole(),
                        0, // TODO: calcular evaluadorCount desde GdrEvaluationAssignment
                        0  // TODO: calcular evaluadoCount desde GdrEvaluationAssignment
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipantResponse updateRole(Long participantId, String role) {
        GdrParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participante no encontrado."));
        participant.setRole(role);
        participant = participantRepository.save(participant);
        return mapToResponse(participant);
    }

    @Override
    @Transactional
    public ParticipantResponse updateStatus(Long participantId, String status) {
        GdrParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participante no encontrado."));
        participant.setStatus(status);
        participant = participantRepository.save(participant);
        return mapToResponse(participant);
    }

    private ParticipantResponse mapToResponse(GdrParticipant p) {
        return new ParticipantResponse(
                p.getId(),
                p.getCycle().getId(),
                p.getPerson().getId(),
                p.getPerson().getDisplayName(),
                p.getPerson().getDocumentNumber(),
                p.getPerson().getOrgUnit().getName(),
                p.getRole(),
                p.getStatus(),
                p.getCreatedAt()
        );
    }
}
