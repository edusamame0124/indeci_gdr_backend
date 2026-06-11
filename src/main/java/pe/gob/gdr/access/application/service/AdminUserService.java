package pe.gob.gdr.access.application.service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.CreateUserRequest;
import pe.gob.gdr.access.application.dto.request.UpdateUserRequest;
import pe.gob.gdr.access.application.dto.request.UpdateUserRolesRequest;
import pe.gob.gdr.access.application.dto.request.UpdateUserStatusRequest;
import pe.gob.gdr.access.application.dto.response.HrOrgUnitOptionResponse;
import pe.gob.gdr.access.application.dto.response.RoleOptionResponse;
import pe.gob.gdr.access.application.dto.response.UserDetailResponse;
import pe.gob.gdr.access.application.dto.response.UserListItemResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.model.HrOrgUnit;
import pe.gob.gdr.access.domain.model.HrPerson;
import pe.gob.gdr.access.domain.model.Role;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.model.UserRole;
import pe.gob.gdr.access.domain.repository.ActiveCycleRepository;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;
import pe.gob.gdr.access.domain.repository.HrOrgUnitRepository;
import pe.gob.gdr.access.domain.repository.HrPersonRepository;
import pe.gob.gdr.access.domain.repository.RoleRepository;
import pe.gob.gdr.access.domain.repository.UserContextAssignmentRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

