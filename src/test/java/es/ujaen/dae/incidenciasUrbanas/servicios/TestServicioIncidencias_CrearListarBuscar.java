package es.ujaen.dae.incidenciasUrbanas.servicios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Estado;
import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.TipoIncidenciaNoencontrado;
import es.ujaen.dae.incidenciasUrbanas.excepciones.UsuarioNoEncontrado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestServicioIncidencias_Incidencias {

    private ServicioIncidencias servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioIncidencias();
        // Usuario base para las pruebas
        var u = new Usuario(
                1, "Alberto", "Lopez",
                LocalDate.of(2000, 1, 1),
                "C/ Ejemplo 1",
                "600000000",
                "alberto@example.com",
                "alberto",
                "clave"
        );
        servicio.registrarUsuario(u);
    }

    /** Inyecta un TipoIncidencia en el mapa privado (no hay API pública para crearlos aún). */
    private UUID seedTipo(String nombre, String descripcion) {
        try {
            Field f = ServicioIncidencias.class.getDeclaredField("tiposIncidencia");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, TipoIncidencia> tipos = (Map<UUID, TipoIncidencia>) f.get(servicio);
            var t = new TipoIncidencia(nombre, descripcion);
            tipos.put(t.getId(), t);
            return t.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void registrarIncidencia_ok() {
        UUID tipoId = seedTipo("Suciedad", "Basuras y limpieza");
        var inc = servicio.registrarIncidencia("alberto", tipoId,
                "Basura en acera", "Gran Vía 1", null);

        assertNotNull(inc);
        assertNotNull(inc.getId());
        assertEquals(Estado.PENDIENTE, inc.getEstado()); // estado inicial
        assertEquals("alberto", inc.getUsuario().getLogin());
        assertEquals(tipoId, inc.getTipo().getId());
    }

    @Test
    void registrarIncidencia_usuarioNoExiste() {
        UUID tipoId = seedTipo("Rotura", "Mobiliario urbano");
        assertThrows(UsuarioNoEncontrado.class, () ->
                servicio.registrarIncidencia("noexiste", tipoId, "Banco roto", "Plaza Mayor", null));
    }

    @Test
    void registrarIncidencia_tipoNoExiste() {
        UUID tipoInexistente = UUID.randomUUID();
        assertThrows(TipoIncidenciaNoencontrado.class, () ->
                servicio.registrarIncidencia("alberto", tipoInexistente, "Columpio roto", "Parque Central", null));
    }

    @Test
    void listarIncidenciasDeUsuario_devuelveLasPropias() {
        UUID tipoSuciedad = seedTipo("Suciedad", "Basuras");
        servicio.registrarIncidencia("alberto", tipoSuciedad, "Papelera llena", "Calle 1", null);
        servicio.registrarIncidencia("alberto", tipoSuciedad, "Restos en acera", "Calle 2", null);

        var mias = servicio.listarIncidenciasDeUsuario("alberto");
        assertNotNull(mias);
        assertEquals(2, mias.size());
        assertTrue(mias.stream().allMatch(i -> "alberto".equals(i.getUsuario().getLogin())));
    }

    @Test
    void buscarIncidencias_porTipo_porEstado_y_compuesto() {
        UUID tipoSuciedad = seedTipo("Suciedad", "Basuras");
        UUID tipoRotura   = seedTipo("Rotura", "Parques / mobiliario");

        var i1 = servicio.registrarIncidencia("alberto", tipoSuciedad, "Basura", "Calle A", null); // PENDIENTE
        var i2 = servicio.registrarIncidencia("alberto", tipoRotura,   "Banco roto", "Calle B", null);
        // Cambiamos estado de i2 a EN_EVALUACION para probar filtros por estado
        i2.actualizarEstado(Estado.EN_EVALUACION);

        // Por tipo
        var soloSuciedad = servicio.buscarIncidencias(tipoSuciedad, null);
        assertEquals(1, soloSuciedad.size());
        assertEquals(i1.getId(), soloSuciedad.get(0).getId());

        // Por estado
        var enEvaluacion = servicio.buscarIncidencias(null, Estado.EN_EVALUACION);
        assertEquals(1, enEvaluacion.size());
        assertEquals(i2.getId(), enEvaluacion.get(0).getId());

        // Combinado tipo + estado
        var roturaEnEvaluacion = servicio.buscarIncidencias(tipoRotura, Estado.EN_EVALUACION);
        assertEquals(1, roturaEnEvaluacion.size());
        assertEquals(i2.getId(), roturaEnEvaluacion.get(0).getId());

        // Combinación que no existe
        var suciedadEnEvaluacion = servicio.buscarIncidencias(tipoSuciedad, Estado.EN_EVALUACION);
        assertTrue(suciedadEnEvaluacion.isEmpty());
    }
}
