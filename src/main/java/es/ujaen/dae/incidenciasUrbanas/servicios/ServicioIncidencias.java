package es.ujaen.dae.incidenciasUrbanas.servicios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Estado;
import es.ujaen.dae.incidenciasUrbanas.entidades.Incidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Validated
public class ServicioIncidencias {
    private final Map<String, Usuario> usuarios = new TreeMap<>();
    private final Map<UUID, Incidencia> incidencias = new HashMap<>();
    private final Map<UUID, TipoIncidencia> tiposIncidencia = new HashMap<>();


    private static final Usuario admin = new Usuario(0,"Admin", "Administrador", LocalDate.of(1990, 1, 1),
            "Ayuntamiento, Plaza Mayor",
            "657232313",
            "admin@ayuntamiento.es",
            "admin",
            "admin123"
    );


    /**
     * @brief Registra un nuevo usuario en el sistema.
     * Añade el usuario al mapa de usuarios registrados
     * @param usuario Objeto de tipo Usuario con los datos del nuevo usuario a registrar.
     * @throws UsuarioYaExiste Si ya existe un usuario con el mismo login.
     */
    public void registrarUsuario(Usuario usuario){
        if (usuarios.containsKey(usuario.getLogin())) {
            throw new UsuarioYaExiste();
        }

        if(usuario.getLogin().equals("admin")){
            throw new UsuarioNoAdmin();
        }

        usuarios.put(usuario.getLogin(), usuario);
    }


    /**
     * @brief Inicia sesión con un usuario registrado.
     * Busca el usuario por su login y comprueba que la contraseña sea correcta.
     * @param login Login de usuario.
     * @param clave Contraseña del usuario.
     * @return El objeto Usuario autenticado.
     * @throws CredencialesInvalidas Si el login no existe o la contraseña no coincide.
     */
    public Usuario login(String login, String clave){
        Usuario usuario = usuarios.get(login);
        if (usuario == null || !usuario.getClave().equals(clave)) {
            throw new CredencialesInvalidas();
        }
        return usuario;
    }


    /**
     * @brief Actualiza los datos de un usuario existente.
     * Reemplaza los datos del usuario con los nuevos valores.
     * @param login Login del usuario que se desea actualizar.
     * @param nuevosDatos Usuario con los nuevos valores de los campos a modificar.
     * @throws UsuarioNoEncontrado Si el usuario con el login especificado no existe.
     */
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


    /**
     * @brief Registra una nueva incidencia de un usuario.
     * Crea una nueva incidencia asociada al usuario y al tipo de incidencia indicado,
     * y la añade al sistema.
     * @param login Login del usuario que hace la incidencia.
     * @param idTipoIncidencia Identificador único del tipo de incidencia.
     * @param descripcion Descripción detallada del problema.
     * @param localizacion Dirección o zona donde ocurre la incidencia.
     * @param gps Coordenadas GPS del lugar de la incidencia.
     * @return La incidencia recién creada.
     * @throws UsuarioNoEncontrado Si el usuario con el login indicado no existe.
     * @throws TipoIncidenciaNoencontrado Si el tipo de incidencia indicado no existe.
     */
    public Incidencia registrarIncidencia(String login, UUID idTipoIncidencia, String descripcion, String localizacion, String gps) { // TODO OBJETOS completos
        Usuario usuario = usuarios.get(login);
        if (usuario == null) {
            throw new UsuarioNoEncontrado();
        }

        TipoIncidencia tipo = tiposIncidencia.get(idTipoIncidencia);
        if (tipo == null) {
            throw new TipoIncidenciaNoencontrado();
        }

        Incidencia nueva = new Incidencia(usuario, tipo, descripcion, localizacion, gps);
        incidencias.put(nueva.getId(), nueva);
        return nueva;
    }


