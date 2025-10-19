package es.ujaen.dae.incidenciasUrbanas.entidades;
import java.util.UUID;
import java.util.List;

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

    //meto un boolean porque ahora mismo no tenemos lista de tipos de excepción
    public static boolean eliminarTipo(TipoIncidencia tipo, List<Incidencia> incidencias) {
        // Comprobar si alguna incidencia usa ese tipo
        for (Incidencia inc : incidencias) {
            if (inc.getTipo().equals(tipo)) {
                System.out.println("No se puede eliminar: el tipo está siendo usado por una incidencia.");
                return false;
            }
        }
        // Si llegamos aquí, el tipo no está en uso
        System.out.println("Tipo de incidencia eliminado correctamente.");
        return true;
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

