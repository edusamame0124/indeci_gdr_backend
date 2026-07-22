package pe.gob.gdr.access.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.model.UserContextAssignment;
import pe.gob.gdr.access.domain.repository.UserContextAssignmentRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

/**
 * Garantiza que los usuarios GDR activos de una persona tengan asignado el
 * contexto operativo del ciclo (USER_CONTEXT_ASSIGNMENT).
 *
 * Punto unico de esta logica: se invoca tanto al crear/activar una relacion
 * evaluador-evaluado (AdminAssignmentService) como al asignar un rol de
 * participacion Evaluador/Evaluado/Mixto (AdminParticipantServiceImpl), para
 * que ambos caminos dejen al usuario en el mismo estado operativo dentro del
 * ciclo y no vuelvan a divergir.
 */
@Service
public class GdrCycleContextProvisioningService {

    private static final String ACTIVE = "ACTIVE";

    private final UserRepository userRepository;
    private final UserContextAssignmentRepository userContextAssignmentRepository;

    public GdrCycleContextProvisioningService(
            UserRepository userRepository,
            UserContextAssignmentRepository userContextAssignmentRepository
    ) {
        this.userRepository = userRepository;
        this.userContextAssignmentRepository = userContextAssignmentRepository;
    }

    @Transactional
    public void ensureContextForPerson(HrPerson person, ActiveCycle cycle) {
        if (person == null || cycle == null || person.getId() == null || cycle.getId() == null) {
            return;
        }

        List<User> users = userRepository.findActiveGdrUsersByPersonId(person.getId());
        for (User user : users) {
            UserContextAssignment context = userContextAssignmentRepository
                    .findByUserIdAndCycleId(user.getId(), cycle.getId())
                    .orElseGet(() -> UserContextAssignment.builder()
                            .user(user)
                            .cycle(cycle)
                            .build());
            context.setContextCode(buildContextCode(cycle));
            context.setContextName(buildContextName(cycle));
            context.setStatus(ACTIVE);
            userContextAssignmentRepository.save(context);
        }
    }

    private String buildContextCode(ActiveCycle cycle) {
        String cycleCode = cycle.getCode() == null || cycle.getCode().trim().isEmpty()
                ? String.valueOf(cycle.getId())
                : cycle.getCode().trim();
        String code = "CTX-GDR-" + cycleCode;
        return code.length() <= 60 ? code : code.substring(0, 60);
    }

    private String buildContextName(ActiveCycle cycle) {
        String cycleName = cycle.getName() == null || cycle.getName().trim().isEmpty()
                ? "ciclo seleccionado"
                : cycle.getName().trim();
        String name = "Participacion GDR - " + cycleName;
        return name.length() <= 180 ? name : name.substring(0, 180);
    }
}
