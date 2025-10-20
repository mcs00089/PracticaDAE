package es.ujaen.dae.incidenciasUrbanas.entidades;
import es.ujaen.dae.incidenciasUrbanas.excepciones.TipoIncidenciaInvalido;

import java.time.LocalDateTime;
import java.util.UUID;

public class Incidencia {

    // Atributos
    private UUID id;
    private LocalDateTime fecha;
    private String descripcion;
    private String localizacion;
    private String localizacionGPS; // opcional
    private Estado estado;

    // Relaciones
    private Usuario usuario; // quien la registra


    private TipoIncidencia tipo; // tipo de incidencia

    // Constructor
    public Incidencia(Usuario usuario, TipoIncidencia tipo, String descripcion, String localizacion, String localizacionGPS) {
        this.id = UUID.randomUUID();
        this.fecha = LocalDateTime.now();
        this.usuario = usuario;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.localizacion = localizacion;
        this.localizacionGPS = localizacionGPS;
        this.estado = Estado.PENDIENTE; // estado inicial por defecto
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public String getLocalizacionGPS() {
        return localizacionGPS;
    }

    public void setLocalizacionGPS(String localizacionGPS) {
        this.localizacionGPS = localizacionGPS;
    }

    public Estado getEstado() {
        return estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoIncidencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoIncidencia tipo) {
        this.tipo = tipo;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    //Cambio de tipo de incidencia
    public void cambiarTipoIncidencia(TipoIncidencia nuevoTipo) {
        if (nuevoTipo == null) {
            throw new TipoIncidenciaInvalido();
        }
        this.tipo = nuevoTipo;
    }

    @Override
    public String toString() {
        return "Incidencia{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", descripcion='" + descripcion + '\'' +
                ", localizacion='" + localizacion + '\'' +
                ", localizacionGPS='" + localizacionGPS + '\'' +
                ", estado=" + estado +
                ", tipo=" + (tipo != null ? tipo.getNombre() : "Sin tipo") +
                ", usuario=" + (usuario != null ? usuario.getLogin() : "Desconocido") +
                '}';
    }
}

