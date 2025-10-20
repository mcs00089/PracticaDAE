package es.ujaen.dae.incidenciasUrbanas.servicios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.CredencialesInvalidas;
import es.ujaen.dae.incidenciasUrbanas.excepciones.UsuarioYaExiste;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

@SpringBootTest(classes = es.ujaen.dae.incidenciasUrbanas.app.IncidenciasUrbanas.class)
public class TestServiciosIncidencias {

    @Autowired
    ServicioIncidencias servicio;


    @Test
    @DirtiesContext
    void testRegistrarUsuarioDuplicado() {
        var usuario = new Usuario(
                1,
                "Juan",
                "Gómez",
                LocalDate.of(1990, 5, 20),
                "Calle Mayor 5",
                "600123456",
                "juan@example.com",
                "juanito",
                "clave123"
        );

        servicio.registrarUsuario(usuario);

        assertThatThrownBy(() -> servicio.registrarUsuario(usuario)).isInstanceOf(UsuarioYaExiste.class);
    }

    @Test
    @DirtiesContext
    void testLoginCorrectoEIncorrecto() {
        var usuario = new Usuario(
                2,
                "Manuel",
                "Cámara",
                LocalDate.of(1988, 3, 10),
                "Avda. Andalucía 10",
                "600987654",
                "manu@gmail.com",
                "manu3454",
                "1111"
        );

        servicio.registrarUsuario(usuario);

        // Prueba Login incorrecto
        assertThatThrownBy(() -> servicio.login("jose", "clave"))
                .isInstanceOf(CredencialesInvalidas.class);

        // Prueba contraseña mal
        assertThatThrownBy(() -> servicio.login("manu3454", "clavemal"))
                .isInstanceOf(CredencialesInvalidas.class);

        // Login correcto
        var usuarioLogueado = servicio.login("manu3454", "1111");
        assertThat(usuarioLogueado.getEmail()).isEqualTo("manu@gmail.com");
    }


    @Test
    @DirtiesContext
    void testActualizarUsuario() {
        var usuario = new Usuario(
                1,
                "Manuel",
                "Cámara",
                LocalDate.of(1985, 7, 15),
                "Avda. de Madrid",
                "600555444",
                "manu@gmail.com",
                "manucs",
                "manu123"
        );

        servicio.registrarUsuario(usuario);

        var nuevosDatos = new Usuario(
                1,
                "Manuel A",
                "Cámara López",
                LocalDate.of(1985, 7, 15),
                "Calle Sol 9",
                "657224217",
                "manuel2@gmail.com",
                "manucs", // LOGIN NO SE CAMBIA
                "123"
        );

        // Actualizamos
        servicio.actualizarUsuario("manucs", nuevosDatos);


        // Comprobamos si los datos han cambiado
        var actualizado = servicio.login("manucs", "123");

        assertThat(actualizado.getNombre()).isEqualTo("Manuel A");
        assertThat(actualizado.getApellidos()).isEqualTo("Cámara López");
        assertThat(actualizado.getDireccion()).isEqualTo("Calle Sol 9");
        assertThat(actualizado.getTelefono()).isEqualTo("657224217");
        assertThat(actualizado.getEmail()).isEqualTo("manuel2@gmail.com");
        assertThat(actualizado.getLogin()).isEqualTo("manucs");
        assertThat(actualizado.getClave()).isEqualTo("123");
    }

}
