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

    @PostMapping
    public ResponseEntity<DIncidencia> crearIncidencia(@RequestBody DNuevaIncidencia dNueva) {
        try {
            // Obtenemos el usuario autenticado
            String login = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Usuario usuario = servicio.buscarUsuario(login).orElseThrow(UsuarioNoEncontrado::new);

            TipoIncidencia tipoAux = new TipoIncidencia();


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


            List<Incidencia> todas = servicio.listarIncidenciasDeUsuario(usuario);


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