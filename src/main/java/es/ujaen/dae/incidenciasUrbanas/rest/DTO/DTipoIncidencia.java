package es.ujaen.dae.incidenciasUrbanas.rest.DTO;

import jakarta.validation.constraints.NotBlank;

public record DTipoIncidencia(
        Integer id,
        @NotBlank String nombre,
        @NotBlank String descripcion
) {}
