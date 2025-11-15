package es.ujaen.dae.incidenciasUrbanas;

import es.ujaen.dae.incidenciasUrbanas.entidades.*;
import es.ujaen.dae.incidenciasUrbanas.excepciones.*;
import es.ujaen.dae.incidenciasUrbanas.servicios.ServicioIncidencias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = es.ujaen.dae.incidenciasUrbanas.app.IncidenciasUrbanas.class)
@ActiveProfiles("test")
public class TestServiciosIncidencias {

    @Autowired
    private ServicioIncidencias servicio;

    private Usuario usuarioNormal;
    private Usuario admin;
    private TipoIncidencia tipoIncidencia;

    @BeforeEach
    void setUp() {
        usuarioNormal = new Usuario(
                "Juan",
                "Gómez Pérez",
                LocalDate.of(1990, 5, 20),
                "Calle Mayor 5",
                "600123456",
                "juan@example.com",
                "juanito",
                "clave123"
        );

        admin = servicio.login("admin", "admin123")
                .orElseThrow(() -> new AssertionError("Admin debería existir"));

        tipoIncidencia = new TipoIncidencia("Basura acumulada", "Acumulación de residuos en vía pública");
    }

    @Test
    @DirtiesContext
    void testRegistrarUsuarioDuplicado() {
        servicio.registrarUsuario(usuarioNormal);
        assertThatThrownBy(() -> servicio.registrarUsuario(usuarioNormal))
                .isInstanceOf(UsuarioYaExiste.class);
    }

    @Test
    @DirtiesContext
    void testRegistrarUsuarioConLoginAdmin() {
        Usuario usuarioConLoginAdmin = new Usuario(
                "Falso",
                "Admin",
                LocalDate.of(1990, 1, 1),
                "Calle Falsa 123",
                "600000000",
                "falso@example.com",
                "admin",
                "clave123"
        );

        assertThatThrownBy(() -> servicio.registrarUsuario(usuarioConLoginAdmin))
                .isInstanceOf(UsuarioNoAdmin.class);
    }

    @Test
    @DirtiesContext
    void testLoginAdmin() {
        Usuario adminLogueado = servicio.login("admin", "admin123")
                .orElseThrow(() -> new AssertionError("Login admin debería funcionar"));
        assertThat(adminLogueado.getNombre()).isEqualTo("Admin");
    }

    @Test
    @DirtiesContext
    void testLoginCorrectoEIncorrecto() {
        servicio.registrarUsuario(usuarioNormal);

        assertThat(servicio.login("inexistente", "clave")).isEmpty();
        assertThat(servicio.login("juanito", "clavemal")).isEmpty();

        Usuario usuarioLogueado = servicio.login("juanito", "clave123")
                .orElseThrow(() -> new AssertionError("Login debería funcionar"));
        assertThat(usuarioLogueado.getEmail()).isEqualTo("juan@example.com");
        assertThat(usuarioLogueado.getNombre()).isEqualTo("Juan");
    }

    @Test
    @DirtiesContext
    void testActualizarUsuarioNoExistente() {
        Usuario usuarioInexistente = new Usuario(
                "Inexistente",
                "Usuario",
                LocalDate.now(),
                "Calle",
                "600000000",
                "email@example.com",
                "inexistente",
                "clave"
        );

        assertThatThrownBy(() -> servicio.actualizarUsuario(usuarioInexistente, usuarioInexistente))
                .isInstanceOf(UsuarioNoEncontrado.class);
    }


