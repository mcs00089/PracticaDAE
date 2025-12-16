package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.CredencialesInvalidas;
import es.ujaen.dae.incidenciasUrbanas.excepciones.TipoIncidenciaEnUso;
import es.ujaen.dae.incidenciasUrbanas.excepciones.UsuarioNoEncontrado;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DTipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.Mapeador;
import es.ujaen.dae.incidenciasUrbanas.servicios.ServicioIncidencias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incidencias")
public class ControladorTipoIncidencia {

    @Autowired
    ServicioIncidencias servicio;

    @Autowired
    Mapeador mapeador;

    // GET /incidencias/tipos
    @GetMapping("/tipos")
    public List<DTipoIncidencia> listarTipos() {
        return servicio.listarTiposIncidencias().stream()
                .map(mapeador::dto)
                .toList();
    }

    // POST /incidencias/tipos  (solo admin)
    @PostMapping("/tipos")
    public ResponseEntity<Void> anadirTipo(@RequestBody DTipoIncidencia dTipo) {
        try {
            String login = (String) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            Usuario admin = servicio.buscarUsuario(login)
                    .orElseThrow(UsuarioNoEncontrado::new);

            servicio.anadirTipoIncidencia(admin, mapeador.entidad(dTipo));

            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (CredencialesInvalidas e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (TipoIncidenciaEnUso e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}

