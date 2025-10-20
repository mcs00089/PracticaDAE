package es.ujaen.dae.incidenciasUrbanas.servicios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Estado;
import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import es.ujaen.dae.incidenciasUrbanas.excepciones.BorrarIncidenciaNoPendiente;
import es.ujaen.dae.incidenciasUrbanas.excepciones.CredencialesInvalidas;
import es.ujaen.dae.incidenciasUrbanas.excepciones.IncidenciaNoEncontrada;
import es.ujaen.dae.incidenciasUrbanas.excepciones.UsuarioNoEncontrado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestServicioIncidencias_Borrado {

    private ServicioIncidencias servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioIncidencias();

        // Registrar admin (login = "admin", clave = "admin123")
        var admin = new Usuario(
                0, "Admin", "Administrador",
                LocalDate.of(1990, 1, 1),
                "Plaza Ayuntamiento",
                "657232313",
                "admin@ayuntamiento.es",
                "admin",
                "admin123"
        );
        servicio.registrarUsuario(admin);

        // Registrar un usuario normal
        var user = new Usuario(
                1, "Eva", "Q",
                LocalDate.of(1995, 2, 2),
                "C/ 2",
                "600000111",
                "eva@example.com",
                "eva",
                "pwd"
        );
        servicio.registrarUsuario(user);
    }

    /** Inyecta un TipoIncidencia en el mapa privado de ServicioIncidencias (no hay API pública aún). */
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
    void propietarioSoloPuedeBorrarSiPendiente_yAdminSiemprePuede() {
        UUID tipo = seedTipo("Parque", "Columpio roto");

        var incPend = servicio.registrarIncidencia("eva", tipo, "pendiente", "dir", null);
        var incNoPend = servicio.registrarIncidencia("eva", tipo, "no pendiente", "dir", null);
        incNoPend.actualizarEstado(Estado.EN_EVALUACION); // ya no es PENDIENTE

        // Propietaria (eva) borra si está PENDIENTE
        assertDoesNotThrow(() -> servicio.borrarIncidencia("eva", incPend.getId()));

        // Propietaria NO puede borrar si no está PENDIENTE
        assertThrows(BorrarIncidenciaNoPendiente.class,
                () -> servicio.borrarIncidencia("eva", incNoPend.getId()));

        // Admin sí puede borrar en cualquier estado
        assertDoesNotThrow(() -> servicio.borrarIncidencia("admin", incNoPend.getId()));
    }

    @Test
    void erroresEnBorrado_incidenciaNoExiste_usuarioNoExiste_yNoDueno() {
        UUID tipo = seedTipo("Mobiliario", "Banco roto");
        var inc = servicio.registrarIncidencia("eva", tipo, "banco roto", "plaza", null);

        // Incidencia no existe
        assertThrows(IncidenciaNoEncontrada.class,
                () -> servicio.borrarIncidencia("eva", UUID.randomUUID()));

        // Usuario no existe
        assertThrows(UsuarioNoEncontrado.class,
                () -> servicio.borrarIncidencia("nadie", inc.getId()));

        // Otro usuario no dueño y no admin → permiso denegado
        var otro = new Usuario(
                2, "Luis", "Soto",
                LocalDate.of(2000, 1, 1),
                "C/ Ejemplo 1",
                "600000000",
                "luis@example.com",
                "luis",
                "clave"
        );
        servicio.registrarUsuario(otro);

        assertThrows(CredencialesInvalidas.class,
                () -> servicio.borrarIncidencia("luis", inc.getId()));
    }
}
