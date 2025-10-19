package es.ujaen.dae.incidenciasUrbanas.entidades;

import es.ujaen.dae.incidenciasUrbanas.excepciones.PasswordIncorrecta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;


public class Usuario {
    Integer id;

    @NotBlank
    String nombre;

    @NotBlank
    String apellidos;

    @NotBlank
    LocalDate fechaNacimiento;

    @NotBlank
    String direccion;

    @Pattern(regexp = "^(\\+34|0034|34)?[6789]\\d{8}$", message = "No es un número de teléfono válido")
    String telefono;

    @Email
    String email;

    @NotBlank
    String login;

    @NotBlank
    String clave;

    public Usuario(int id, String nombre, String apellidos, LocalDate fechaNacimiento, String direccion, String telefono, String email, String login, String clave) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.login = login;
        this.clave = clave;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    //Método según UML
    public void cambiarClave(String claveActual, String nuevaClave) {
        if (!this.clave.equals(claveActual)) {
            throw new PasswordIncorrecta();
        }
        this.clave = nuevaClave;
    }


}
