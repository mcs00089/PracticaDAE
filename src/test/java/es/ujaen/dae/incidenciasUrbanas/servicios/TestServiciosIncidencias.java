package es.ujaen.dae.incidenciasUrbanas.servicios;

import es.ujaen.dae.incidenciasUrbanas.entidades.*;
import es.ujaen.dae.incidenciasUrbanas.excepciones.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = es.ujaen.dae.incidenciasUrbanas.app.IncidenciasUrbanas.class)
public class TestServiciosIncidencias {

    @Autowired
    ServicioIncidencias servicio;

    private Usuario usuarioNormal;
    private Usuario admin;
    private TipoIncidencia tipoIncidencia;

    @BeforeEach
    void setUp() {
        usuarioNormal = new Usuario(
                1,
                "Juan",
                "Gómez Pérez",
                LocalDate.of(1990, 5, 20),
                "Calle Mayor 5",
                "600123456",
                "juan@example.com",
                "juanito",
                "clave123"
        );

        admin = new Usuario(
                0,
                "Admin",
                "Administrador",
                LocalDate.of(1990, 1, 1),
                "Ayuntamiento, Plaza Mayor",
                "657232313",
                "admin@ayuntamiento.es",
                "admin",
                "admin123"
        );

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
                2,
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
        Usuario adminLogueado = servicio.login("admin", "admin123"); // Cogemos el admin de servicio
        assertThat(adminLogueado).isNotNull();
        assertThat(adminLogueado.getNombre()).isEqualTo("Admin");
    }

    @Test
    @DirtiesContext
    void testLoginCorrectoEIncorrecto() {
        servicio.registrarUsuario(usuarioNormal);

        // Usuario no existe
        assertThatThrownBy(() -> servicio.login("inexistente", "clave"))
                .isInstanceOf(CredencialesInvalidas.class);

        // contraseña mal
        assertThatThrownBy(() -> servicio.login("juanito", "clavemal"))
                .isInstanceOf(CredencialesInvalidas.class);

        // login bien
        Usuario usuarioLogueado = servicio.login("juanito", "clave123");
        assertThat(usuarioLogueado.getEmail()).isEqualTo("juan@example.com");
        assertThat(usuarioLogueado.getNombre()).isEqualTo("Juan");
    }

    @Test
    @DirtiesContext
    void testActualizarUsuario() {
        servicio.registrarUsuario(usuarioNormal);

        Usuario nuevosDatos = new Usuario(
                1,
                "Juan Carlos",
                "Gómez López",
                LocalDate.of(1990, 5, 20),
                "Calle Nueva 10",
                "611223344",
                "juancarlos@example.com",
                "juanito",
                "nuevaClave456"
        );

        servicio.actualizarUsuario("juanito", nuevosDatos);

        Usuario actualizado = servicio.login("juanito", "nuevaClave456");
        assertThat(actualizado.getNombre()).isEqualTo("Juan Carlos");
        assertThat(actualizado.getApellidos()).isEqualTo("Gómez López");
        assertThat(actualizado.getDireccion()).isEqualTo("Calle Nueva 10");
        assertThat(actualizado.getTelefono()).isEqualTo("611223344");
        assertThat(actualizado.getEmail()).isEqualTo("juancarlos@example.com");
    }

    @Test
    @DirtiesContext
    void testActualizarUsuarioNoExistente() {
        Usuario usuarioInexistente = new Usuario(
                99,
                "Inexistente",
                "Usuario",
                LocalDate.now(),
                "Calle",
                "600000000",
                "email@example.com",
                "inexistente",
                "clave"
        );

        assertThatThrownBy(() -> servicio.actualizarUsuario("inexistente", usuarioInexistente))
                .isInstanceOf(UsuarioNoEncontrado.class);
    }

    @Test
    @DirtiesContext
    void testRegistrarIncidencia() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        Incidencia incidencia = servicio.registrarIncidencia(
                usuarioNormal,
                tipoIncidencia,
                "Basura acumulada en contenedores",
                "Calle Mayor esquina Calle Sol",
                "37.7749,-122.4194"
        );

        assertThat(incidencia).isNotNull();
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

        Usuario usuarioFalso = new Usuario(
                99,
                "Falso",
                "Usuario",
                LocalDate.now(),
                "Calle",
                "600000000",
                "falso@example.com",
                "falso",
                "clave"
        );

