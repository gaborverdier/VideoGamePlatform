package com.gaming.platform.service;

import com.gaming.platform.model.User;
import com.gaming.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant les opérations sur les utilisateurs.
 *
 * RESPONSABILITÉS :
 * 1. Fournir les opérations CRUD sur les utilisateurs
 * 2. Logique métier liée aux utilisateurs
 * 3. Validation des données
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Récupère tous les utilisateurs.
     *
     * @return Liste de tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupère un utilisateur par son ID.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Optional contenant l'utilisateur si trouvé
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * Récupère un utilisateur par son username.
     *
     * @param username Le nom d'utilisateur
     * @return Optional contenant l'utilisateur si trouvé
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Récupère un utilisateur par son email.
     *
     * @param email L'email
     * @return Optional contenant l'utilisateur si trouvé
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Crée un nouvel utilisateur.
     *
     * @param user L'utilisateur à créer
     * @return L'utilisateur créé
     * @throws IllegalArgumentException si un utilisateur avec ce username/email existe déjà
     */
    @Transactional
    public User createUser(User user) {
        // Vérifications
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username déjà utilisé: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé: " + user.getEmail());
        }

        logger.info("Création d'un nouvel utilisateur: {}", user.getUsername());
        return userRepository.save(user);
    }

    /**
     * Compte le nombre total d'utilisateurs.
     *
     * @return Le nombre d'utilisateurs
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Supprime un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur à supprimer
     */
    @Transactional
    public void deleteUser(String userId) {
        logger.info("Suppression de l'utilisateur: {}", userId);
        userRepository.deleteById(userId);
    }
}
