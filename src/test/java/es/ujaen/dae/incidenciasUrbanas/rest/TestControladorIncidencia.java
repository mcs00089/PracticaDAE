package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.app.IncidenciasUrbanas;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.*;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = IncidenciasUrbanas.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestControladorIncidencia {

    @LocalServerPort
    int localPort;

    TestRestTemplate restTemplate;

    @PostConstruct
    void initRestTemplate() {
        var builder = new RestTemplateBuilder().rootUri("http://localhost:" + localPort + "/incidencias");
        restTemplate = new TestRestTemplate(builder);
    }

    private String obtenerToken(String login, String clave) {
        var resp = restTemplate.postForEntity("/autenticacion", new DAutenticacionUsuario(login, clave), String.class);
        return resp.getBody();
    }

    private HttpHeaders headerAuth(String token) {
        HttpHeaders h = new HttpHeaders();
        h.add("Authorization", "Bearer " + token);
        return h;
    }

    @Test
    @DirtiesContext
    void testCrearYListarIncidencia() {
        // 1. Crear Usuario
        DUsuario usuario = new DUsuario("Pepe", "Gotera", LocalDate.now(), "Calle A", "600000000", "pepe@test.com", "pepe", "clave");
        restTemplate.postForEntity("/usuarios", usuario, Void.class);
        String token = obtenerToken("pepe", "clave");

        // 2. Obtener un tipo de incidencia (Asumimos que el Admin crea uno al inicio o lo creamos ahora)
        // Login como admin para crear tipo
        String tokenAdmin = obtenerToken("admin", "admin123");
        DTipoIncidencia nuevoTipo = new DTipoIncidencia(0, "Farola Rota", "Luz fundida");

        var reqTipo = new RequestEntity<>(nuevoTipo, headerAuth(tokenAdmin), HttpMethod.POST, java.net.URI.create("/incidencias/tipos"));
        restTemplate.exchange(reqTipo, Void.class);

        // Listar para obtener el ID del tipo
        var reqListarTipos = new RequestEntity<>(headerAuth(token), HttpMethod.GET, java.net.URI.create("/incidencias/tipos"));
        var respTipos = restTemplate.exchange(reqListarTipos, new ParameterizedTypeReference<List<DTipoIncidencia>>() {});
        int idTipo = respTipos.getBody().get(0).id();

        // 3. Crear Incidencia (Usuario Pepe)
        DNuevaIncidencia nueva = new DNuevaIncidencia(idTipo, "Mi calle a oscuras", "Calle A", "38.0,-3.0", null);
        var reqCrear = new RequestEntity<>(nueva, headerAuth(token), HttpMethod.POST, java.net.URI.create("/incidencias"));
        var respCrear = restTemplate.exchange(reqCrear, DIncidencia.class);

        assertThat(respCrear.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respCrear.getBody().descripcion()).isEqualTo("Mi calle a oscuras");

        // 4. Listar mis incidencias
        var reqListar = new RequestEntity<>(headerAuth(token), HttpMethod.GET, java.net.URI.create("/incidencias/propias"));
        var respListar = restTemplate.exchange(reqListar, new ParameterizedTypeReference<List<DIncidencia>>() {});

        assertThat(respListar.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respListar.getBody()).hasSize(1);
        assertThat(respListar.getBody().get(0).usuario()).isEqualTo("pepe");
    }
}