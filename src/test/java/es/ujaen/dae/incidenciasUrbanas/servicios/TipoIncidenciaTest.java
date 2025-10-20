public package es.ujaen.dae.incidenciasUrbanas.entidades;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TipoIncidenciaTest {

    private Usuario usuarioDemo(String login) {
        return new Usuario(
                100, "Nombre", "Apellidos",
                LocalDate.of(1998, 1, 1),
                "Dirección X",
                "600000001",
                login + "@example.com",
                login,
                "pwd"
        );
    }

    @Test
    void eliminarTipo_true_siNoEstáEnUso() {
        var t = new TipoIncidencia("Iluminación", "Farolas");
        var incidencias = new ArrayList<Incidencia>();

        assertTrue(TipoIncidencia.eliminarTipo(t, incidencias));
    }

    @Test
    void eliminarTipo_true_siListaTieneIncidenciasPeroNingunaConEseTipo() {
        var t   = new TipoIncidencia("Aceras", "Losetas");
        var t2  = new TipoIncidencia("Parque", "Columpios");
        var incs = new ArrayList<Incidencia>();

        incs.add(new Incidencia(usuarioDemo("userA"), t2, "Columpio roto", "Parque Norte", null));

        // Aunque hay incidencias, ninguna usa 't'
        assertTrue(TipoIncidencia.eliminarTipo(t, incs));
    }

    @Test
    void eliminarTipo_false_siAlgunaIncidenciaUsaEseTipo() {
        var t = new TipoIncidencia("Suciedad", "Basuras");
        var incs = new ArrayList<Incidencia>();

        incs.add(new Incidencia(usuarioDemo("userB"), t, "Papelera llena", "Calle Sur 12", null));

        assertFalse(TipoIncidencia.eliminarTipo(t, incs));
    }

    @Test
    void equalsHashCode_basicoPorId_siLoImplementas() {
        // Igual que en IncidenciaTest: solo si has sobreescrito equals/hashCode en TipoIncidencia.
        var a = new TipoIncidencia("Señales", "Tráfico");
        var b = new TipoIncidencia("Señales", "Tráfico");

        assertNotNull(a.getId());
        assertNotNull(b.getId());
        assertNotEquals(a.getId(), b.getId()); // por defecto distintos
    }
}
 {
    
}
