package es.ujaen.dae.incidenciasUrbanas.rest.DTO;

public record DNuevaIncidencia(
        int idTipo,
        String descripcion,
        String localizacion,
        String localizacionGPS,
        byte[] foto
) {}