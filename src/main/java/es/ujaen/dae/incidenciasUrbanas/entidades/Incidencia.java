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
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

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

    @ManyToOne
    private Usuario usuario; // quien la registra

    @ManyToOne
    private TipoIncidencia tipo; // tipo de incidencia

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] foto;

    @Version
    private long version;

    public Incidencia(Usuario usuario,
                      TipoIncidencia tipo,
                      String descripcion,
                      String localizacion,
                      String localizacionGPS,byte[] foto) {
        this.fecha = LocalDateTime.now();
        this.usuario = usuario;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.localizacion = localizacion;
        this.localizacionGPS = localizacionGPS;
        this.estado = Estado.PENDIENTE; // estado inicial por defecto
        this.foto = foto;
    }

    public Incidencia() {
    }

    public int getId() {
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

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
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
