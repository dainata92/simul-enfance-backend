package com.municipal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse d'authentification (login ou register).
 * 
 * Retourne le token JWT et les informations de l'utilisateur connecté.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * Token JWT à utiliser dans le header Authorization des requêtes suivantes.
     * Format : "Bearer <token>"
     */
    private String token;
    
    /**
     * Email de l'utilisateur connecté.
     */
    private String email;
    
    /**
     * Rôle de l'utilisateur (ROLE_USER ou ROLE_ADMIN).
     */
    private String role;
    
    /**
     * ID de l'utilisateur.
     */
    private Long id;
    
    /**
     * Prénom de l'utilisateur.
     */
    private String firstName;
    
    /**
     * Nom de famille de l'utilisateur.
     */
    private String lastName;
    
    /**
     * Nom complet de l'utilisateur (prénom + nom).
     */
    private String name;
    
    /**
     * Photo de profil de l'utilisateur encodée en base64.
     */
    private String profilePicture;
}
