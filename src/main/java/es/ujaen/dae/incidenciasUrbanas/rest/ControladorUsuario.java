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

    // =========================
    // Registro de usuario
    // =========================
    @PostMapping("/usuarios")
    public ResponseEntity<Void> nuevoUsuario(@RequestBody DUsuario usuario) {
        try {
            servicioIncidencias.registrarUsuario(
                    mapeador.entidadNueva(usuario)
            );
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UsuarioYaExiste e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // =========================
    // Obtener usuario por login
    // =========================
    @GetMapping("/usuarios/{login}")
    public ResponseEntity<DUsuario> obtenerUsuario(@PathVariable String login) {
        try {
            Usuario usuario = servicioIncidencias
                    .buscarUsuario(login)
                    .orElseThrow(UsuarioNoEncontrado::new);

            return ResponseEntity.ok(mapeador.dto(usuario));
        } catch (UsuarioNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // =========================
    // Actualizar usuario
    // =========================
    @PutMapping("/usuarios/{login}")
    public ResponseEntity<Void> actualizarUsuario(
            @PathVariable String login,
            @RequestBody DUsuario datosActualizados
    ) {
        try {
            String loginLogueado = (String) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            if (!login.equals(loginLogueado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Usuario usuarioActual = servicioIncidencias
                    .buscarUsuario(login)
                    .orElseThrow(UsuarioNoEncontrado::new);

            Usuario nuevosDatos = mapeador.entidad(datosActualizados);

            servicioIncidencias.actualizarUsuario(usuarioActual, nuevosDatos);

            return ResponseEntity.noContent().build();
        } catch (UsuarioNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
