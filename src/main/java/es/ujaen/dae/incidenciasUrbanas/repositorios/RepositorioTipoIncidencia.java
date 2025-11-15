package es.ujaen.dae.incidenciasUrbanas.repositorios;

import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Transactional
@Repository
public class RepositorioTipoIncidencia {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Guarda un nuevo tipo de incidencia en la base de datos.
     * @param tipo TipoIncidencia a guardar
     * @return TipoIncidencia guardado
     */
    public TipoIncidencia guardar(TipoIncidencia tipo) {
        entityManager.persist(tipo);
        return tipo;
    }

    /**
     * Actualiza un tipo de incidencia existente.
     * @param tipo TipoIncidencia con los datos actualizados
     * @return TipoIncidencia actualizado
     */
    public TipoIncidencia actualizar(TipoIncidencia tipo) {
        return entityManager.merge(tipo);
    }

    /**
     * Busca un tipo de incidencia por su ID (UUID).
     * @param id El UUID del tipo de incidencia
     * @return Optional con el tipo de incidencia si existe
     */
    public Optional<TipoIncidencia> buscarPorId(UUID id) {
        TipoIncidencia tipo = entityManager.find(TipoIncidencia.class, id);
        return Optional.ofNullable(tipo);
    }

    /**
     * Busca un tipo de incidencia por su nombre.
     * @param nombre El nombre del tipo de incidencia
     * @return Optional con el tipo de incidencia si existe
     */
    public Optional<TipoIncidencia> buscarPorNombre(String nombre) {
        try {
            TipoIncidencia tipo = entityManager.createQuery(
                            "SELECT t FROM TipoIncidencia t WHERE t.nombre = :nombre", TipoIncidencia.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
            return Optional.of(tipo);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Borra un tipo de incidencia de la base de datos.
     * @param tipo El tipo de incidencia a borrar
     */
    public void borrar(TipoIncidencia tipo) {
        // Nos aseguramos de que la entidad esté gestionada (attached) antes de borrar
        if (entityManager.contains(tipo)) {
            entityManager.remove(tipo);
        } else {
            entityManager.remove(entityManager.merge(tipo));
        }
    }
}
