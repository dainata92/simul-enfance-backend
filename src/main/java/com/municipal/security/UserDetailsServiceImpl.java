package com.municipal.security;

import com.municipal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service d'implémentation de UserDetailsService pour Spring Security.
 * 
 * Cette classe fait le pont entre notre entité User personnalisée et 
 * l'interface UserDetails requise par Spring Security.
 * 
 * Rôle :
 * - Charger les informations utilisateur depuis la base de données
 * - Convertir notre entité User en objet UserDetails compréhensible par Spring Security
 * - Utilisé par Spring Security lors de l'authentification (login)
 * 
 * UserDetails contient :
 * - username : l'identifiant de l'utilisateur (ici l'email)
 * - password : le mot de passe encodé (pour vérification lors du login)
 * - authorities : les rôles/permissions de l'utilisateur
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Charge un utilisateur par son nom d'utilisateur (ici l'email).
     * 
     * Cette méthode est appelée automatiquement par Spring Security lors :
     * - Du processus de login (AuthenticationManager)
     * - De la validation des credentials
     * 
     * @param email L'email de l'utilisateur (username dans notre cas)
     * @return Un objet UserDetails contenant les informations d'authentification
     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Recherche de l'utilisateur en base de données
        com.municipal.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé avec l'email : " + email
                ));
        
        // 2. Conversion de notre entité User en UserDetails de Spring Security
        return User.builder()
                .username(user.getEmail())              // L'email sert d'identifiant
                .password(user.getPassword())            // Mot de passe encodé BCrypt
                .authorities(Collections.singletonList(  // Liste des autorisations/rôles
                        new SimpleGrantedAuthority(user.getRole().name())
                ))
                .accountExpired(false)                   // Le compte n'est pas expiré
                .accountLocked(false)                    // Le compte n'est pas verrouillé
                .credentialsExpired(false)              // Les credentials ne sont pas expirés
                .disabled(false)                         // Le compte n'est pas désactivé
                .build();
    }
}
