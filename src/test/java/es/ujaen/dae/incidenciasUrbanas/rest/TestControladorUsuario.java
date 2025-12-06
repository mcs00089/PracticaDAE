package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.app.IncidenciasUrbanas;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DAutenticacionUsuario;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DUsuario;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(
        classes = IncidenciasUrbanas.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestControladorUsuario {

    @LocalServerPort
    int localPort;

    TestRestTemplate restTemplate;

    static HttpHeaders headerAutorizacion(String token) {
        HttpHeaders h = new HttpHeaders();
        h.add("Authorization", "Bearer " + token);
        return h;
    }

    @PostConstruct
    void initRestTemplate() {
        var builder = new RestTemplateBuilder()
                .rootUri("http://localhost:" + localPort + "/incidencias");

        restTemplate = new TestRestTemplate(builder);
    }

    // Registro de usuario
    @Test
    @DirtiesContext
    void testRegistroUsuario() {
        DUsuario u1 = new DUsuario(
                "Juan", "Pérez", LocalDate.of(1990, 5, 10),
                "Calle A", "600123123",
                "juan@test.es", "juan", "clave123"
        );

        var r1 = restTemplate.postForEntity("/usuarios", u1, Void.class);

        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // intentamos registrar el mismo usuario
        var r2 = restTemplate.postForEntity("/usuarios", u1, Void.class);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // Login
    @Test
    @DirtiesContext
    void testLogin() {
        // registramos un usuario
        DUsuario u = new DUsuario(
                "Ana", "López", LocalDate.of(1988, 3, 15),
                "Calle B", "611111111",
                "ana@test.es", "ana", "abcd"
        );

        restTemplate.postForEntity("/usuarios", u, Void.class);

        // login incorrecto
        var r1 = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario("usuarioNoExiste", "abcd"),
                String.class
        );
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // clave mal
        var r2 = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario("ana", "badpass"),
                String.class
        );
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // login  correcto
        var r3 = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario("ana", "abcd"),
                String.class
        );
        assertThat(r3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r3.getBody()).isNotEmpty();
    }

    private String obtenerToken(String login, String clave) {
        return restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario(login, clave),
                String.class
        ).getBody();
    }

    // Obtener usuario
    @Test
    @DirtiesContext
    void testObtenerUsuario() {
        DUsuario u = new DUsuario(
                "Luis", "Martínez", LocalDate.of(1995, 7, 22),
                "Calle C", "622222222",
                "luis@test.es", "luis", "clave"
        );

        // Registramos un usuario
        restTemplate.postForEntity("/usuarios", u, Void.class);

        // Sin token prohibido
        var r1 = restTemplate.getForEntity("/usuarios/{login}", DUsuario.class, u.login());
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Obtener token
        String token = obtenerToken("luis", "clave");

        var req = RequestEntity
                .get("/usuarios/{login}", u.login())
                .headers(headerAutorizacion(token))
                .build();

        var r2 = restTemplate.exchange(req, DUsuario.class);

        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r2.getBody().email()).isEqualTo("luis@test.es");
        assertThat(r2.getBody().login()).isEqualTo("luis");
    }

    // Actualizar Usuario
    @Test
    @DirtiesContext
    void testActualizarUsuario() {
        // registramos
        DUsuario u = new DUsuario(
                "Sara", "Jiménez", LocalDate.of(2000, 1, 20),
                "Calle D", "633333333",
                "sara@test.es", "sara", "1234"
        );

        restTemplate.postForEntity("/usuarios", u, Void.class);

        String token = obtenerToken("sara", "1234");

        // Datos modificados
        DUsuario nuevo = new DUsuario(
                "Sara Nueva", "Jiménez Actualizado",
                LocalDate.of(2000, 1, 20),
                "Calle Nueva", "699999999",
                "sara@test.es",
                "sara",
                "1234"
        );

        var req = RequestEntity
                .put("/usuarios/{login}", u.login())
                .headers(headerAutorizacion(token))
                .body(nuevo);

        var r = restTemplate.exchange(req, Void.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Obtenemos el usuario para verificar si se ha cambiado
        var req2 = RequestEntity
                .get("/usuarios/{login}", u.login())
                .headers(headerAutorizacion(token))
                .build();

        var r2 = restTemplate.exchange(req2, DUsuario.class);

        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r2.getBody().direccion()).isEqualTo("Calle Nueva");
        assertThat(r2.getBody().nombre()).isEqualTo("Sara Nueva");
    }
}
