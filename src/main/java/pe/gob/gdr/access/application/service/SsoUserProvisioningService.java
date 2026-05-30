package pe.gob.gdr.access.application.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.domain.model.Role;
import pe.gob.gdr.access.domain.model.User;
import pe.gob.gdr.access.domain.model.UserRole;
import pe.gob.gdr.access.domain.repository.RoleRepository;
import pe.gob.gdr.access.domain.repository.UserRepository;

/**
 * SSO Fase 3 — Aprovisionamiento JIT (just-in-time) del usuario local en GDR.
 *
 * Con la Opción A (identidad centralizada en el SISRH) el token SSO no garantiza
 * una fila en SEC_USER, pero la lógica de negocio de GDR la exige
 * (findByUsername(...).orElseThrow para resolver el usuario en notificaciones,
 * metas, evaluaciones, etc.). Este servicio garantiza, en el primer request SSO
 * de cada usuario, que exista la ficha local — creándola si falta — y mantiene
 * sus roles locales en sintonía con el claim {@code sistemas.rendimiento} del
 * token en cada ingreso.
 *
 * El token del SISRH NO trae nombre ni email → se sintetizan (editables luego por
 * el ADMIN de GDR). El hash de contraseña es un placeholder aleatorio: el login
 * real ocurre en el SISRH, GDR nunca lo valida. {@code person} (HrPerson) queda
 * nulo: un usuario administrativo SSO no requiere ficha de RR. HH.; los flujos
 * que dependen de persona quedan vacíos pero no rompen.
 *
 * No se anota @Transactional a propósito: cada operación de repositorio corre en
 * su propia transacción, lo que permite recuperarse de la carrera de inserción
 * concurrente (el dashboard dispara varios requests en el primer ingreso)
 * capturando DataIntegrityViolationException y re-buscando. El encoder es local
 * (no el bean de SecurityConfig) para evitar el ciclo
 * jwtFilter → ssoProvisioning → securityConfig → jwtFilter.
 */
@Service
public class SsoUserProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(SsoUserProvisioningService.class);
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ESTADO_ACTIVO = "ACTIVE";
    private static final String ESTADO_INACTIVO = "INACTIVE";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecureRandom random = new SecureRandom();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SsoUserProvisioningService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Garantiza la ficha local del usuario SSO y sincroniza sus roles.
     *
     * @param username      subject del token SISRH (== USERNAME local).
     * @param rolesDelToken roles del claim sistemas.rendimiento (vienen SIN
     *                      prefijo ROLE_; coinciden con SEC_ROLE.ROLE_CODE).
     */
    public void ensureLocalUser(String username, List<String> rolesDelToken) {
        if (username == null || username.isBlank()) {
            return;
        }
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = crearUsuarioSso(username);
        }
        sincronizarRoles(user, rolesDelToken);
    }

    private User crearUsuarioSso(String username) {
        try {
            User nuevo = User.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(passwordAleatoria()))
                    .email(emailSintetico(username))
                    .displayName(username)
                    .person(null)
                    .status(ESTADO_ACTIVO)
                    .failedAttempts(0)
                    .build();
            User guardado = userRepository.save(nuevo);
            log.info("[SSO] Usuario local aprovisionado: {}", username);
            return guardado;
        } catch (DataIntegrityViolationException carrera) {
            // Otra request concurrente lo creó primero (primer ingreso dispara
            // varias llamadas en paralelo desde el dashboard).
            return userRepository.findByUsername(username).orElseThrow(() -> carrera);
        }
    }

    /**
     * Re-sincroniza los roles locales con el token: activa/crea los presentes en
     * el token y desactiva (no borra) los locales activos que ya no vienen.
     * Sólo escribe en BD si hubo algún cambio efectivo.
     */
    private void sincronizarRoles(User user, List<String> rolesDelToken) {
        Set<String> deseados = (rolesDelToken == null ? List.<String>of() : rolesDelToken).stream()
                .filter(Objects::nonNull)
                .map(this::normalizarCodigo)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, UserRole> actuales = new HashMap<>();
        for (UserRole ur : user.getUserRoles()) {
            if (ur.getRole() != null) {
                actuales.put(ur.getRole().getCode(), ur);
            }
        }

        boolean cambios = false;

        // 1) Activar o crear los roles que vienen en el token.
        for (String codigo : deseados) {
            UserRole existente = actuales.get(codigo);
            if (existente != null) {
                if (!ESTADO_ACTIVO.equalsIgnoreCase(existente.getStatus())) {
                    existente.setStatus(ESTADO_ACTIVO);
                    cambios = true;
                }
            } else {
                Role role = roleRepository.findByCode(codigo).orElse(null);
                if (role == null) {
                    log.warn("[SSO] Rol '{}' del token no existe en SEC_ROLE; se omite para {}",
                            codigo, user.getUsername());
                    continue;
                }
                user.getUserRoles().add(UserRole.builder()
                        .user(user)
                        .role(role)
                        .status(ESTADO_ACTIVO)
                        .assignedAt(LocalDateTime.now())
                        .build());
                cambios = true;
            }
        }

        // 2) Desactivar los roles locales activos que ya no vienen en el token.
        for (UserRole ur : user.getUserRoles()) {
            String codigo = ur.getRole() != null ? ur.getRole().getCode() : null;
            if (codigo != null && ESTADO_ACTIVO.equalsIgnoreCase(ur.getStatus()) && !deseados.contains(codigo)) {
                ur.setStatus(ESTADO_INACTIVO);
                cambios = true;
            }
        }

        if (cambios) {
            userRepository.save(user);
            log.debug("[SSO] Roles sincronizados para {}: {}", user.getUsername(), deseados);
        }
    }

    private String normalizarCodigo(String rol) {
        String r = rol.trim();
        return r.startsWith(ROLE_PREFIX) ? r.substring(ROLE_PREFIX.length()) : r;
    }

    private String emailSintetico(String username) {
        String base = username.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
        if (base.isBlank()) {
            base = "sso";
        }
        return base + "@sso.indeci.gob.pe";
    }

    private String passwordAleatoria() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return "SSO_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
