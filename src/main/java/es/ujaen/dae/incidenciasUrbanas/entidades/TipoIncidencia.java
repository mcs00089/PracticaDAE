package es.ujaen.dae.incidenciasUrbanas.entidades;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;
import java.util.List;

@Entity
public class TipoIncidencia {

    // Atributos
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String descripcion;

    @Version
    private long version;

    // Constructor
    public TipoIncidencia(String nombre, String descripcion) {

        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public TipoIncidencia() {

    }

    public int getId() {
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

    @Override
    public String toString() {
        return "TipoIncidencia{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}

