package com.municipal.security;

import com.municipal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service de sécurité pour les vérifications personnalisées d'accès aux données.
 * 
 * Utilisé avec @PreAuthorize pour vérifier que l'utilisateur accède à ses propres données.
 * Exemple : @PreAuthorize("@userSecurityService.isOwner(#userId, authentication.principal.username)")
 */
@Service
@RequiredArgsConstructor
public class UserSecurityService {
    
    private final UserRepository userRepository;
    
    /**
     * Vérifie si l'utilisateur authentifié est le propriétaire des données.
     * 
     * @param userId L'ID de l'utilisateur dont on veut accéder aux données
     * @param email L'email de l'utilisateur authentifié
     * @return true si l'utilisateur est le propriétaire, false sinon
     */
    public boolean isOwner(Long userId, String email) {
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(email))
                .orElse(false);
    }
}
