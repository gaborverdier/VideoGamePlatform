package com.gaming.platform.repository;

import com.gaming.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité User.
 *
 * Fournit les opérations CRUD de base et des méthodes de recherche personnalisées.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Recherche un utilisateur par son nom d'utilisateur.
     *
     * @param username Le nom d'utilisateur à rechercher
     * @return Optional contenant l'utilisateur si trouvé
     */
    Optional<User> findByUsername(String username);

    /**
     * Recherche un utilisateur par son email.
     *
     * @param email L'email à rechercher
     * @return Optional contenant l'utilisateur si trouvé
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un utilisateur existe avec ce username.
     *
     * @param username Le nom d'utilisateur à vérifier
     * @return true si l'utilisateur existe
     */
    boolean existsByUsername(String username);

    /**
     * Vérifie si un utilisateur existe avec cet email.
     *
     * @param email L'email à vérifier
     * @return true si l'email existe
     */
    boolean existsByEmail(String email);
}
