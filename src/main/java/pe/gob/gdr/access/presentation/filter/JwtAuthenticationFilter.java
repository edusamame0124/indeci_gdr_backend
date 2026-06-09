package pe.gob.gdr.access.presentation.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.gob.gdr.access.application.service.SsoUserProvisioningService;
import pe.gob.gdr.access.infrastructure.security.CustomUserDetailsService;
import pe.gob.gdr.access.infrastructure.security.JwtTokenProvider;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final SsoUserProvisioningService ssoUserProvisioningService;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService,
            SsoUserProvisioningService ssoUserProvisioningService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.ssoUserProvisioningService = ssoUserProvisioningService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (StringUtils.hasText(token)
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtTokenProvider.validateToken(token)) {
            boolean sso = jwtTokenProvider.isSsoToken(token);
            // SSO Fase 3: el token del SISRH no trae el claim type=="access", así
            // que se acepta por ser SSO; el token propio de GDR sigue exigiendo access.
            if (sso || jwtTokenProvider.isAccessToken(token)) {
                String username = jwtTokenProvider.extractSubject(token);
                UsernamePasswordAuthenticationToken authenticationToken;
                if (sso) {
                    // Authorities desde el claim sistemas.rendimiento (vienen SIN
                    // prefijo; GDR antepone ROLE_). Principal = String username
                    // (todo el código usa auth.getName()).
                    List<String> rolesSso = jwtTokenProvider.getSistemaRoles(token, "rendimiento");
                    List<SimpleGrantedAuthority> authorities = rolesSso.stream()
                            .map(this::toAuthority)
                            .collect(Collectors.toList());
                    // SSO Fase 4: el token trae DNI, nombre y oficina (areas.rendimiento)
                    // para crear/vincular el HrPerson local.
                    String dni = jwtTokenProvider.getDni(token);
                    String nombre = jwtTokenProvider.getNombre(token);
                    String area = jwtTokenProvider.getSistemaArea(token, "rendimiento");
                    // JIT provisioning: la identidad vive en el SISRH, pero el negocio
                    // de GDR exige ficha local. Best-effort: si falla, se loguea y la
                    // autenticación procede igual.
                    try {
                        ssoUserProvisioningService.ensureLocalUser(username, rolesSso, dni, nombre, area);
                    } catch (Exception ex) {
                        logger.error("[SSO] Fallo al aprovisionar usuario local '" + username + "': " + ex.getMessage(), ex);
                    }
                    authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                } else {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                }
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private SimpleGrantedAuthority toAuthority(String role) {
        String r = role.trim();
        return new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
