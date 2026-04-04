package com.municipal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la mise à jour du profil utilisateur.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    /**
     * Email de l'utilisateur.
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
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
     * Photo de profil de l'utilisateur encodée en base64.
     */
    private String profilePicture;
}