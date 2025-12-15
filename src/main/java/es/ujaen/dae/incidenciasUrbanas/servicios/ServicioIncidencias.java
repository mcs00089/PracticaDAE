package es.ujaen.dae.incidenciasUrbanas.servicios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Estado;
import es.ujaen.dae.incidenciasUrbanas.entidades.Incidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.*;
import es.ujaen.dae.incidenciasUrbanas.repositorios.RepositorioIncidencias;
import es.ujaen.dae.incidenciasUrbanas.repositorios.RepositorioTipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.repositorios.RepositorioUsuarios;
import es.ujaen.dae.incidenciasUrbanas.util.GeoUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class ServicioIncidencias {

    @Autowired
    private RepositorioUsuarios repositorioUsuario;

    @Autowired
    private RepositorioTipoIncidencia repositorioTipos;

    @Autowired
    private RepositorioIncidencias repositorioIncidencias;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario admin;

    @PostConstruct
    @Transactional
    public void initAdmin() {
        if (!repositorioUsuario.existeLogin("admin")) {
            admin = new Usuario(
                    "Admin", "Administrador", LocalDate.of(1990, 1, 1),
                    "Ayuntamiento, Plaza Mayor", "657232313",
                    "admin@ayuntamiento.es", "admin",
                    passwordEncoder.encode("admin123")
            );
            repositorioUsuario.guardar(admin);
        } else {
            admin = repositorioUsuario.buscarPorLogin("admin").orElseThrow();
        }
    }

    public void registrarUsuario(@Valid Usuario usuario) {
        if (usuario.getLogin().equals("admin")) {
            throw new UsuarioNoAdmin();
        }

        if (repositorioUsuario.existeLogin(usuario.getLogin())) {
            throw new UsuarioYaExiste();
        }

        repositorioUsuario.guardar(usuario);
    }


    public Optional<Usuario> login(@NotBlank String login, @NotBlank String clave) {
        Optional<Usuario> usuarioOpt = repositorioUsuario.buscarPorLogin(login);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();
        if (!passwordEncoder.matches(clave, usuario.getClave())) {
            return Optional.empty();
        }

        return Optional.of(usuario);
    }

    @Transactional
    public void actualizarUsuario(Usuario usuarioLogueado, Usuario nuevosDatos) {

        Usuario usuActualizar = repositorioUsuario.buscarPorLoginConBloqueo(usuarioLogueado.getLogin());

        usuActualizar.setNombre(nuevosDatos.getNombre());
        usuActualizar.setApellidos(nuevosDatos.getApellidos());
        usuActualizar.setDireccion(nuevosDatos.getDireccion());
        usuActualizar.setTelefono(nuevosDatos.getTelefono());
        usuActualizar.setFechaNacimiento(nuevosDatos.getFechaNacimiento());

        if (nuevosDatos.getClave() != null && !nuevosDatos.getClave().isEmpty()) {
            usuActualizar.setClave(passwordEncoder.encode(nuevosDatos.getClave()));
        }
    }

    /**
     * Busca un usuario por email o login
     */
    public Optional<Usuario> buscarUsuario(String Login) {
        if (Login.equals(admin.getLogin()))
            return Optional.of(admin);

        Optional<Usuario> porEmail = repositorioUsuario.buscarPorLogin(Login);
        if (porEmail.isPresent()) {
            return porEmail;
        }

        return repositorioUsuario.buscarPorLogin(Login);
    }


    public List<Usuario> listarUsuarios() {
        return repositorioUsuario.buscarTodos();
    }

    public Incidencia registrarIncidencia(@Valid Usuario usuario, @Valid TipoIncidencia tipoInci,
                                          String descripcion, String localizacion, String gps,
                                          byte[] foto) {
        if (usuario == null || !repositorioUsuario.existeLogin(usuario.getLogin())) {
            throw new UsuarioNoEncontrado();
        }

        TipoIncidencia tipo = repositorioTipos.buscarPorId(tipoInci.getId())
                .orElseThrow(TipoIncidenciaNoencontrado::new);

        Incidencia nueva = new Incidencia(usuario, tipo, descripcion, localizacion, gps, foto);
        return repositorioIncidencias.guardar(nueva);
    }

    public List<Incidencia> listarIncidenciasDeUsuario(@Valid Usuario usuario) {
        if (usuario == null || !repositorioUsuario.existeLogin(usuario.getLogin())) {
            throw new UsuarioNoEncontrado();
        }

        return repositorioIncidencias.buscarPorUsuario(usuario);
    }

    public List<Incidencia> buscarIncidencias(TipoIncidencia tipoIncidencia, Estado estado) {
        return repositorioIncidencias.buscarPorTipoYEstado(tipoIncidencia, estado);
    }

    @Transactional
    public void borrarIncidencia(Usuario usuario, Incidencia incidencia) {

        if (usuario == null) throw new UsuarioNoEncontrado();

        Usuario usuSistema = repositorioUsuario
                .buscarPorLoginConBloqueo(usuario.getLogin());

        Incidencia incSistema = repositorioIncidencias
                .buscarPorIdConBloqueo(incidencia.getId());

        boolean esAdmin = usuSistema.getLogin().equals("admin");

        if (esAdmin || incSistema.getUsuario().getLogin().equals(usuSistema.getLogin())) {
            if (esAdmin || incSistema.getEstado() == Estado.PENDIENTE) {
                repositorioIncidencias.borrar(incSistema);
            } else {
                throw new BorrarIncidenciaNoPendiente();
            }
        } else {
            throw new CredencialesInvalidas();
        }
    }

    public void anadirTipoIncidencia(@Valid Usuario usuario, @Valid TipoIncidencia tipo) {
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

        if (!usuario.getLogin().equals("admin"))
            throw new CredencialesInvalidas();

        TipoIncidencia tipoSistema = repositorioTipos.buscarPorIdConBloqueo(tipo.getId());

        if (repositorioIncidencias.contarPorTipo(tipoSistema) > 0) {
            throw new TipoIncidenciaEnUso();
        }

        repositorioTipos.borrar(tipoSistema);
    }

    @Transactional
    public void cambiarEstadoIncidencia(Usuario usuario,
                                        Incidencia incidencia,
                                        Estado nuevoEstado) {

        if (!usuario.getLogin().equals("admin"))
            throw new CredencialesInvalidas();

        Incidencia inc = repositorioIncidencias.buscarPorIdConBloqueo(incidencia.getId());

        inc.setEstado(nuevoEstado);
    }

    public List<TipoIncidencia> listarTiposIncidencias() {
        return repositorioTipos.buscarTodas();
    }

    @Transactional
    public List<Incidencia> incidenciasCercanas(String localizacionGPS) {
        String[] partes = localizacionGPS.split(",");
        if (partes.length != 2) return List.of();

        double latReferencia = Double.parseDouble(partes[0]);
        double lonReferencia = Double.parseDouble(partes[1]);

        List<Incidencia> incidencias = repositorioIncidencias.buscarPorEstados(
                List.of(Estado.PENDIENTE, Estado.EN_EVALUACION)
        );

        return incidencias.stream()
                .filter(i -> {
                    String[] coord = i.getLocalizacionGPS().split(",");
                    if (coord.length != 2) return false;
                    double lat = Double.parseDouble(coord[0]);
                    double lon = Double.parseDouble(coord[1]);
                    return GeoUtil.distanciaEnMetros(latReferencia, lonReferencia, lat, lon) <= 10;
                })
                .toList();
    }
}
