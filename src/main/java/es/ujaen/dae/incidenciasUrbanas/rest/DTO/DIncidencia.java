package es.ujaen.dae.incidenciasUrbanas.rest.DTO;

import es.ujaen.dae.incidenciasUrbanas.entidades.Estado;
import java.time.LocalDateTime;

public record DIncidencia(
        int id,
        String usuario, // Login del usuario
        String tipo,    // Nombre del tipo
        String descripcion,
        String localizacion,
        String localizacionGPS,
        LocalDateTime fecha,
        Estado estado
) {}