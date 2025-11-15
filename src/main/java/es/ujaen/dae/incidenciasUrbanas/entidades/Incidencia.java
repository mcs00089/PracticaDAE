package es.ujaen.dae.incidenciasUrbanas.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Incidencia {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotNull
    private LocalDateTime fecha;

    @NotBlank
    private String descripcion;

    @NotBlank
    private String localizacion;

    @NotBlank
    private String localizacionGPS;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Estado estado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Usuario usuario; // quien la registra

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TipoIncidencia tipo; // tipo de incidencia

    public Incidencia(Usuario usuario,
                      TipoIncidencia tipo,
                      String descripcion,
                      String localizacion,
                      String localizacionGPS) {
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

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
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

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoIncidencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoIncidencia tipo) {
        this.tipo = tipo;
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
