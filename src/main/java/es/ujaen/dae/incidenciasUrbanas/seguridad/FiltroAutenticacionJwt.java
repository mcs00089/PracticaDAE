package es.ujaen.dae.incidenciasUrbanas.seguridad;

import es.ujaen.dae.incidenciasUrbanas.util.UtilJwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;


public class FiltroAutenticacionJwt extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            Claims claims = null;
            try {

                claims = UtilJwt.extraerContenido(authorizationHeader.substring(7));
            }
            catch (JwtException e) {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Introducir la autenticación en Spring Security
            var authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get("roles", String.class));

            var authenticationToken = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