@Service
public class AdminUserService {

    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";
    private static final Set<String> VALID_USER_STATUSES = Set.of(ACTIVE, INACTIVE);
    private static final Set<String> ASSIGNABLE_ROLE_CODES = Set.of(
            "ADMIN_SISTEMA",
            "GDR_ORH",
            "GDR_JUNTA_DIRECTIVOS",
            "GDR_USUARIO",
            "GDR_CONSULTA",
            "GDR_CIE",
            "GDR_TITULAR",
            "GDR_AUDITOR"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HrPersonRepository hrPersonRepository;
    private final HrOrgUnitRepository hrOrgUnitRepository;
    private final ActiveCycleRepository activeCycleRepository;
    private final GdrEvaluationAssignmentRepository assignmentRepository;
    private final UserContextAssignmentRepository userContextAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HrPersonRepository hrPersonRepository,
            HrOrgUnitRepository hrOrgUnitRepository,
            ActiveCycleRepository activeCycleRepository,
            GdrEvaluationAssignmentRepository assignmentRepository,
            UserContextAssignmentRepository userContextAssignmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hrPersonRepository = hrPersonRepository;
        this.hrOrgUnitRepository = hrOrgUnitRepository;
        this.activeCycleRepository = activeCycleRepository;
        this.assignmentRepository = assignmentRepository;
        this.userContextAssignmentRepository = userContextAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserListItemResponse> listUsers() {
        return userRepository.findAllForAdministration().stream()
                .map(this::toListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUser(Long id) {
        return toDetail(loadUser(id));
    }

    @Transactional
    public UserDetailResponse createUser(CreateUserRequest request) {
        String username = normalizeRequired(request.username(), "El nombre de usuario es obligatorio.");
        String email = normalizeRequired(request.email(), "El correo electronico es obligatorio.");
        String displayName = trimRequired(request.displayName(), "El nombre visible es obligatorio.");
        String initialPassword = trimRequired(request.initialPassword(), "La contrasena inicial es obligatoria.");

        if (userRepository.existsByUsername(username)) {
            throw new DomainException("Ya existe un usuario con ese nombre de usuario.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DomainException("Ya existe un usuario con ese correo electronico.");
        }

        HrPerson person = resolveOrCreatePerson(
                request.documentNumber(),
                request.orgUnitId(),
                displayName,
                email,
                null
        );
        List<Role> roles = resolveAssignableRoles(request.roleCodes());

        User user = User.builder()
                .username(username)
                .email(email)
                .displayName(displayName)
                .passwordHash(passwordEncoder.encode(initialPassword))
                .person(person)
                .status(ACTIVE)
                .failedAttempts(0)
                .build();

        roles.forEach(role -> user.getUserRoles().add(UserRole.builder()
                .user(user)
                .role(role)
                .status(ACTIVE)
                .build()));

        return toDetail(userRepository.save(user));
    }

    @Transactional
    public UserDetailResponse updateUser(Long id, UpdateUserRequest request) {
        User user = loadUser(id);
        String email = normalizeRequired(request.email(), "El correo electronico es obligatorio.");
        String displayName = trimRequired(request.displayName(), "El nombre visible es obligatorio.");

        if (userRepository.existsByEmailForAnotherUser(email, id)) {
            throw new DomainException("Ya existe otro usuario con ese correo electronico.");
        }

        user.setEmail(email);
        user.setDisplayName(displayName);
        return toDetail(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<HrOrgUnitOptionResponse> listActiveOrgUnits() {
        return hrOrgUnitRepository.findAllActiveOrdered().stream()
                .map(this::toOrgUnitOption)
                .toList();
    }

    @Transactional
    public UserDetailResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = loadUser(id);
        String status = normalizeRequired(request.status(), "El estado del usuario es obligatorio.");
        if (!VALID_USER_STATUSES.contains(status)) {
            throw new DomainException("El estado del usuario debe ser ACTIVE o INACTIVE.");
        }

        user.setStatus(status);
        return toDetail(userRepository.save(user));
    }

    @Transactional
    public UserDetailResponse updateUserRoles(Long id, UpdateUserRolesRequest request) {
        User user = loadUser(id);
        List<Role> requestedRoles = resolveAssignableRoles(request.roleCodes());
        Set<String> requestedCodes = requestedRoles.stream()
                .map(Role::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Role> rolesByCode = requestedRoles.stream()
                .collect(Collectors.toMap(Role::getCode, Function.identity()));
        Map<String, UserRole> currentByRoleCode = user.getUserRoles().stream()
                .filter(userRole -> userRole.getRole() != null)
                .collect(Collectors.toMap(
                        userRole -> userRole.getRole().getCode(),
                        Function.identity(),
                        (first, ignored) -> first
                ));

        user.getUserRoles().forEach(userRole -> {
            Role role = userRole.getRole();
            if (role != null && requestedCodes.contains(role.getCode())) {
                userRole.setStatus(ACTIVE);
            } else {
                userRole.setStatus(INACTIVE);
            }
        });

        requestedCodes.stream()
                .filter(roleCode -> !currentByRoleCode.containsKey(roleCode))
                .map(rolesByCode::get)
                .filter(Objects::nonNull)
                .forEach(role -> user.getUserRoles().add(UserRole.builder()
                        .user(user)
                        .role(role)
                        .status(ACTIVE)
                        .build()));

        if (activeAssignableRoles(user).isEmpty()) {
            throw new DomainException("El usuario debe conservar al menos un rol tecnico activo.");
        }

        return toDetail(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<RoleOptionResponse> listAssignableRoles() {
        return roleRepository.findActiveByCodes(ASSIGNABLE_ROLE_CODES).stream()
                .sorted(Comparator.comparing(Role::getCode))
                .map(this::toRoleOption)
                .toList();
    }

    private User loadUser(Long id) {
        return userRepository.findByIdForAdministration(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario solicitado."));
    }

    private HrPerson resolveOrCreatePerson(
            String rawDocumentNumber,
            Long orgUnitId,
            String displayName,
            String email,
            Long currentUserId
    ) {
        String documentNumber = rawDocumentNumber == null ? null : rawDocumentNumber.trim();
        if (documentNumber == null || documentNumber.isEmpty()) {
            throw new DomainException("El DNI es obligatorio.");
        }

        if (hrPersonRepository.findActiveByDocumentNumber(documentNumber).isPresent()) {
            throw new DomainException(
                    "El DNI " + documentNumber + " ya se encuentra registrado.");
        }

        if (orgUnitId == null) {
            throw new DomainException("La unidad organica es obligatoria.");
        }
        HrOrgUnit orgUnit = hrOrgUnitRepository.findActiveById(orgUnitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro una unidad organica activa con el identificador indicado."));

        HrPerson newPerson = HrPerson.builder()
                .documentNumber(documentNumber)
                .displayName(displayName)
                .email(email)
                .orgUnit(orgUnit)
                .status(ACTIVE)
                .build();
        return hrPersonRepository.save(newPerson);
    }

    private List<Role> resolveAssignableRoles(List<String> roleCodes) {
        Set<String> normalizedCodes = normalizeRoleCodes(roleCodes);
        if (normalizedCodes.isEmpty()) {
            throw new DomainException("Debe asignar al menos un rol tecnico.");
        }
        if (!ASSIGNABLE_ROLE_CODES.containsAll(normalizedCodes)) {
            throw new DomainException("La lista contiene roles no permitidos para este modulo.");
        }

        List<Role> roles = roleRepository.findActiveByCodes(normalizedCodes);
        Set<String> foundCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
        if (!foundCodes.containsAll(normalizedCodes)) {
            throw new DomainException("Uno o mas roles tecnicos no existen o no estan activos.");
        }
        return roles;
    }

    private Set<String> normalizeRoleCodes(List<String> roleCodes) {
        if (roleCodes == null) {
            return Set.of();
        }
        return roleCodes.stream()
                .filter(Objects::nonNull)
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeRequired(String value, String message) {
        return trimRequired(value, message).toLowerCase(Locale.ROOT);
    }

    private String trimRequired(String value, String message) {
        if (value == null || value.trim().isBlank()) {
            throw new DomainException(message);
        }
        return value.trim();
    }

    private List<RoleOptionResponse> activeAssignableRoles(User user) {
        return user.getUserRoles().stream()
                .filter(userRole -> ACTIVE.equalsIgnoreCase(userRole.getStatus()))
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .filter(role -> ACTIVE.equalsIgnoreCase(role.getStatus()))
                .filter(role -> ASSIGNABLE_ROLE_CODES.contains(role.getCode()))
                .sorted(Comparator.comparing(Role::getCode))
                .map(this::toRoleOption)
                .toList();
    }

    private UserListItemResponse toListItem(User user) {
        HrPerson person = user.getPerson();
        HrOrgUnit orgUnit = person == null ? null : person.getOrgUnit();
        GdrParticipationStatus participationStatus = resolveGdrParticipationStatus(user);
        return new UserListItemResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus(),
                person == null ? null : person.getId(),
                person == null ? null : person.getDisplayName(),
                orgUnit == null ? null : orgUnit.getName(),
                activeAssignableRoles(user),
                participationStatus.status(),
                participationStatus.label(),
                participationStatus.functionalActor(),
                participationStatus.cycleContextAssigned(),
                user.getLastLoginAt()
        );
    }

    private UserDetailResponse toDetail(User user) {
        HrPerson person = user.getPerson();
        HrOrgUnit orgUnit = person == null ? null : person.getOrgUnit();
        GdrParticipationStatus participationStatus = resolveGdrParticipationStatus(user);
        return new UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus(),
                person == null ? null : person.getId(),
                person == null ? null : person.getDisplayName(),
                orgUnit == null ? null : orgUnit.getName(),
                activeAssignableRoles(user),
                participationStatus.status(),
                participationStatus.label(),
                participationStatus.functionalActor(),
                participationStatus.cycleContextAssigned(),
                user.getFailedAttempts(),
                user.getLockedUntil(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private RoleOptionResponse toRoleOption(Role role) {
        return new RoleOptionResponse(role.getCode(), role.getName());
    }

    private HrOrgUnitOptionResponse toOrgUnitOption(HrOrgUnit orgUnit) {
        return new HrOrgUnitOptionResponse(
                orgUnit.getId(),
                orgUnit.getCode(),
                orgUnit.getName()
        );
    }

    private GdrParticipationStatus resolveGdrParticipationStatus(User user) {
        if (!hasActiveAssignableRole(user, "GDR_USUARIO")) {
            return new GdrParticipationStatus("NO_APLICA", "No aplica", "SIN_ROL_FUNCIONAL_GDR", false);
        }
        HrPerson person = user.getPerson();
        if (person == null) {
            return new GdrParticipationStatus("SIN_PERSONA_HR", "Sin persona HR", "SIN_ROL_FUNCIONAL_GDR", false);
        }

        Optional<ActiveCycle> activeCycle = activeCycleRepository.findActiveCycle();
        if (activeCycle.isEmpty()) {
            return new GdrParticipationStatus("SIN_CICLO_ACTIVO", "Sin ciclo activo", "SIN_ROL_FUNCIONAL_GDR", false);
        }

        List<GdrEvaluationAssignment> assignments = assignmentRepository.findActiveByPersonIdInActiveCycle(person.getId());
        long asEvaluator = assignments.stream()
                .filter(assignment -> assignment.getEvaluatorPerson() != null)
                .filter(assignment -> Objects.equals(assignment.getEvaluatorPerson().getId(), person.getId()))
                .count();
        long asEvaluated = assignments.stream()
                .filter(assignment -> assignment.getEvaluatedPerson() != null)
                .filter(assignment -> Objects.equals(assignment.getEvaluatedPerson().getId(), person.getId()))
                .count();
        String functionalActor = resolveFunctionalActor(asEvaluator, asEvaluated);
        boolean contextAssigned = userContextAssignmentRepository
                .findActiveByUsernameAndCycleId(user.getUsername(), activeCycle.get().getId())
                .isPresent();

        if ("SIN_ROL_FUNCIONAL_GDR".equals(functionalActor)) {
            return new GdrParticipationStatus(
                    "SIN_PARTICIPACION_GDR",
                    "Sin participacion GDR",
                    functionalActor,
                    contextAssigned
            );
        }
        if (!contextAssigned) {
            return new GdrParticipationStatus(
                    "SIN_CONTEXTO_CICLO",
                    "Falta contexto de ciclo",
                    functionalActor,
                    false
            );
        }
        return new GdrParticipationStatus("COMPLETA", "Participacion completa", functionalActor, true);
    }

    private boolean hasActiveAssignableRole(User user, String roleCode) {
        return user.getUserRoles().stream()
                .filter(userRole -> ACTIVE.equalsIgnoreCase(userRole.getStatus()))
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .filter(role -> ACTIVE.equalsIgnoreCase(role.getStatus()))
                .anyMatch(role -> roleCode.equalsIgnoreCase(role.getCode()));
    }

    private String resolveFunctionalActor(long asEvaluatorCount, long asEvaluatedCount) {
        if (asEvaluatorCount > 0 && asEvaluatedCount > 0) {
            return "EVALUADOR_Y_EVALUADO";
        }
        if (asEvaluatorCount > 0) {
            return "EVALUADOR";
        }
        if (asEvaluatedCount > 0) {
            return "EVALUADO";
        }
        return "SIN_ROL_FUNCIONAL_GDR";
    }

    private record GdrParticipationStatus(
            String status,
            String label,
            String functionalActor,
            boolean cycleContextAssigned
    ) {
    }
}
