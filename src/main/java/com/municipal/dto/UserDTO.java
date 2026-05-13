package com.municipal.dto;

import com.municipal.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la représentation d'un utilisateur dans les réponses API.
 * Inclut un champ 'name' calculé à partir de firstName et lastName.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    private String email;
    private String role;
    private String name;
    private String firstName;
    private String lastName;
    private String profilePicture;
    
    /**
     * Convertit une entité User en UserDTO.
     * Construit automatiquement le champ 'name' à partir de firstName et lastName.
     */
    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setName(buildFullName(user.getFirstName(), user.getLastName(), user.getEmail()));
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }
    
    /**
     * Construit le nom complet à partir du prénom et du nom.
     * Utilise l'email comme fallback si firstName et lastName sont vides.
     */
    private static String buildFullName(String firstName, String lastName, String email) {
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        } else if (firstName != null && !firstName.isEmpty()) {
            return firstName;
        } else if (lastName != null && !lastName.isEmpty()) {
            return lastName;
        }
        // Fallback : utilise la partie avant @ de l'email
        return email != null ? email.split("@")[0] : "";
    }
}
