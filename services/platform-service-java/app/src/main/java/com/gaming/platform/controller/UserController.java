package com.gaming.platform.controller;

import com.gaming.platform.model.User;
import com.gaming.platform.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller pour la gestion des utilisateurs.
 *
 * ENDPOINTS DISPONIBLES :
 * GET    /api/users              Liste tous les utilisateurs
 * GET    /api/users/{id}         Détails d'un utilisateur
 * GET    /api/users/search?username=X  Recherche par username
 * GET    /api/users/stats        Statistiques des utilisateurs
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Liste tous les utilisateurs.
     *
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        logger.info("Récupération de {} utilisateurs", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Récupère les détails d'un utilisateur.
     *
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Recherche un utilisateur par username.
     *
     * GET /api/users/search?username=player123
     */
    @GetMapping("/search")
    public ResponseEntity<User> searchUser(@RequestParam String username) {
        return userService.getUserByUsername(username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Statistiques des utilisateurs.
     *
     * GET /api/users/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        long totalUsers = userService.countUsers();

        Map<String, Object> stats = Map.of(
            "totalUsers", totalUsers,
            "message", "Statistiques des utilisateurs de la plateforme"
        );

        return ResponseEntity.ok(stats);
    }
}
