package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.*;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DTipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.Mapeador;
import es.ujaen.dae.incidenciasUrbanas.servicios.ServicioIncidencias;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incidencias/tipos")
public class ControladorTipoIncidencia {

    @Autowired
    Mapeador mapeador;

    @Autowired
    ServicioIncidencias servicioIncidencias;

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ConstraintViolationException.class)
    public void mapeadoExcepcionConstraintViolationException() {}

    @PostMapping
    public ResponseEntity<Void> crearTipoIncidencia(@RequestBody DTipoIncidencia tipoDto) {
        try {
            String loginLogueado = (String) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            Usuario admin = servicioIncidencias.buscarUsuario(loginLogueado)
                    .orElseThrow(UsuarioNoEncontrado::new);

            TipoIncidencia tipo = mapeador.entidad(tipoDto);
            servicioIncidencias.anadirTipoIncidencia(admin, tipo);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (CredencialesInvalidas e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (TipoIncidenciaEnUso e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<DTipoIncidencia>> listarTiposIncidencia() {
        List<TipoIncidencia> tipos = servicioIncidencias.listarTiposIncidencias();
        List<DTipoIncidencia> tiposDto = tipos.stream()
                .map(mapeador::dto)
                .toList();
        return ResponseEntity.ok(tiposDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DTipoIncidencia> obtenerTipoIncidencia(@PathVariable Integer id) {
        List<TipoIncidencia> tipos = servicioIncidencias.listarTiposIncidencias();
        TipoIncidencia tipo = tipos.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);

        if (tipo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(mapeador.dto(tipo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarTipoIncidencia(@PathVariable Integer id) {
        try {
            String loginLogueado = (String) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            Usuario admin = servicioIncidencias.buscarUsuario(loginLogueado)
                    .orElseThrow(UsuarioNoEncontrado::new);

            List<TipoIncidencia> tipos = servicioIncidencias.listarTiposIncidencias();
            TipoIncidencia tipo = tipos.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElseThrow(TipoIncidenciaNoencontrado::new);

            servicioIncidencias.borrarTipoIncidencia(admin, tipo);

            return ResponseEntity.noContent().build();
        } catch (CredencialesInvalidas e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (TipoIncidenciaNoencontrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (TipoIncidenciaEnUso e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
