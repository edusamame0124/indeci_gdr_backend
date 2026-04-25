package pe.gob.gdr.access.infrastructure.security;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.domain.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) {
        pe.gob.gdr.access.domain.model.User user = userRepository.findByLoginId(loginId.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new DisabledException("La cuenta se encuentra inactiva.");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException("La cuenta se encuentra bloqueada temporalmente.");
        }

        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .filter(userRole -> "ACTIVE".equalsIgnoreCase(userRole.getStatus()))
                .filter(userRole -> userRole.getRole() != null)
                .filter(userRole -> "ACTIVE".equalsIgnoreCase(userRole.getRole().getStatus()))
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getCode()))
                .toList();

        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}