    @Test
    @DirtiesContext
    void testRegistrarIncidencia() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);
        TipoIncidencia tipoDisponible = servicio.listarTiposIncidencias().get(0);
        byte[] foto = new byte[]{1, 2, 3, 4, 5};
        Incidencia incidencia = servicio.registrarIncidencia(
                usuarioNormal,
                tipoDisponible,
                "Basura acumulada en contenedores",
                "Calle Mayor esquina Calle Sol",
                "37.7749,-122.4194",
                foto
        );

        assertThat(incidencia.getId()).isNotNull();
        assertThat(incidencia.getDescripcion()).isEqualTo("Basura acumulada en contenedores");
        assertThat(incidencia.getEstado()).isEqualTo(Estado.PENDIENTE);
        assertThat(incidencia.getUsuario().getLogin()).isEqualTo("juanito");
        assertThat(incidencia.getTipo().getNombre()).isEqualTo("Basura acumulada");
    }

    @Test
    @DirtiesContext
    void testRegistrarIncidenciaUsuarioNoExistente() {
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);
        TipoIncidencia tipoDisponible = servicio.listarTiposIncidencias().get(0);
        Usuario usuarioFalso = new Usuario(
                "Falso",
                "Usuario",
                LocalDate.now(),
                "Calle",
                "600000000",
                "falso@example.com",
                "falso",
                "clave"
        );

        byte[] foto = new byte[]{1, 2, 3, 4, 5};
        assertThatThrownBy(() -> servicio.registrarIncidencia(
                usuarioFalso,
                tipoDisponible,
                "Descripción",
                "Localización",
                "GPS",
                foto
        )).isInstanceOf(UsuarioNoEncontrado.class);
    }

    @Test
    @DirtiesContext
    void testRegistrarIncidenciaTipoNoExistente() {
        servicio.registrarUsuario(usuarioNormal);

        TipoIncidencia tipoFalso = new TipoIncidencia("Tipo falso", "Descripción falsa");
        byte[] foto = new byte[]{1, 2, 3, 4, 5};
        assertThatThrownBy(() -> servicio.registrarIncidencia(
                usuarioNormal,
                tipoFalso,
                "Descripción",
                "Localización",
                "GPS",
                foto
        )).isInstanceOf(TipoIncidenciaNoencontrado.class);
    }

    @Test
    @DirtiesContext
    void testListarIncidenciasDeUsuario() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);
        TipoIncidencia tipoDisponible = servicio.listarTiposIncidencias().get(0);
        byte[] foto = new byte[]{1, 2, 3, 4, 5};
        servicio.registrarIncidencia(usuarioNormal, tipoDisponible, "Incidencia 1", "Loc 1", "GPS1",foto);
        servicio.registrarIncidencia(usuarioNormal, tipoDisponible, "Incidencia 2", "Loc 2", "GPS2",foto);

        List<Incidencia> incidencias = servicio.listarIncidenciasDeUsuario(usuarioNormal);

        assertThat(incidencias).hasSize(2);
        assertThat(incidencias).extracting("descripcion")
                .containsExactlyInAnyOrder("Incidencia 1", "Incidencia 2");
    }

    @Test
    @DirtiesContext
    void testBuscarIncidenciasPorTipoYEstado() {
        servicio.registrarUsuario(usuarioNormal);

        TipoIncidencia tipo1 = new TipoIncidencia("Basura", "Problemas con basura");
        TipoIncidencia tipo2 = new TipoIncidencia("Mobiliario", "Problemas con mobiliario urbano");

        servicio.anadirTipoIncidencia(admin, tipo1);
        servicio.anadirTipoIncidencia(admin, tipo2);
        byte[] foto = new byte[]{1, 2, 3, 4, 5};
        Incidencia inc1 = servicio.registrarIncidencia(usuarioNormal, tipo1, "Basura 1", "Loc 1", "GPS1",foto);
        Incidencia inc2 = servicio.registrarIncidencia(usuarioNormal, tipo2, "Mobiliario 1", "Loc 2", "GPS2",foto);

        servicio.cambiarEstadoIncidencia(admin, inc2, Estado.EN_EVALUACION);

        List<Incidencia> incidenciasTipo1 = servicio.buscarIncidencias(tipo1, null);
        assertThat(incidenciasTipo1).hasSize(1);

        List<Incidencia> incidenciasPendientes = servicio.buscarIncidencias(null, Estado.PENDIENTE);
        assertThat(incidenciasPendientes).hasSize(1);

        List<Incidencia> incidenciasTipo2Eval = servicio.buscarIncidencias(tipo2, Estado.EN_EVALUACION);
        assertThat(incidenciasTipo2Eval).hasSize(1);
    }

    @Test
    @DirtiesContext
    void testBorrarIncidenciaPorPropietario() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);
        TipoIncidencia tipoDisponible = servicio.listarTiposIncidencias().get(0);
        byte[] foto = new byte[]{1, 2, 3, 4, 5};
        Incidencia incidencia = servicio.registrarIncidencia(usuarioNormal, tipoDisponible, "Desc", "Loc", "GPS",foto);

        servicio.borrarIncidencia(usuarioNormal, incidencia);

        List<Incidencia> incidencias = servicio.listarIncidenciasDeUsuario(usuarioNormal);
        assertThat(incidencias).isEmpty();
    }

    @Test
    @DirtiesContext
    void testBorrarIncidenciaNoPendientePorPropietario() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);
        TipoIncidencia tipoDisponible = servicio.listarTiposIncidencias().get(0);
        byte[] foto = new byte[]{1, 2, 3, 4, 5};
        Incidencia incidencia = servicio.registrarIncidencia(usuarioNormal, tipoDisponible, "Desc", "Loc", "GPS",foto);

        servicio.cambiarEstadoIncidencia(admin, incidencia, Estado.EN_EVALUACION);

        assertThatThrownBy(() -> servicio.borrarIncidencia(usuarioNormal, incidencia))
                .isInstanceOf(BorrarIncidenciaNoPendiente.class);
    }

    @Test
    @DirtiesContext
    void testBorrarIncidenciaPorAdmin() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);
        TipoIncidencia tipoDisponible = servicio.listarTiposIncidencias().get(0);
        byte[] foto = new byte[]{1, 2, 3, 4, 5};
        Incidencia incidencia = servicio.registrarIncidencia(usuarioNormal, tipoDisponible, "Desc", "Loc", "GPS",foto);

        servicio.cambiarEstadoIncidencia(admin, incidencia, Estado.RESUELTA);

        servicio.borrarIncidencia(admin, incidencia);

        List<Incidencia> incidencias = servicio.listarIncidenciasDeUsuario(usuarioNormal);
        assertThat(incidencias).isEmpty();
    }

    @Test
    @DirtiesContext
    void testAnadirTipoIncidenciaDuplicado() {
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        TipoIncidencia tipoDuplicado = new TipoIncidencia("Basura acumulada", "Otra descripción");

        assertThatThrownBy(() -> servicio.anadirTipoIncidencia(admin, tipoDuplicado))
                .isInstanceOf(TipoIncidenciaEnUso.class);
    }

    @Test
    @DirtiesContext
    void testBorrarTipoIncidenciaEnUso() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);
        TipoIncidencia tipoDisponible = servicio.listarTiposIncidencias().get(0);
        byte[] foto = new byte[]{1, 2, 3, 4, 5};
        servicio.registrarIncidencia(usuarioNormal, tipoDisponible, "Desc", "Loc", "GPS",foto);

        assertThatThrownBy(() -> servicio.borrarTipoIncidencia(admin, tipoIncidencia))
                .isInstanceOf(TipoIncidenciaEnUso.class);
    }

    @Test
    @DirtiesContext
    void testCambiarEstadoIncidenciaPorUsuarioNoAdmin() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);
        TipoIncidencia tipoDisponible = servicio.listarTiposIncidencias().get(0);


        byte[] foto = new byte[]{1, 2, 3, 4, 5};


        Incidencia incidencia = servicio.registrarIncidencia(usuarioNormal, tipoDisponible, "Desc", "Loc", "GPS",foto);

        assertThatThrownBy(() -> servicio.cambiarEstadoIncidencia(usuarioNormal, incidencia, Estado.RESUELTA))
                .isInstanceOf(CredencialesInvalidas.class);
    }

    @Test
    @DirtiesContext
    void testIncidenciasCercanas() {
        // Registrar usuario y tipo
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        // Coordenadas de referencia
        String gpsReferencia = "37.7749,-122.4194";

        byte[] foto = new byte[]{1, 2, 3, 4, 5};  // Simulamos una imagen

        TipoIncidencia tipoDisponible = servicio.listarTiposIncidencias().get(0);

        // Incidencias cercanas
        Incidencia incCercana1 = servicio.registrarIncidencia(
                usuarioNormal, tipoDisponible, "Incidencia cercana 1", "Loc 1", "37.77491,-122.41941", foto
        );
        Incidencia incCercana2 = servicio.registrarIncidencia(
                usuarioNormal, tipoDisponible, "Incidencia cercana 2", "Loc 2", "37.77492,-122.41942", foto
        );

        // Incidencia lejana
        Incidencia incLejana = servicio.registrarIncidencia(
                usuarioNormal, tipoDisponible, "Incidencia lejana", "Loc 3", "37.7755,-122.4200", foto
        );

        // Cambiamos el estado de una incidencia a RESUELTA para comprobar que se excluye
        servicio.cambiarEstadoIncidencia(admin, incCercana2, Estado.RESUELTA);

        List<Incidencia> cercanas = servicio.incidenciasCercanas(gpsReferencia);

        // Comprobaciones
        assertThat(cercanas).hasSize(1);
        assertThat(cercanas.get(0).getDescripcion()).isEqualTo("Incidencia cercana 1");
        assertThat(cercanas.get(0).getFoto()).isEqualTo(foto);
    }




}