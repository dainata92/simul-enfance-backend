package com.municipal.controller;

import com.municipal.dto.UserResponse;
import com.municipal.dto.UserUpdateRequest;
import com.municipal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Contrôleur pour les endpoints utilisateur.
 * 
 * Routes /api/user/** accessibles aux utilisateurs authentifiés.
 * Routes /api/users/** accessibles à tous (avec authentification).
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Récupère les informations d'un utilisateur par son ID.
     * 
     * GET /api/users/{id}
     * Header : Authorization: Bearer <token>
     * 
     * @param id L'ID de l'utilisateur
     * @return Les informations de l'utilisateur
     */
    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Met à jour le profil d'un utilisateur.
     *
     * PUT /api/users/{id}
     * Header : Authorization: Bearer <token>
     *
     * Accessible au propriétaire du profil ou à un admin.
     *
     * @param id L'ID de l'utilisateur
     * @param request Les données de mise à jour
     * @return Les informations mises à jour de l'utilisateur
     */
    @PutMapping("/api/users/{id}")
    @PreAuthorize("@userSecurityService.isOwner(#id, authentication.principal.username) or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
    
    /**
     * Récupère les informations du profil de l'utilisateur connecté.
     * 
     * GET /api/user/profile
     * Header : Authorization: Bearer <token>
     * 
     * @param userDetails Les détails de l'utilisateur authentifié (injecté automatiquement)
     * @return Les informations du profil
     */
    @GetMapping("/api/user/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok()
                .body("Profil de l'utilisateur : " + userDetails.getUsername() + 
                      " | Rôle : " + userDetails.getAuthorities());
    }
    
    /**
     * Exemple d'endpoint protégé accessible à l'utilisateur connecté.
     * 
     * GET /api/user/{userId}
     * 
     * Note : Pour vérifier que l'utilisateur accède uniquement à ses propres données,
     * vous pouvez ajouter une validation dans la méthode.
     */
    @GetMapping("/api/user/{userId}")
    public ResponseEntity<?> getUserData(@PathVariable Long userId) {
        return ResponseEntity.ok()
                .body("Données de l'utilisateur " + userId);
    }
}
