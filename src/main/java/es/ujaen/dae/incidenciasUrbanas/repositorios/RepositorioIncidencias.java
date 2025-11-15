package es.ujaen.dae.incidenciasUrbanas.repositorios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Estado;
import es.ujaen.dae.incidenciasUrbanas.entidades.Incidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Repository
public class RepositorioIncidencias {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Guarda una incidencia en la base de datos.
     */
    public Incidencia guardar(Incidencia incidencia) {
        entityManager.persist(incidencia);
        return incidencia;
    }

    /**
     * Actualiza una incidencia ya existente.
     */
    public Incidencia actualizar(Incidencia incidencia) {
        return entityManager.merge(incidencia);
    }

    /**
     * Busca una incidencia por su ID.
     */
    public Optional<Incidencia> buscarPorId(UUID id) {
        Incidencia inc = entityManager.find(Incidencia.class, id);
        return Optional.ofNullable(inc);
    }

    /**
     * Borra una incidencia.
     */
    public void borrar(Incidencia incidencia) {
        if (entityManager.contains(incidencia)) {
            entityManager.remove(incidencia);
        } else {
            entityManager.remove(entityManager.merge(incidencia));
        }
    }

    /**
     * Devuelve TODAS las incidencias.
     */
    public List<Incidencia> buscarTodas() {
        return entityManager.createQuery(
                        "SELECT i FROM Incidencia i", Incidencia.class)
                .getResultList();
    }

    /**
     * Cuenta cuántas incidencias usan un TipoIncidencia.
     * Sirve para impedir borrar un tipo en uso.
     */
    public long contarPorTipo(TipoIncidencia tipo) {
        return entityManager.createQuery(
                        "SELECT COUNT(i) FROM Incidencia i WHERE i.tipo = :tipo", Long.class)
                .setParameter("tipo", tipo)
                .getSingleResult();
    }

    /**
     * Buscar por estado (si lo necesitas).
     */
    public List<Incidencia> buscarPorEstado(Estado estado) {
        return entityManager.createQuery(
                        "SELECT i FROM Incidencia i WHERE i.estado = :estado", Incidencia.class)
                .setParameter("estado", estado)
                .getResultList();
    }
}
