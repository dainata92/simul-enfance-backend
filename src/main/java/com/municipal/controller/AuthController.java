package com.municipal.controller;

import com.municipal.dto.AuthResponse;
import com.municipal.dto.LoginRequest;
import com.municipal.dto.RegisterRequest;
import com.municipal.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion de l'authentification.
 * 
 * Expose les endpoints pour :
 * - L'inscription (register)
 * - La connexion (login)
 * 
 * Ces endpoints sont publics (pas d'authentification requise).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // À configurer selon vos besoins CORS
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Endpoint d'inscription pour créer un nouveau compte utilisateur.
     * 
     * POST /api/auth/register
     * Body : { "email": "user@example.com", "password": "motdepasse" }
     * 
     * @param request Les informations d'inscription
     * @return Le token JWT et les informations utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    /**
     * Endpoint de connexion pour authentifier un utilisateur existant.
     * 
     * POST /api/auth/login
     * Body : { "email": "user@example.com", "password": "motdepasse" }
     * 
     * @param request Les credentials de connexion
     * @return Le token JWT et les informations utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
