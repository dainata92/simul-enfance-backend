package com.municipal.service;

import com.municipal.dto.UserResponse;
import com.municipal.dto.UserUpdateRequest;
import com.municipal.entity.User;
import com.municipal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service pour la gestion des utilisateurs.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Récupère un utilisateur par son ID.
     * 
     * @param id L'ID de l'utilisateur
     * @return Les informations de l'utilisateur
     * @throws RuntimeException Si l'utilisateur n'est pas trouvé
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé avec l'ID : " + id));
        
        return mapToUserResponse(user);
    }

    /**
     * Met à jour le profil d'un utilisateur.
     *
     * @param id L'ID de l'utilisateur
     * @param request Les données à mettre à jour
     * @return Les informations mises à jour de l'utilisateur
     */
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé avec l'ID : " + id));

        String newEmail = request.getEmail() != null ? request.getEmail().trim() : null;
        if (newEmail == null || newEmail.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'email est obligatoire");
        }

        if (!newEmail.equalsIgnoreCase(user.getEmail())) {
            boolean emailExists = userRepository.findByEmail(newEmail)
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .isPresent();
            if (emailExists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cet email est déjà utilisé par un autre utilisateur");
            }
            user.setEmail(newEmail);
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        // Mise à jour de la photo de profil si fournie
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }
    
    /**
     * Mappe une entité User vers un DTO UserResponse.
     * 
     * @param user L'entité User
     * @return Le DTO UserResponse
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setName(buildFullName(user.getFirstName(), user.getLastName()));
        response.setRole(user.getRole().name());
        response.setProfilePicture(user.getProfilePicture());
        return response;
    }
    
    /**
     * Construit le nom complet à partir du prénom et du nom.
     * 
     * @param firstName Le prénom
     * @param lastName Le nom de famille
     * @return Le nom complet
     */
    private String buildFullName(String firstName, String lastName) {
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        } else if (firstName != null && !firstName.isEmpty()) {
            return firstName;
        } else if (lastName != null && !lastName.isEmpty()) {
            return lastName;
        }
        return "";
    }
}
