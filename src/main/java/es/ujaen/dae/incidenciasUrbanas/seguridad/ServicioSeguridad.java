package es.ujaen.dae.incidenciasUrbanas.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class ServicioSeguridad {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConf) throws Exception {
        return authConf.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.disable())
                .addFilterAfter(new FiltroAutenticacionJwt(),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        // Registro y login
                        .requestMatchers(HttpMethod.POST, "/incidencias/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/incidencias/autenticacion").permitAll()

                        // Tipos (públicos)
                        .requestMatchers(HttpMethod.GET, "/incidencias/tipos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/incidencias/tipos/{id}").permitAll()

                        // Usuario (solo su propio login)
                        .requestMatchers(HttpMethod.GET, "/incidencias/usuarios/{login}")
                        .access(new WebExpressionAuthorizationManager("#login == principal"))

                        .requestMatchers(HttpMethod.PUT, "/incidencias/usuarios/{login}")
                        .access(new WebExpressionAuthorizationManager("#login == principal"))

                        // Admin
                        .requestMatchers(HttpMethod.POST, "/incidencias/tipos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/incidencias/tipos/{id}").hasRole("ADMIN")

                        // --- NUEVAS REGLAS PARA INCIDENCIAS ---
                        // Admin puede crear tipos
                        .requestMatchers(HttpMethod.POST, "/incidencias/tipos").hasRole("ADMIN")
                        // Admin puede borrar tipos
                        .requestMatchers(HttpMethod.DELETE, "/incidencias/tipos/**").hasRole("ADMIN")

                        // Cualquier usuario autenticado puede ver tipos, crear incidencias y ver las suyas
                        .requestMatchers("/incidencias/**").authenticated()

                        .anyRequest().authenticated()
                )
                .build();
    }

}
