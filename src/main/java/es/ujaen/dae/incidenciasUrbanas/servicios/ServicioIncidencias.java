package es.ujaen.dae.incidenciasUrbanas.servicios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Estado;
import es.ujaen.dae.incidenciasUrbanas.entidades.Incidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.*;
import es.ujaen.dae.incidenciasUrbanas.repositorios.RepositorioIncidencias;
import es.ujaen.dae.incidenciasUrbanas.repositorios.RepositorioTipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.repositorios.RepositorioUsuarios;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class ServicioIncidencias {

    @Autowired
    private RepositorioUsuarios repositorioUsuario;

    @Autowired
    private RepositorioTipoIncidencia repositorioTipos;

    @Autowired
    private RepositorioIncidencias repositorioIncidencias;

    // Admin por defecto (si queréis persistirlo en BD, se haría en otro sitio)
    private static final Usuario admin = new Usuario("Admin", "Administrador", LocalDate.of(1990, 1, 1),
            "Ayuntamiento, Plaza Mayor",
            "657232313",
            "admin@ayuntamiento.es",
            "admin",
            "admin123"
    );

    public ServicioIncidencias() {
        // Antes metías el admin en el mapa; ahora ya no hay mapas
    }

    // ================================================================
    // USUARIOS
    // ================================================================

    /**
     * Registra un nuevo usuario en el sistema.
     */
    @Transactional
    public void registrarUsuario(@Valid Usuario usuario) {
        if (usuario.getLogin().equals("admin")) {
            throw new UsuarioNoAdmin();
        }

        if (repositorioUsuario.existeLogin(usuario.getLogin())) {
            throw new UsuarioYaExiste();
        }

        repositorioUsuario.guardar(usuario);
    }

    /**
     * Inicia sesión con un usuario registrado.
     */
    public Optional<Usuario> login(@NotBlank String login, @NotBlank String clave) {
        Optional<Usuario> usuarioOpt = repositorioUsuario.buscarPorLogin(login);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();
        if (!usuario.getClave().equals(clave)) {
            return Optional.empty();
        }

        return Optional.of(usuario);
    }

    /**
     * Actualiza los datos de un usuario existente.
     */
    @Transactional
    public void actualizarUsuario(@Valid Usuario usuarioLogueado, @Valid Usuario nuevosDatos) {
        Usuario usuActualizar = repositorioUsuario.buscarPorLogin(usuarioLogueado.getLogin())
                .orElseThrow(UsuarioNoEncontrado::new);

        usuActualizar.setNombre(nuevosDatos.getNombre());
        usuActualizar.setApellidos(nuevosDatos.getApellidos());
        usuActualizar.setEmail(nuevosDatos.getEmail());
        usuActualizar.setDireccion(nuevosDatos.getDireccion());
        usuActualizar.setTelefono(nuevosDatos.getTelefono());
        usuActualizar.setFechaNacimiento(nuevosDatos.getFechaNacimiento());
        usuActualizar.setClave(nuevosDatos.getClave());

        repositorioUsuario.actualizar(usuActualizar);
    }

    // ================================================================
    // INCIDENCIAS
    // ================================================================

    /**
     * Registra una nueva incidencia.
     */
    @Transactional
    public Incidencia registrarIncidencia(Usuario usuario, TipoIncidencia tipoInci,
                                          String descripcion, String localizacion, String gps) {

        if (usuario == null || !repositorioUsuario.existeLogin(usuario.getLogin())) {
            throw new UsuarioNoEncontrado();
        }

        // Usuario desde BD
        Usuario usuarioBd = repositorioUsuario.buscarPorLogin(usuario.getLogin())
                .orElseThrow(UsuarioNoEncontrado::new);

        // Tipo desde BD
        TipoIncidencia tipo = repositorioTipos.buscarPorId(tipoInci.getId())
                .orElseThrow(TipoIncidenciaNoencontrado::new);

        Incidencia nueva = new Incidencia(usuarioBd, tipo, descripcion, localizacion, gps);

        // 👉 Ahora se guarda con el REPOSITORIO, no con el Map
        return repositorioIncidencias.guardar(nueva);
    }

    /**
     * Lista todas las incidencias de un usuario.
     */
    public List<Incidencia> listarIncidenciasDeUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new UsuarioNoEncontrado();
        }

        // Comprobamos que el usuario existe en BD
        repositorioUsuario.buscarPorLogin(usuario.getLogin())
                .orElseThrow(UsuarioNoEncontrado::new);

        // Tomamos todas las incidencias de BD y filtramos por login
        return repositorioIncidencias.buscarTodas().stream()
                .filter(i -> i.getUsuario() != null &&
                        i.getUsuario().getLogin().equals(usuario.getLogin()))
                .collect(Collectors.toList());
    }

    /**
     * Busca incidencias por tipo y/o estado.
     */
    public List<Incidencia> buscarIncidencias(TipoIncidencia tipoIncidencia, Estado estado) {
        return repositorioIncidencias.buscarTodas().stream()
                .filter(i -> (tipoIncidencia == null ||
                        (i.getTipo() != null && i.getTipo().getId().equals(tipoIncidencia.getId())))
                        && (estado == null || i.getEstado() == estado))
                .collect(Collectors.toList());
    }

    /**
     * Elimina una incidencia del sistema.
     */
    @Transactional
    public void borrarIncidencia(Usuario usuario, Incidencia incidencia) {
        if (usuario == null) throw new UsuarioNoEncontrado();
        if (incidencia == null) throw new IncidenciaNoEncontrada();

        Usuario usuSistema = repositorioUsuario.buscarPorLogin(usuario.getLogin())
                .orElseThrow(UsuarioNoEncontrado::new);

        Incidencia incSistema = repositorioIncidencias.buscarPorId(incidencia.getId())
                .orElseThrow(IncidenciaNoEncontrada::new);

        boolean esAdmin = usuSistema.getLogin().equals("admin");

        if (esAdmin || (incSistema.getUsuario() != null &&
                incSistema.getUsuario().getLogin().equals(usuSistema.getLogin()))) {

            if (esAdmin || incSistema.getEstado() == Estado.PENDIENTE) {
                repositorioIncidencias.borrar(incSistema);
            } else {
                throw new BorrarIncidenciaNoPendiente();
            }
        } else {
            throw new CredencialesInvalidas();
        }
    }

    /**
     * Cambia el estado de una incidencia.
     */
    @Transactional
    public void cambiarEstadoIncidencia(Usuario usuario, Incidencia incidencia, Estado nuevoEstado) {
        if (nuevoEstado == null) return;

        if (usuario == null) {
            throw new UsuarioNoEncontrado();
        }

        Usuario usuSistema = repositorioUsuario.buscarPorLogin(usuario.getLogin())
                .orElseThrow(UsuarioNoEncontrado::new);

        if (!usuSistema.getLogin().equals("admin")) {
            throw new CredencialesInvalidas();
        }

        Incidencia incSistema = repositorioIncidencias.buscarPorId(incidencia.getId())
                .orElseThrow(IncidenciaNoEncontrada::new);

        incSistema.setEstado(nuevoEstado);
        repositorioIncidencias.actualizar(incSistema);
    }

    // ================================================================
    // TIPOS DE INCIDENCIA
    // ================================================================

    public void anadirTipoIncidencia(Usuario usuario, TipoIncidencia tipo) {
        if (usuario == null || !usuario.getLogin().equals("admin")) {
            throw new CredencialesInvalidas();
        }

        if (tipo == null) {
            throw new TipoIncidenciaInvalido();
        }

        Optional<TipoIncidencia> existente = repositorioTipos.buscarPorNombre(tipo.getNombre());
        if (existente.isPresent()) {
            throw new TipoIncidenciaEnUso();
        }

        repositorioTipos.guardar(tipo);
    }

    @Transactional
    public void borrarTipoIncidencia(Usuario usuario, TipoIncidencia tipo) {
        if (usuario == null || !usuario.getLogin().equals("admin")) {
            throw new CredencialesInvalidas();
        }

        TipoIncidencia tipoSistema = repositorioTipos.buscarPorId(tipo.getId())
                .orElseThrow(TipoIncidenciaNoencontrado::new);

        // Comprobamos en incidencias si ese tipo está en uso (ya desde BD)
        boolean enUso = repositorioIncidencias.buscarTodas().stream()
                .anyMatch(i -> i.getTipo() != null &&
                        i.getTipo().getId().equals(tipo.getId()));

        if (enUso) {
            throw new TipoIncidenciaEnUso();
        }

        repositorioTipos.borrar(tipoSistema);
    }
}
