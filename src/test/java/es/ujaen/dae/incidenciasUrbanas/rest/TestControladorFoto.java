package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.app.IncidenciasUrbanas;
import es.ujaen.dae.incidenciasUrbanas.entidades.Estado;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DAutenticacionUsuario;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DIncidencia;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DTipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.rest.DTO.DUsuario;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import static org.assertj.core.api.Assertions.assertThat;


import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest(
        classes = IncidenciasUrbanas.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestControladorFoto {

    @LocalServerPort
    int localPort;

    TestRestTemplate restTemplate;

    @PostConstruct
    void initRestTemplate() {
        var builder = new RestTemplateBuilder()
                .rootUri("http://localhost:" + localPort + "/incidencias");

        restTemplate = new TestRestTemplate(builder);
    }

    private String obtenerToken(String login, String clave) {
        return restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario(login, clave),
                String.class
        ).getBody();
    }

    @Test
    @DirtiesContext
    void testSubirYDescargarFoto() {
        DUsuario u = new DUsuario(
                "Juan", "Pérez", LocalDate.of(1990, 5, 10),
                "Calle A", "600123123",
                "juan@test.es", "juan", "clave123"
        );
        ResponseEntity<Void> rUsuario = restTemplate.postForEntity("/usuarios", u, Void.class);
        assertThat(rUsuario.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String token = obtenerToken("juan", "clave123");
        assertThat(token).isNotNull();

        DTipoIncidencia tipo = new DTipoIncidencia(3,"Obras", "Descripción tipo");
        HttpHeaders headersTipo = new HttpHeaders();
        headersTipo.set("Authorization", "Bearer " + token);
        headersTipo.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DTipoIncidencia> entityTipo = new HttpEntity<>(tipo, headersTipo);
        ResponseEntity<Void> rTipo = restTemplate.exchange(
                "/tipos",
                HttpMethod.POST,
                entityTipo,
                Void.class
        );
        assertThat(rTipo.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        DIncidencia incidencia = new DIncidencia(
                0,
                "juan",
                "Obras",
                "Incidencia de prueba",
                "Calle B",
                "40.4168,-3.7038",
                LocalDateTime.now(),
                Estado.PENDIENTE
        );

        HttpHeaders headersIncidencia = new HttpHeaders();
        headersIncidencia.set("Authorization", "Bearer " + token);
        headersIncidencia.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DIncidencia> entityIncidencia = new HttpEntity<>(incidencia, headersIncidencia);

        ResponseEntity<DIncidencia> rIncidencia = restTemplate.exchange(
                "/",
                HttpMethod.POST,
                entityIncidencia,
                DIncidencia.class
        );
        assertThat(rIncidencia.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        int idIncidencia = rIncidencia.getBody().id();


        byte[] fotoBytes = new byte[]{1,2,3,4,5,6,7,8,9,10};

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fotoBytes) {
            @Override
            public String getFilename() {
                return "foto.jpg";
            }
        });

        HttpHeaders headersFoto = new HttpHeaders();
        headersFoto.setContentType(MediaType.MULTIPART_FORM_DATA);
        headersFoto.set("Authorization", "Bearer " + token);

        HttpEntity<MultiValueMap<String, Object>> requestFoto = new HttpEntity<>(body, headersFoto);

        ResponseEntity<String> rFoto = restTemplate.exchange(
                "/" + idIncidencia + "/foto",
                HttpMethod.POST,
                requestFoto,
                String.class
        );
        assertThat(rFoto.getStatusCode()).isEqualTo(HttpStatus.OK);


        HttpHeaders headersDescarga = new HttpHeaders();
        headersDescarga.set("Authorization", "Bearer " + token);
        HttpEntity<Void> requestDescarga = new HttpEntity<>(headersDescarga);

        ResponseEntity<byte[]> rDescarga = restTemplate.exchange(
                "/" + idIncidencia + "/foto",
                HttpMethod.GET,
                requestDescarga,
                byte[].class
        );

        assertThat(rDescarga.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rDescarga.getBody()).isNotEmpty();
        assertThat(rDescarga.getBody().length).isEqualTo(fotoBytes.length);
    }
}
