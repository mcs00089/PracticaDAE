package es.ujaen.dae.incidenciasUrbanas.seguridad;

import es.ujaen.dae.incidenciasUrbanas.repositorios.RepositorioUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class DetallesUsuarioServicio implements UserDetailsService {

    @Autowired
    private RepositorioUsuarios repositorioUsuarios;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        var usuario = repositorioUsuarios.buscarPorLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + login));

        // Determinar el rol
        String rol = usuario.getLogin().equals("admin") ? "ROLE_ADMIN" : "ROLE_USER";

        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getClave())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(rol)))
                .build();
    }
}
