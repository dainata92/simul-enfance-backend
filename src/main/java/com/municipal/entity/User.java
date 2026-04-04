package com.municipal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité représentant un utilisateur dans le système.
 * 
 * Cette classe contient les informations d'identification et d'autorisation :
 * - id : Identifiant unique généré automatiquement
 * - email : Adresse email unique utilisée comme identifiant de connexion
 * - password : Mot de passe encodé avec BCrypt (jamais stocké en clair)
 * - role : Rôle de l'utilisateur (ROLE_USER ou ROLE_ADMIN)
 * 
 * La table associée est 'users' (nom au pluriel pour éviter les conflits avec le mot-clé SQL USER).
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * Identifiant unique de l'utilisateur, généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Prénom de l'utilisateur.
     */
    @Column
    private String firstName;
    
    /**
     * Nom de famille de l'utilisateur.
     */
    @Column
    private String lastName;
    
    /**
     * Adresse email de l'utilisateur, utilisée comme identifiant de connexion.
     * Doit être unique dans la base de données et respecter le format email.
     */
    @Column(unique = true, nullable = false)
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
    
    /**
     * Mot de passe encodé avec BCrypt.
     * IMPORTANT : Ne jamais stocker ou transmettre le mot de passe en clair.
     * L'encodage BCrypt génère un hash de 60 caractères avec un salt unique.
     */
    @Column(nullable = false)
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
    
    /**
     * Rôle de l'utilisateur définissant ses permissions dans le système.
     * Stocké en base comme une chaîne de caractères (EnumType.STRING) 
     * pour faciliter la lecture et éviter les problèmes lors de modifications de l'enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    /**
     * Photo de profil de l'utilisateur encodée en base64.
     * Peut être null si l'utilisateur n'a pas défini de photo.
     */
    @Column(columnDefinition = "TEXT")
    private String profilePicture;
}
