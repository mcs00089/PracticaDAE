package es.ujaen.dae.incidenciasUrbanas.entidades;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;
import java.util.List;

@Entity
public class TipoIncidencia {

    // Atributos
    @Id
    private UUID id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String descripcion;

    // Constructor
    public TipoIncidencia(String nombre, String descripcion) {
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public TipoIncidencia() {

    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Para mostrar información legible
    @Override
    public String toString() {
        return "TipoIncidencia{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}

