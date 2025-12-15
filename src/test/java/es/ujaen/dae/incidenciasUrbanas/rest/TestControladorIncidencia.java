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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = IncidenciasUrbanas.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestControladorIncidencia {

    @LocalServerPort
    int localPort;

    TestRestTemplate restTemplate;

    // Datos auxiliares para los tests
    private int idTipoIncidencia;
    private final String loginUsuario = "pepe";
    private final String claveUsuario = "clave";

    @PostConstruct
    void initRestTemplate() {
        // OJO: La raíz ya incluye "/incidencias"
        var builder = new RestTemplateBuilder().rootUri("http://localhost:" + localPort + "/incidencias");
        restTemplate = new TestRestTemplate(builder);
    }

    /**
     * Este método se ejecuta antes de CADA test.
     * Prepara el terreno: crea un usuario y un tipo de incidencia.
     */
    @BeforeEach
    void setUp() {
        // 1. Registramos al usuario "Pepe" si no existe (capturamos error por si el contexto no se limpia)
        DUsuario usuario = new DUsuario("Pepe", "Gotera", LocalDate.now(), "Calle A", "600000000", "pepe@test.com", loginUsuario, claveUsuario);
        try {
            restTemplate.postForEntity("/usuarios", usuario, Void.class);
        } catch (Exception e) {
            // Ignoramos si ya existe por tests previos no limpiados
        }

        // 2. Nos logueamos como ADMIN para crear un Tipo de Incidencia
        String tokenAdmin = obtenerToken("admin", "admin123");
        DTipoIncidencia nuevoTipo = new DTipoIncidencia(0, "Farola Rota", "Luz fundida");

        // Creamos el tipo (Ruta relativa: /tipos, NO /incidencias/tipos)
        var reqTipo = new RequestEntity<>(nuevoTipo, headerAuth(tokenAdmin), HttpMethod.POST, java.net.URI.create("/tipos"));
        restTemplate.exchange(reqTipo, Void.class);

        // 3. Recuperamos el ID del tipo creado para usarlo en los tests
        var reqListarTipos = new RequestEntity<>(headerAuth(tokenAdmin), HttpMethod.GET, java.net.URI.create("/tipos"));
        var respTipos = restTemplate.exchange(reqListarTipos, new ParameterizedTypeReference<List<DTipoIncidencia>>() {});

        // Guardamos el ID del primer tipo que encontremos
        if (respTipos.getBody() != null && !respTipos.getBody().isEmpty()) {
            idTipoIncidencia = respTipos.getBody().get(0).id();
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private String obtenerToken(String login, String clave) {
        var resp = restTemplate.postForEntity("/autenticacion", new DAutenticacionUsuario(login, clave), String.class);
        return resp.getBody();
    }

    private HttpHeaders headerAuth(String token) {
        HttpHeaders h = new HttpHeaders();
        h.add("Authorization", "Bearer " + token);
        return h;
    }

    // --- LOS TESTS QUE TE HAN PEDIDO ---

    @Test
    @DirtiesContext
    void testCrearIncidencia() {
        String token = obtenerToken(loginUsuario, claveUsuario);

        // Creamos la incidencia
        DNuevaIncidencia nueva = new DNuevaIncidencia(idTipoIncidencia, "Mi calle a oscuras", "Calle A", "38.0,-3.0", null);

        // Hacemos POST a la raíz ("/") porque el controlador ya está en /incidencias
        var reqCrear = new RequestEntity<>(nueva, headerAuth(token), HttpMethod.POST, java.net.URI.create("/"));
        var respCrear = restTemplate.exchange(reqCrear, DIncidencia.class);

        // Verificamos que se ha creado (201 CREATED)
        assertThat(respCrear.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respCrear.getBody()).isNotNull();
        assertThat(respCrear.getBody().descripcion()).isEqualTo("Mi calle a oscuras");
    }

    @Test
    @DirtiesContext
    void testListarMisIncidencias() {
        String token = obtenerToken(loginUsuario, claveUsuario);

        // 1. Primero creamos una para tener algo que listar
        DNuevaIncidencia nueva = new DNuevaIncidencia(idTipoIncidencia, "Bache en la acera", "Calle B", "38.1,-3.1", null);
        var reqCrear = new RequestEntity<>(nueva, headerAuth(token), HttpMethod.POST, java.net.URI.create("/"));
        restTemplate.exchange(reqCrear, DIncidencia.class);

        // 2. Listamos las incidencias propias (GET /propias)
        var reqListar = new RequestEntity<>(headerAuth(token), HttpMethod.GET, java.net.URI.create("/propias"));
        var respListar = restTemplate.exchange(reqListar, new ParameterizedTypeReference<List<DIncidencia>>() {});

        // Verificamos
        assertThat(respListar.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respListar.getBody()).isNotEmpty();
        assertThat(respListar.getBody().get(0).descripcion()).isEqualTo("Bache en la acera");
    }

    @Test
    @DirtiesContext
    void testBorrarIncidencia() {
        String token = obtenerToken(loginUsuario, claveUsuario);

        // 1. Creamos la incidencia para luego borrarla
        DNuevaIncidencia nueva = new DNuevaIncidencia(idTipoIncidencia, "Para borrar", "Calle C", "38.2,-3.2", null);
        var reqCrear = new RequestEntity<>(nueva, headerAuth(token), HttpMethod.POST, java.net.URI.create("/"));
        var respCrear = restTemplate.exchange(reqCrear, DIncidencia.class);

        int idIncidenciaCreada = respCrear.getBody().id();

        // 2. La borramos (DELETE /{id})
        var reqBorrar = new RequestEntity<>(headerAuth(token), HttpMethod.DELETE, java.net.URI.create("/" + idIncidenciaCreada));
        var respBorrar = restTemplate.exchange(reqBorrar, Void.class);

        // Verificamos que devuelve 204 No Content
        assertThat(respBorrar.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 3. Intentamos listarla para asegurar que ya no está
        var reqListar = new RequestEntity<>(headerAuth(token), HttpMethod.GET, java.net.URI.create("/propias"));
        var respListar = restTemplate.exchange(reqListar, new ParameterizedTypeReference<List<DIncidencia>>() {});

        assertThat(respListar.getBody()).isEmpty();
    }
}