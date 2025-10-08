package es.ujaen.dae.incidenciasUrbanas.entidades;
import java.util.UUID;

public class TipoIncidencia {

    // Atributos
    private UUID id;
    private String nombre;
    private String descripcion;

    // Constructor
    public TipoIncidencia(String nombre, String descripcion) {
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
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

