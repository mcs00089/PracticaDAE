package es.ujaen.dae.incidenciasUrbanas.entidades;
import es.ujaen.dae.incidenciasUrbanas.excepciones.TipoIncidenciaInvalido;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Incidencia {

    // Atributos
    @Id
    private UUID id;

    @NotNull
    private LocalDateTime fecha;

    @NotBlank
    private String descripcion;

    @NotBlank
    private String localizacion;

    @NotBlank
    private String localizacionGPS;

    @NotBlank
    private Estado estado;

    @Transient
    private Usuario usuario; // quien la registra

    @Transient
    private TipoIncidencia tipo; // tipo de incidencia

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

    public Incidencia() {

    }

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