    /**
     * @brief Lista todas las incidencias registradas por un usuario.
     * Filtra las incidencias en el sistema y devuelve solo las que pertenecen al usuario con el login indicado.
     * @param login Login del usuario cuyas incidencias se desean consultar.
     * @return Lista de incidencias asociadas al usuario.
     */
    public List<Incidencia> listarIncidenciasDeUsuario(String login) {
        return incidencias.values().stream()
                .filter(i -> i.getUsuario().getLogin().equals(login))
                .collect(Collectors.toList());
    }

    /**
     * @brief Busca incidencias en el sistema según tipo y/o estado.
     * Permite filtrar las incidencias por tipo o por estado. Si alguno de los parámetros es null, se ignora ese filtro.
     * @param idTipoIncidencia Identificador del tipo de incidencia.
     * @param estado Estado de la incidencia.
     * @return Lista de incidencias que cumplen los criterios de búsqueda.
     */
    public List<Incidencia> buscarIncidencias(UUID idTipoIncidencia, Estado estado) { //TODO Pasar objetos no UUID
        return incidencias.values().stream()
                .filter(i -> (idTipoIncidencia == null || i.getTipo().getId().equals(idTipoIncidencia)) &&
                        (estado == null || i.getEstado() == estado))
                .collect(Collectors.toList());
    }


    /**
     * @brief Elimina una incidencia del sistema.
     * Permite borrar una incidencia si el usuario es su propietario y la incidencia está en estado PENDIENTE, o si el usuario tiene el rol de administrador.
     *
     * @param usuario Login del usuario que solicita el borrado.
     * @param incidencia Identificador de la incidencia a eliminar.
     *
     * @throws IncidenciaNoEncontrada Si la incidencia indicada no existe.
     * @throws UsuarioNoEncontrado Si el usuario no existe.
     * @throws BorrarIncidenciaNoPendiente Si el usuario intenta borrar una incidencia no pendiente.
     * @throws CredencialesInvalidas Si el usuario no tiene permiso para eliminar la incidencia.
     */
    public void borrarIncidencia(Usuario usuario, Incidencia incidencia) {
        if (usuario == null) throw new UsuarioNoEncontrado();
        if (incidencia == null) throw new IncidenciaNoEncontrada();

        Incidencia incSistema = incidencias.get(incidencia.getId());
        if (incSistema == null) throw new IncidenciaNoEncontrada();

        Usuario usuSistema = usuarios.get(usuario.getLogin());
        if (usuSistema == null) throw new UsuarioNoEncontrado();

        boolean esAdmin = usuario.getLogin().equals("admin");

        if (esAdmin || incSistema.getUsuario().getLogin().equals(usuario.getLogin())) {
            if (esAdmin || incSistema.getEstado() == Estado.PENDIENTE) {
                incidencias.remove(incSistema.getId());
            } else {
                throw new BorrarIncidenciaNoPendiente();
            }
        } else {
            throw new CredencialesInvalidas();
        }
    }

    public void cambiarTipoIncidencia(TipoIncidencia nuevoTipo, Incidencia incidencia) {
        if (nuevoTipo == null) {
            throw new TipoIncidenciaInvalido();
        }
        incidencia.setTipo(nuevoTipo);
    }

    public void borrarTipoIncidencia(Usuario usuario, TipoIncidencia tipo) {
        if (usuario == null) throw new UsuarioNoEncontrado();
        if (tipo == null) throw new TipoIncidenciaNoencontrado();

        Usuario usuSistema = usuarios.get(usuario.getLogin());
        if (usuSistema == null) {
            throw new UsuarioNoEncontrado();
        }

        if (!usuario.getLogin().equals("admin")) {
            throw new CredencialesInvalidas();
        }

        TipoIncidencia tipoSistema = tiposIncidencia.get(tipo.getId());
        if (tipoSistema == null) {
            throw new TipoIncidenciaNoencontrado();
        }

        boolean enUso = incidencias.values().stream()
                .anyMatch(i -> i.getTipo().getId().equals(tipo.getId()));

        if (enUso) {
            throw new TipoIncidenciaEnUso();
        }

        tiposIncidencia.remove(tipo.getId());
    }


}
