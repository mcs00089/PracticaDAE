package es.ujaen.dae.incidenciasUrbanas.servicios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.CredencialesInvalidas;
import es.ujaen.dae.incidenciasUrbanas.excepciones.UsuarioNoEncontrado;
import es.ujaen.dae.incidenciasUrbanas.excepciones.UsuarioYaExiste;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;


@Service
@Validated
public class ServicioIncidencias {
    private final Map<String, Usuario> usuarios = new TreeMap<>();


    private static final Usuario admin = new Usuario(0,"Admin", "Administrador", LocalDate.of(1990, 1, 1),
            "Ayuntamiento, Plaza Mayor",
            "657232313",
            "admin@ayuntamiento.es",
            "admin",
            "admin123"
    );


    public void registrarUsuario(Usuario usuario){
        if (usuarios.containsKey(usuario.getLogin())) {
            throw new UsuarioYaExiste();
        }
        usuarios.put(usuario.getLogin(), usuario);
    }

    public Usuario login(String login, String clave){
        Usuario usuario = usuarios.get(login);
        if (usuario == null || !usuario.getClave().equals(clave)) {
            throw new CredencialesInvalidas();
        }
        return usuario;
    }

    public void actualizarUsuario(String login, Usuario nuevosDatos) {
        Usuario usuActualizar = usuarios.get(login);
        if (usuActualizar == null) {
            throw new UsuarioNoEncontrado();
        }

        usuActualizar.setNombre(nuevosDatos.getNombre());
        usuActualizar.setApellidos(nuevosDatos.getApellidos());
        usuActualizar.setEmail(nuevosDatos.getEmail());
        usuActualizar.setDireccion(nuevosDatos.getDireccion());
        usuActualizar.setTelefono(nuevosDatos.getTelefono());
        usuActualizar.setFechaNacimiento(nuevosDatos.getFechaNacimiento());
        usuActualizar.setClave(nuevosDatos.getClave());

    }
}
