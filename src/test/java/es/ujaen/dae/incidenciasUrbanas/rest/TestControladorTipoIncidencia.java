package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.app.IncidenciasUrbanas;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DAutenticacionUsuario;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DTipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DUsuario;
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

@SpringBootTest(
        classes = IncidenciasUrbanas.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestControladorTipoIncidencia {

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

    private String obtenerTokenAdmin() {
        return restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario("admin", "admin123"),
                String.class
        ).getBody();
    }

    private String obtenerTokenUsuario(String login, String clave) {
        return restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario(login, clave),
                String.class
        ).getBody();
    }

    @Test
    @DirtiesContext
    void testListarTiposPublico() {
        var r = restTemplate.exchange(
                "/tipos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DTipoIncidencia>>() {}
        );

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotNull();
    }

    @Test
    @DirtiesContext
    void testCrearTipoComoAdmin() {
        String tokenAdmin = obtenerTokenAdmin();

        DTipoIncidencia nuevoTipo = new DTipoIncidencia(
                null,
                "Bache en calzada",
                "Baches y desperfectos en la calzada"
        );

        var req = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(tokenAdmin))
                .body(nuevoTipo);

        var r = restTemplate.exchange(req, Void.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var r2 = restTemplate.exchange(
                "/tipos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DTipoIncidencia>>() {}
        );

        assertThat(r2.getBody()).anyMatch(t -> t.nombre().equals("Bache en calzada"));
    }

    @Test
    @DirtiesContext
    void testCrearTipoComoUsuarioNormal() {
        DUsuario u = new DUsuario(
                "Pedro", "García", LocalDate.of(1992, 6, 15),
                "Calle Z", "644444444",
                "pedro@test.es", "pedro", "clave"
        );
        restTemplate.postForEntity("/usuarios", u, Void.class);

        String tokenUsuario = obtenerTokenUsuario("pedro", "clave");

        DTipoIncidencia nuevoTipo = new DTipoIncidencia(
                null,
                "Farola fundida",
                "Farolas sin luz"
        );

        var req = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(tokenUsuario))
                .body(nuevoTipo);

        var r = restTemplate.exchange(req, Void.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DirtiesContext
    void testCrearTipoDuplicado() {
        String tokenAdmin = obtenerTokenAdmin();

        DTipoIncidencia tipo1 = new DTipoIncidencia(
                null,
                "Contenedor desbordado",
                "Contenedores llenos"
        );

        var req1 = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(tokenAdmin))
                .body(tipo1);

        restTemplate.exchange(req1, Void.class);

        var req2 = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(tokenAdmin))
                .body(tipo1);

        var r2 = restTemplate.exchange(req2, Void.class);

        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DirtiesContext
    void testObtenerTipoPorId() {
        String tokenAdmin = obtenerTokenAdmin();

        DTipoIncidencia nuevoTipo = new DTipoIncidencia(
                null,
                "Árbol caído",
                "Árboles y ramas caídas"
        );

        var req = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(tokenAdmin))
                .body(nuevoTipo);

        restTemplate.exchange(req, Void.class);

        // Obtenemos todos los tipos
        var r1 = restTemplate.exchange(
                "/tipos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DTipoIncidencia>>() {}
        );

        DTipoIncidencia tipoCreado = r1.getBody().stream()
                .filter(t -> t.nombre().equals("Árbol caído"))
                .findFirst()
                .orElse(null);

        assertThat(tipoCreado).isNotNull();

        var r2 = restTemplate.getForEntity("/tipos/{id}", DTipoIncidencia.class, tipoCreado.id());

        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r2.getBody().nombre()).isEqualTo("Árbol caído");
    }

    @Test
    @DirtiesContext
    void testBorrarTipoComoAdmin() {
        String tokenAdmin = obtenerTokenAdmin();

        DTipoIncidencia nuevoTipo = new DTipoIncidencia(
                null,
                "Graffiti",
                "Pintadas en fachadas"
        );

        var req = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(tokenAdmin))
                .body(nuevoTipo);

        restTemplate.exchange(req, Void.class);

        var r1 = restTemplate.exchange(
                "/tipos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DTipoIncidencia>>() {}
        );

        DTipoIncidencia tipoCreado = r1.getBody().stream()
                .filter(t -> t.nombre().equals("Graffiti"))
                .findFirst()
                .orElse(null);

        assertThat(tipoCreado).isNotNull();

        var reqDelete = RequestEntity
                .delete("/tipos/{id}", tipoCreado.id())
                .headers(headerAutorizacion(tokenAdmin))
                .build();

        var r2 = restTemplate.exchange(reqDelete, Void.class);

        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var r3 = restTemplate.getForEntity("/tipos/{id}", DTipoIncidencia.class, tipoCreado.id());
        assertThat(r3.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void testBorrarTipoComoUsuarioNormal() {
        String tokenAdmin = obtenerTokenAdmin();

        DTipoIncidencia nuevoTipo = new DTipoIncidencia(
                null,
                "Mobiliario roto",
                "Bancos y mobiliario urbano deteriorado"
        );

        var req = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(tokenAdmin))
                .body(nuevoTipo);

        restTemplate.exchange(req, Void.class);

        DUsuario u = new DUsuario(
                "Carlos", "Ruiz", LocalDate.of(1994, 8, 20),
                "Calle X", "655555555",
                "carlos@test.es", "carlos", "clave"
        );
        restTemplate.postForEntity("/usuarios", u, Void.class);

        String tokenUsuario = obtenerTokenUsuario("carlos", "clave");

        var r1 = restTemplate.exchange(
                "/tipos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DTipoIncidencia>>() {}
        );

        DTipoIncidencia tipoCreado = r1.getBody().stream()
                .filter(t -> t.nombre().equals("Mobiliario roto"))
                .findFirst()
                .orElse(null);

        assertThat(tipoCreado).isNotNull();

        var reqDelete = RequestEntity
                .delete("/tipos/{id}", tipoCreado.id())
                .headers(headerAutorizacion(tokenUsuario))
                .build();

        var r2 = restTemplate.exchange(reqDelete, Void.class);

        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
