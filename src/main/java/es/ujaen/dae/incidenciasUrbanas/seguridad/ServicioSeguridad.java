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

                        .requestMatchers(HttpMethod.POST, "/incidencias/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/incidencias/autenticacion").permitAll()

                        .requestMatchers(HttpMethod.GET, "/incidencias/tipos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/incidencias/tipos/{id}").permitAll()

                        .requestMatchers(HttpMethod.POST, "/incidencias/tipos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/incidencias/tipos/{id}").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/incidencias/usuarios/{login}").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/incidencias/usuarios/{login}").authenticated()

                        .requestMatchers(HttpMethod.POST, "/incidencias").authenticated()
                        .requestMatchers(HttpMethod.GET, "/incidencias/propias").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/incidencias/{idIncidencia}").authenticated()

                        .requestMatchers(HttpMethod.POST, "/incidencias/{id}/foto").authenticated()
                        .requestMatchers(HttpMethod.GET, "/incidencias/{id}/foto").authenticated()

                        .anyRequest().authenticated()
                )
                .build();
    }
}
