package com.municipal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse contenant les informations d'un utilisateur.
 * Utilisé par l'endpoint GET /api/users/{id}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    /**
     * ID de l'utilisateur.
     */
    private Long id;
    
    /**
     * Email de l'utilisateur.
     */
    private String email;
    
    /**
     * Prénom de l'utilisateur.
     */
    private String firstName;
    
    /**
     * Nom de famille de l'utilisateur.
     */
    private String lastName;
    
    /**
     * Nom complet de l'utilisateur.
     */
    private String name;
    
    /**
     * Rôle de l'utilisateur.
     */
    private String role;
    
    /**
     * Photo de profil de l'utilisateur encodée en base64.
     */
    private String profilePicture;
}
