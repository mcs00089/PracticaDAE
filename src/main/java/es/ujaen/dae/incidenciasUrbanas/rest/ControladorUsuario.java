package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.excepciones.UsuarioYaExiste;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DUsuario;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.Mapeador;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.UsuarioNoEncontrado;
import es.ujaen.dae.incidenciasUrbanas.servicios.ServicioIncidencias;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/incidencias")
public class ControladorUsuario {

    @Autowired
    Mapeador mapeador;

    @Autowired
    ServicioIncidencias servicioIncidencias;

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ConstraintViolationException.class)
    public void mapeadoExcepcionConstraintViolationException() {}


    @PostMapping("/usuarios")
    public ResponseEntity<Void> nuevoUsuario(@RequestBody DUsuario usuario) {
        try {
            servicioIncidencias.registrarUsuario(mapeador.entidadNueva(usuario));
        }
        catch(UsuarioYaExiste e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/usuarios/{email}")
    public ResponseEntity<DUsuario> obtenerUsuario(@PathVariable String email) {
        try {
            Usuario usuario = servicioIncidencias.buscarUsuario(email).orElseThrow(UsuarioNoEncontrado::new);
            return ResponseEntity.ok(mapeador.dto(usuario));
        }
        catch(UsuarioNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/usuarios/{email}")
    public ResponseEntity<Void> actualizarUsuario(
            @PathVariable String email,
            @RequestBody DUsuario datosActualizados
    ) {
        try {
            String loginLogueado = (String) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            Usuario usuarioAActualizar = servicioIncidencias
                    .buscarUsuario(email)
                    .orElseThrow(UsuarioNoEncontrado::new);

            if (!usuarioAActualizar.getLogin().equals(loginLogueado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Usuario nuevosDatos = mapeador.entidad(datosActualizados);

            servicioIncidencias.actualizarUsuario(usuarioAActualizar, nuevosDatos);

            return ResponseEntity.noContent().build();
        }
        catch (UsuarioNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
