import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public package es.ujaen.dae.incidenciasUrbanas.servicios;

import es.ujaen.dae.incidenciasUrbanas.excepciones.TipoIncidenciaInvalido;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class IncidenciaTest {

    private Usuario usuarioDemo() {
        return new Usuario(
                1, "Nombre", "Apellidos",
                LocalDate.of(1999, 12, 31),
                "Dirección 1",
                "600000000",
                "userA@example.com",
                "userA",
                "pwd"
        );
    }

    @Test
    void constructor_seteaCamposYEstadoInicialPendiente() {
        var u = usuarioDemo();
        var tipo = new TipoIncidencia("Suciedad", "Basuras y limpieza");

        var inc = new Incidencia(u, tipo, "Basura en acera", "Calle Uno 1", null);

        assertNotNull(inc.getId());
        assertEquals(u.getLogin(), inc.getUsuario().getLogin());
        assertEquals("Suciedad", inc.getTipo().getNombre());
        assertEquals("Basura en acera", inc.getDescripcion());
        assertEquals("Calle Uno 1", inc.getLocalizacion());
        assertNull(inc.getLocalizacionGps()); // opcional en tu modelo
        assertEquals(Estado.PENDIENTE, inc.getEstado());
    }

    @Test
    void actualizarEstado_transicionesBasicas() {
        var inc = new Incidencia(
                usuarioDemo(),
                new TipoIncidencia("Rotura", "Mobiliario"),
                "Banco roto",
                "Plaza Central",
                null
        );

        // PENDIENTE -> EN_EVALUACION
        inc.actualizarEstado(Estado.EN_EVALUACION);
        assertEquals(Estado.EN_EVALUACION, inc.getEstado());

        // EN_EVALUACION -> RESUELTA
        inc.actualizarEstado(Estado.RESUELTA);
        assertEquals(Estado.RESUELTA, inc.getEstado());

        // RESUELTA -> PENDIENTE (si tu dominio lo permite; no hay restricción en tu entidad)
        inc.actualizarEstado(Estado.PENDIENTE);
        assertEquals(Estado.PENDIENTE, inc.getEstado());
    }

    @Test
    void cambiarTipoIncidencia_validoYCasosError() {
        var inc = new Incidencia(
                usuarioDemo(),
                new TipoIncidencia("Suciedad", "Basuras"),
                "Restos en acera",
                "Calle Dos 2",
                null
        );

        // Cambiar a tipo válido
        var nuevoTipo = new TipoIncidencia("Parque", "Juegos infantiles");
        inc.cambiarTipoIncidencia(nuevoTipo);
        assertEquals("Parque", inc.getTipo().getNombre());

        // Cambiar a null => excepción
        assertThrows(TipoIncidenciaInvalido.class, () -> inc.cambiarTipoIncidencia(null));
    }

    @Test
    void equalsHashCode_basicoPorId_siTuEntidadLoImplementa() {
        // Este test solo tiene sentido si has sobreescrito equals/hashCode por ID.
        // Si no lo has hecho, puedes eliminar este método.
        var u = usuarioDemo();
        var t = new TipoIncidencia("Señales", "Tráfico");
        var a = new Incidencia(u, t, "Señal caída", "Avenida 3", null);
        var b = new Incidencia(u, t, "Señal caída", "Avenida 3", null);

        // Fuerza a que ambos tengan mismo id (si ofreces setter en tests o constructor con id).
        // Si no tienes setter de id, comenta estas líneas y elimina las aserciones de igualdad.
        // a.setId(b.getId()); // <- solo si lo tienes

        // assertEquals(a, b);
        // assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.getId());
        assertNotNull(b.getId());
        assertNotEquals(a.getId(), b.getId()); // por defecto, IDs distintos
    }
}
 {
    
}
