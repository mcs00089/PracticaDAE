package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.app.IncidenciasUrbanas;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.*;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = IncidenciasUrbanas.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class TestControladorTipoIncidencia {

    @LocalServerPort
    int localPort;

    TestRestTemplate restTemplate;

    @PostConstruct
    void initRestTemplate() {
        // La raíz ya incluye /incidencias
        var builder = new RestTemplateBuilder()
                .rootUri("http://localhost:" + localPort + "/incidencias");
        restTemplate = new TestRestTemplate(builder);
    }

    // ---------- MÉTODOS AUXILIARES ----------

    private String obtenerToken(String login, String clave) {
        var resp = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario(login, clave),
                String.class
        );
        return resp.getBody();
    }

    private HttpHeaders headerAuth(String token) {
        HttpHeaders h = new HttpHeaders();
        h.add("Authorization", "Bearer " + token);
        return h;
    }

    // ---------- SETUP ----------

    @BeforeEach
    void setUp() {
        // No hace falta crear usuarios:
        // admin ya existe por @PostConstruct del servicio
    }

    // ---------- TESTS ----------

    @Test
    @DirtiesContext
    void testCrearYListarTiposComoAdmin() {
        String tokenAdmin = obtenerToken("admin", "admin123");

        // 1. Creamos un tipo de incidencia
        DTipoIncidencia nuevo = new DTipoIncidencia(
                0,
                "Semáforo roto",
                "No cambia de color"
        );

        var reqCrear = new RequestEntity<>(
                nuevo,
                headerAuth(tokenAdmin),
                HttpMethod.POST,
                java.net.URI.create("/tipos")
        );

        var respCrear = restTemplate.exchange(reqCrear, Void.class);

        // Verificamos creación
        assertThat(respCrear.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. Listamos los tipos
        var reqListar = new RequestEntity<>(
                headerAuth(tokenAdmin),
                HttpMethod.GET,
                java.net.URI.create("/tipos")
        );

        var respListar = restTemplate.exchange(
                reqListar,
                new ParameterizedTypeReference<List<DTipoIncidencia>>() {}
        );

        // Verificamos listado
        assertThat(respListar.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respListar.getBody()).isNotEmpty();
        assertThat(
                respListar.getBody().stream()
                        .anyMatch(t -> t.nombre().equals("Semáforo roto"))
        ).isTrue();
    }

    @Test
    @DirtiesContext
    void testCrearTipoComoUsuarioNoAdmin() {
        // Creamos un usuario normal
        DUsuario usuario = new DUsuario(
                "Juan", "Perez",
                java.time.LocalDate.now(),
                "Calle X",
                "600000001",
                "juan@test.com",
                "juan",
                "clave"
        );

        try {
            restTemplate.postForEntity("/usuarios", usuario, Void.class);
        } catch (Exception ignored) {}

        String tokenUsuario = obtenerToken("juan", "clave");

        DTipoIncidencia nuevo = new DTipoIncidencia(
                0,
                "Contenedor lleno",
                "No recogen la basura"
        );

        var reqCrear = new RequestEntity<>(
                nuevo,
                headerAuth(tokenUsuario),
                HttpMethod.POST,
                java.net.URI.create("/tipos")
        );

        var respCrear = restTemplate.exchange(reqCrear, Void.class);

        // Un usuario normal NO puede crear tipos
        assertThat(respCrear.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
