package es.ujaen.dae.incidenciasUrbanas.repositorios;

import es.ujaen.dae.incidenciasUrbanas.entidades.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Transactional
@Repository
public class RepositorioUsuarios {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Guarda un nuevo usuario en la base de datos.
     * @param usuario Usuario a guardar
     * @return Usuario guardado
     */
    public Usuario guardar(Usuario usuario) {
        entityManager.persist(usuario);
        return usuario;
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     * @param usuario Usuario con los datos actualizados
     * @return Usuario actualizado
     */
    public Usuario actualizar(Usuario usuario) {
        return entityManager.merge(usuario);
    }


    /**
     * Busca un usuario por su login.
     * @param login Login del usuario
     * @return Optional con el usuario si existe
     */
    public Optional<Usuario> buscarPorLogin(String login) {
        try {
            Usuario usuario = entityManager.createQuery(
                            "SELECT u FROM Usuario u WHERE u.login = :login", Usuario.class)
                    .setParameter("login", login)
                    .getSingleResult();
            return Optional.of(usuario);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    /**
     * Verifica si existe un usuario con el login que le pasamos.
     * @param login Login a verificar
     * @return true si existe
     */
    public boolean existeLogin(String login) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(u) FROM Usuario u WHERE u.login = :login", Long.class)
                .setParameter("login", login)
                .getSingleResult();
        return count > 0;
    }

}