        assertThatThrownBy(() -> servicio.registrarIncidencia(
                usuarioFalso,
                tipoIncidencia,
                "Descripción",
                "Localización",
                "GPS"
        )).isInstanceOf(UsuarioNoEncontrado.class);
    }

    @Test
    @DirtiesContext
    void testRegistrarIncidenciaTipoNoExistente() {
        servicio.registrarUsuario(usuarioNormal);

        TipoIncidencia tipoFalso = new TipoIncidencia("Tipo falso", "Descripción falsa");

        assertThatThrownBy(() -> servicio.registrarIncidencia(
                usuarioNormal,
                tipoFalso,
                "Descripción",
                "Localización",
                "GPS"
        )).isInstanceOf(TipoIncidenciaNoencontrado.class);
    }

    @Test
    @DirtiesContext
    void testListarIncidenciasDeUsuario() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        servicio.registrarIncidencia(usuarioNormal, tipoIncidencia, "Incidencia 1", "Loc 1", "GPS1");
        servicio.registrarIncidencia(usuarioNormal, tipoIncidencia, "Incidencia 2", "Loc 2", "GPS2");

        List<Incidencia> incidencias = servicio.listarIncidenciasDeUsuario(usuarioNormal);

        assertThat(incidencias).hasSize(2);
        assertThat(incidencias).extracting("descripcion")
                .containsExactlyInAnyOrder("Incidencia 1", "Incidencia 2");
    }

    @Test
    @DirtiesContext
    void testListarIncidenciasDeUsuarioNoExistente() {
        Usuario usuarioFalso = new Usuario(
                99,
                "Falso",
                "Usuario",
                LocalDate.now(),
                "Calle",
                "600000000",
                "falso@example.com",
                "falso",
                "clave"
        );

        assertThatThrownBy(() -> servicio.listarIncidenciasDeUsuario(usuarioFalso))
                .isInstanceOf(UsuarioNoEncontrado.class);
    }

    @Test
    @DirtiesContext
    void testBuscarIncidenciasPorTipoYEstado() {
        servicio.registrarUsuario(usuarioNormal);

        TipoIncidencia tipo1 = new TipoIncidencia("Basura", "Problemas con basura");
        TipoIncidencia tipo2 = new TipoIncidencia("Mobiliario", "Problemas con mobiliario urbano");

        servicio.anadirTipoIncidencia(admin, tipo1);
        servicio.anadirTipoIncidencia(admin, tipo2);

        Incidencia inc1 = servicio.registrarIncidencia(usuarioNormal, tipo1, "Basura 1", "Loc 1", "GPS1");
        Incidencia inc2 = servicio.registrarIncidencia(usuarioNormal, tipo2, "Mobiliario 1", "Loc 2", "GPS2");

        servicio.cambiarEstadoIncidencia(admin, inc2, Estado.EN_EVALUACION);

        // Buscamos por tipo
        List<Incidencia> incidenciasTipo1 = servicio.buscarIncidencias(tipo1, null);
        assertThat(incidenciasTipo1).hasSize(1);
        assertThat(incidenciasTipo1.get(0).getTipo().getNombre()).isEqualTo("Basura");

        // Buscamos por estado
        List<Incidencia> incidenciasPendientes = servicio.buscarIncidencias(null, Estado.PENDIENTE);
        assertThat(incidenciasPendientes).hasSize(1);
        assertThat(incidenciasPendientes.get(0).getEstado()).isEqualTo(Estado.PENDIENTE);

        // Buscamos por tipo y estado
        List<Incidencia> incidenciasTipo2Evaluacion = servicio.buscarIncidencias(tipo2, Estado.EN_EVALUACION);
        assertThat(incidenciasTipo2Evaluacion).hasSize(1);
    }

    @Test
    @DirtiesContext
    void testAnadirTipoIncidencia() {
        servicio.registrarUsuario(usuarioNormal);

        TipoIncidencia nuevoTipo = new TipoIncidencia("Nuevo tipo", "Descripción del nuevo tipo");

        servicio.anadirTipoIncidencia(admin, nuevoTipo);

        Incidencia incidencia = servicio.registrarIncidencia(
                usuarioNormal,
                nuevoTipo,
                "Descripción",
                "Localización",
                "GPS"
        );

        assertThat(incidencia.getTipo().getNombre()).isEqualTo("Nuevo tipo");
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
    void testAnadirTipoIncidenciaPorUsuarioNoAdmin() {
        servicio.registrarUsuario(usuarioNormal);

        assertThatThrownBy(() -> servicio.anadirTipoIncidencia(usuarioNormal, tipoIncidencia))
                .isInstanceOf(CredencialesInvalidas.class);
    }

    @Test
    @DirtiesContext
    void testBorrarIncidenciaPorPropietario() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        Incidencia incidencia = servicio.registrarIncidencia(
                usuarioNormal,
                tipoIncidencia,
                "Descripción",
                "Localización",
                "GPS"
        );

        // Borramos como propietario
        servicio.borrarIncidencia(usuarioNormal, incidencia);

        List<Incidencia> incidencias = servicio.listarIncidenciasDeUsuario(usuarioNormal);
        assertThat(incidencias).isEmpty();
    }

    @Test
    @DirtiesContext
    void testBorrarIncidenciaNoPendientePorPropietario() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        Incidencia incidencia = servicio.registrarIncidencia(
                usuarioNormal,
                tipoIncidencia,
                "Descripción",
                "Localización",
                "GPS"
        );

        // Cambiamos estado a no pendiente
        servicio.cambiarEstadoIncidencia(admin, incidencia, Estado.EN_EVALUACION);

        // Intentamos borrar no deberia de dejar
        assertThatThrownBy(() -> servicio.borrarIncidencia(usuarioNormal, incidencia))
                .isInstanceOf(BorrarIncidenciaNoPendiente.class);
    }

    @Test
    @DirtiesContext
    void testBorrarIncidenciaPorAdmin() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        Incidencia incidencia = servicio.registrarIncidencia(
                usuarioNormal,
                tipoIncidencia,
                "Descripción",
                "Localización",
                "GPS"
        );

        // Cambiamos estado a no pendiente
        servicio.cambiarEstadoIncidencia(admin, incidencia, Estado.RESUELTA);

        // Borramos como admin debe funcionar
        servicio.borrarIncidencia(admin, incidencia);

        List<Incidencia> incidencias = servicio.listarIncidenciasDeUsuario(usuarioNormal);
        assertThat(incidencias).isEmpty();
    }

    @Test
    @DirtiesContext
    void testBorrarTipoIncidencia() {
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        servicio.borrarTipoIncidencia(admin, tipoIncidencia);

        servicio.registrarUsuario(usuarioNormal);
        assertThatThrownBy(() -> servicio.registrarIncidencia(
                usuarioNormal,
                tipoIncidencia,
                "Descripción",
                "Localización",
                "GPS"
        )).isInstanceOf(TipoIncidenciaNoencontrado.class);
    }

    @Test
    @DirtiesContext
    void testBorrarTipoIncidenciaEnUso() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        servicio.registrarIncidencia(usuarioNormal, tipoIncidencia, "Descripción", "Localización", "GPS");

        // Intentamos borrar un tipo en uso
        assertThatThrownBy(() -> servicio.borrarTipoIncidencia(admin, tipoIncidencia))
                .isInstanceOf(TipoIncidenciaEnUso.class);
    }

    @Test
    @DirtiesContext
    void testCambiarClaveSoloAdmin() {
        servicio.registrarUsuario(usuarioNormal);

        assertThatThrownBy(() -> servicio.cambiarClave(usuarioNormal, "nuevaClaveSegura"))
                .isInstanceOf(CredencialesInvalidas.class);

        servicio.cambiarClave(admin, "nuevoAdmin123");

        Usuario adminActualizado = servicio.login("admin", "nuevoAdmin123");
        assertThat(adminActualizado).isNotNull();
    }


    @Test
    @DirtiesContext
    void testCambiarEstadoIncidencia() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        Incidencia incidencia = servicio.registrarIncidencia(
                usuarioNormal,
                tipoIncidencia,
                "Descripción",
                "Localización",
                "GPS"
        );

        assertThat(incidencia.getEstado()).isEqualTo(Estado.PENDIENTE);

        // Cambiamos estado como admin
        servicio.cambiarEstadoIncidencia(admin, incidencia, Estado.EN_EVALUACION);

        List<Incidencia> incidencias = servicio.buscarIncidencias(null, Estado.EN_EVALUACION);
        assertThat(incidencias).hasSize(1);
        assertThat(incidencias.get(0).getEstado()).isEqualTo(Estado.EN_EVALUACION);
    }

    @Test
    @DirtiesContext
    void testCambiarEstadoIncidenciaPorUsuarioNoAdmin() {
        servicio.registrarUsuario(usuarioNormal);
        servicio.anadirTipoIncidencia(admin, tipoIncidencia);

        Incidencia incidencia = servicio.registrarIncidencia(
                usuarioNormal,
                tipoIncidencia,
                "Descripción",
                "Localización",
                "GPS"
        );

        // Intentamos cambiar el estado como un usuario normal
        assertThatThrownBy(() -> servicio.cambiarEstadoIncidencia(usuarioNormal, incidencia, Estado.RESUELTA))
                .isInstanceOf(CredencialesInvalidas.class);
    }

}