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

    public Incidencia guardar(Incidencia incidencia) {
        entityManager.persist(incidencia);
        return incidencia;
    }

    public Incidencia actualizar(Incidencia incidencia) {
        return entityManager.merge(incidencia);
    }

    public Optional<Incidencia> buscarPorId(UUID id) {
        Incidencia inc = entityManager.find(Incidencia.class, id);
        return Optional.ofNullable(inc);
    }

    public void borrar(Incidencia incidencia) {
        if (entityManager.contains(incidencia)) {
            entityManager.remove(incidencia);
        } else {
            entityManager.remove(entityManager.merge(incidencia));
        }
    }

    public List<Incidencia> buscarTodas() {
        return entityManager.createQuery("SELECT i FROM Incidencia i", Incidencia.class)
                .getResultList();
    }

    // EXTRA: buscar incidencias por login de usuario
    public List<Incidencia> buscarPorLoginUsuario(String login) {
        return entityManager.createQuery(
                        "SELECT i FROM Incidencia i WHERE i.usuario.login = :login",
                        Incidencia.class)
                .setParameter("login", login)
                .getResultList();
    }

    // EXTRA: contar incidencias de un tipo concreto
    public long contarPorTipo(TipoIncidencia tipo) {
        return entityManager.createQuery(
                        "SELECT COUNT(i) FROM Incidencia i WHERE i.tipo = :tipo",
                        Long.class)
                .setParameter("tipo", tipo)
                .getSingleResult();
    }
}
