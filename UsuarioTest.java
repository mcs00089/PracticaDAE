public package es.ujaen.dae.incidenciasUrbanas.entidades;

import es.ujaen.dae.incidenciasUrbanas.excepciones.PasswordIncorrecta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    void cambiarClave_ok_y_errorClaveActual() {
        var u = new Usuario(
                1, "Pedro", "ruiz",
                LocalDate.of(2000, 1, 1),
                "C/ Ejemplo 1",
                "600000000",
                "Pedro@example.com",
                "Pedro",
                "old"
        );

        u.cambiarClave("old", "new");
        assertEquals("new", u.getClave());

        assertThrows(PasswordIncorrecta.class, () -> u.cambiarClave("mal", "otra"));
    }
}
 {
    
}
