package es.ujaen.dae.incidenciasUrbanas.rest.DTO;

import java.time.LocalDate;

public record DUsuario(
        String nombre,
        String apellidos,
        LocalDate fechaNacimiento,
        String direccion,
        String telefono,
        String email,
        String login,
        String clave
) { }

