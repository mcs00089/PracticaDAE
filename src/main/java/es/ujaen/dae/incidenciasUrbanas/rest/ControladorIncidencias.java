package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.entidades.Incidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.*;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.*;
import es.ujaen.dae.incidenciasUrbanas.servicios.ServicioIncidencias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incidencias")
public class ControladorIncidencias {

    @Autowired
    ServicioIncidencias servicio;

    @Autowired
    Mapeador mapeador;

    // --- TIPOS DE INCIDENCIA ---

    @GetMapping("/tipos")
    public List<DTipoIncidencia> listarTipos() {
        return servicio.listarTiposIncidencias().stream()
                .map(t -> mapeador.dto(t))
                .toList();
    }

    @PostMapping("/tipos")
    public ResponseEntity<Void> anadirTipo(@RequestBody DTipoIncidencia dTipo) {
        try {
            String login = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Usuario admin = servicio.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

            servicio.anadirTipoIncidencia(admin, mapeador.entidad(dTipo));
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (CredencialesInvalidas e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (TipoIncidenciaEnUso e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // --- INCIDENCIAS ---

    @PostMapping
    public ResponseEntity<DIncidencia> crearIncidencia(@RequestBody DNuevaIncidencia dNueva) {
        try {
            // Obtenemos el usuario autenticado
            String login = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Usuario usuario = servicio.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

            // Creamos un objeto TipoIncidencia auxiliar solo con el ID para que el servicio lo busque
            TipoIncidencia tipoAux = new TipoIncidencia();
            // Usamos reflexión o un setter si el ID no es accesible, pero asumiremos que el servicio lo busca por ID
            // Como el servicio usa tipoInci.getId(), necesitamos un constructor o setter.
            // Truco rápido: buscamos el tipo aquí o simulamos el objeto
            // Dado que en tu entidad TipoIncidencia el id es autogenerado y no tiene setter publico facil,
            // lo ideal es buscar el tipo antes o confiar en que el servicio lo busque.
            // *Corrección para tu código*: El servicio busca por ID.
            // Vamos a buscar el tipo aqui para pasarselo limpio al servicio o crear un objeto dummy.
            // La forma mas limpia dado tu servicio:

            // Creamos un tipo "dummy" sobrescribiendo el ID mediante reflexión o asumiendo que el servicio busca
            // Modificamos ligeramente la lógica: Buscamos el tipo real aquí para pasarlo.
            TipoIncidencia tipo = servicio.listarTiposIncidencias().stream()
                    .filter(t -> t.getId() == dNueva.idTipo())
                    .findFirst()
                    .orElseThrow(TipoIncidenciaNoencontrado::new);

            Incidencia creada = servicio.registrarIncidencia(
                    usuario,
                    tipo,
                    dNueva.descripcion(),
                    dNueva.localizacion(),
                    dNueva.localizacionGPS(),
                    dNueva.foto()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(mapeador.dto(creada));
        } catch (UsuarioNoEncontrado | TipoIncidenciaNoencontrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/propias")
    public List<DIncidencia> listarMisIncidencias() {
        String login = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = servicio.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

        return servicio.listarIncidenciasDeUsuario(usuario).stream()
                .map(i -> mapeador.dto(i))
                .toList();
    }

    @DeleteMapping("/{idIncidencia}")
    public ResponseEntity<Void> borrarIncidencia(@PathVariable int idIncidencia) {
        try {
            String login = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Usuario usuario = servicio.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

            // Creamos una incidencia dummy con el ID para pasarla al servicio
            // (El servicio la buscará en BDD por el ID)
            // Necesitamos acceder al ID. Como tu Entidad Incidencia tiene id privado sin setter,
            // el servicio debería tener un método borrarPorId, pero tiene borrar(Usuario, Incidencia).
            // Lo solucionamos buscando la incidencia primero.

            // Nota: Esto es un poco ineficiente (buscar para luego buscar dentro del servicio),
            // pero se ajusta a tu API actual.
            List<Incidencia> todas = servicio.listarIncidenciasDeUsuario(usuario);
            // Ojo: si es admin, esto de arriba solo lista las suyas.
            // Mejor intentar simular el objeto si es posible, o usar el repositorio si pudiéramos (pero estamos en controller).
            // Solución pragmática: Iteramos o asumimos que el servicio valida.

            // Truco: Como no podemos crear una incidencia con ID específico fácilmente desde fuera (JPA),
            // lo ideal sería añadir un método 'obtenerPorId' al servicio.
            // PERO, como no podemos tocar el servicio según instrucciones estrictas, haremos lo siguiente:
            // Usaremos reflexión para setear el ID en un objeto vacío, o mejor:
            // Como el servicio hace "repositorioIncidencias.buscarPorId(incidencia.getId())",
            // necesitamos pasarle un objeto que devuelva ese ID.

            Incidencia incDummy = new Incidencia();
            // Usamos reflexión para poner el ID privado (necesario pq no hay setter)
            java.lang.reflect.Field idField = Incidencia.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.setInt(incDummy, idIncidencia);

            servicio.borrarIncidencia(usuario, incDummy);

            return ResponseEntity.noContent().build();

        } catch (IncidenciaNoEncontrada e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (BorrarIncidenciaNoPendiente | CredencialesInvalidas e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}