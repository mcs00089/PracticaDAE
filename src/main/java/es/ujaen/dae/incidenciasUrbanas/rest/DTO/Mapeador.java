package es.ujaen.dae.incidenciasUrbanas.rest.DTO;

import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Mapeador {

    @Autowired
    private PasswordEncoder codificadorClaves;

    public DUsuario dto(Usuario usuario) {
        return new DUsuario(
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getFechaNacimiento(),
                usuario.getDireccion(),
                usuario.getTelefono(),
                usuario.getEmail(),
                usuario.getLogin(),
                ""
        );
    }

    public Usuario entidad(DUsuario dUsuario) {
        return new Usuario(
                dUsuario.nombre(),
                dUsuario.apellidos(),
                dUsuario.fechaNacimiento(),
                dUsuario.direccion(),
                dUsuario.telefono(),
                dUsuario.email(),
                dUsuario.login(),
                dUsuario.clave()
        );
    }

    public Usuario entidadNueva(DUsuario dUsuario) {
        return new Usuario(
                dUsuario.nombre(),
                dUsuario.apellidos(),
                dUsuario.fechaNacimiento(),
                dUsuario.direccion(),
                dUsuario.telefono(),
                dUsuario.email(),
                dUsuario.login(),
                codificadorClaves.encode(dUsuario.clave())
        );
    }

    public DTipoIncidencia dto(TipoIncidencia tipo) {
        return new DTipoIncidencia(
                tipo.getId(),
                tipo.getNombre(),
                tipo.getDescripcion()
        );
    }

    public TipoIncidencia entidad(DTipoIncidencia dTipo) {
        return new TipoIncidencia(
                dTipo.nombre(),
                dTipo.descripcion()
        );
    }

}