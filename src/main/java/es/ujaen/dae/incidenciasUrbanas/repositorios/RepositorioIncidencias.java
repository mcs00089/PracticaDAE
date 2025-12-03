package es.ujaen.dae.incidenciasUrbanas.repositorios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Estado;
import es.ujaen.dae.incidenciasUrbanas.entidades.Incidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.TipoIncidencia;
import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RepositorioIncidencias {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Guarda una incidencia en la base de datos.
     */
    @Transactional
    public Incidencia guardar(Incidencia incidencia) {
        entityManager.persist(incidencia);
        return incidencia;
    }

    /**
     * Actualiza una incidencia ya existente.
     */
    @Transactional
    public Incidencia actualizar(Incidencia incidencia) {
        return entityManager.merge(incidencia);
    }

    /**
     * Busca una incidencia por su ID.
     */
    public Optional<Incidencia> buscarPorId(int id) {
        Incidencia inc = entityManager.find(Incidencia.class, id);
        return Optional.ofNullable(inc);
    }

    /**
     * Borra una incidencia.
     */
    @Transactional
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

    public List<Incidencia> buscarPorUsuario(Usuario usuario) {
        return entityManager.createQuery(
                        "SELECT i FROM Incidencia i WHERE i.usuario = :u", Incidencia.class)
                .setParameter("u", usuario)
                .getResultList();
    }

    public List<Incidencia> buscarPorTipoYEstado(TipoIncidencia tipo, Estado estado) {
        StringBuilder jpql = new StringBuilder("SELECT i FROM Incidencia i WHERE 1=1");

        if (tipo != null) {
            jpql.append(" AND i.tipo = :tipo");
        }
        if (estado != null) {
            jpql.append(" AND i.estado = :estado");
        }

        var query = entityManager.createQuery(jpql.toString(), Incidencia.class);

        if (tipo != null) {
            query.setParameter("tipo", tipo);
        }
        if (estado != null) {
            query.setParameter("estado", estado);
        }

        return query.getResultList();
    }

    public List<Incidencia> buscarPorEstados(List<Estado> estados) {
        return entityManager.createQuery(
                        "SELECT i FROM Incidencia i WHERE i.estado IN :estados", Incidencia.class)
                .setParameter("estados", estados)
                .getResultList();
    }

}
