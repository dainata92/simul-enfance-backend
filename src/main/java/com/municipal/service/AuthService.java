package com.municipal.service;

import com.municipal.dto.AuthResponse;
import com.municipal.dto.LoginRequest;
import com.municipal.dto.RegisterRequest;
import com.municipal.entity.Role;
import com.municipal.entity.User;
import com.municipal.repository.UserRepository;
import com.municipal.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification pour gérer le login et l'inscription.
 * 
 * Ce service est responsable de :
 * - L'inscription de nouveaux utilisateurs (register)
 * - L'authentification des utilisateurs existants (login)
 * - La génération des tokens JWT après authentification
 * 
 * Workflow d'authentification :
 * 
 * 1. INSCRIPTION (Register) :
 *    Client → Envoie email + password
 *          → AuthService vérifie que l'email n'existe pas
 *          → Encode le password avec BCrypt
 *          → Sauvegarde l'utilisateur en BDD
 *          → Génère un token JWT
 *          → Retourne token + infos utilisateur
 * 
 * 2. CONNEXION (Login) :
 *    Client → Envoie email + password
 *          → AuthenticationManager valide les credentials
 *          → Si OK : génère un token JWT
 *          → Retourne token + infos utilisateur
 *          → Client utilise ce token pour les requêtes suivantes
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Inscrit un nouvel utilisateur dans le système.
     * 
     * Processus :
     * 1. Vérifie que l'email n'est pas déjà utilisé
     * 2. Encode le mot de passe avec BCrypt
     * 3. Crée un nouvel utilisateur avec le rôle ROLE_USER par défaut
     * 4. Sauvegarde en base de données
     * 5. Génère un token JWT pour l'utilisateur
     * 6. Retourne le token et les informations utilisateur
     * 
     * @param request Les informations d'inscription (email, password)
     * @return La réponse contenant le token JWT et les infos utilisateur
     * @throws RuntimeException Si l'email existe déjà
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un compte existe déjà avec cet email");
        }
        
        // 2. Création du nouvel utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        // Encodage du mot de passe avec BCrypt (JAMAIS stocker en clair !)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Attribution du rôle USER par défaut (ADMIN doit être créé manuellement ou via endpoint admin)
        user.setRole(Role.ROLE_USER);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setProfilePicture(request.getProfilePicture());
        
        // 3. Sauvegarde en base de données
        user = userRepository.save(user);
        
        // 4. Génération du token JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        // 5. Construction de la réponse avec id et nom complet
        String fullName = buildFullName(user.getFirstName(), user.getLastName());
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setId(user.getId());
        response.setName(fullName);
        response.setProfilePicture(user.getProfilePicture());
        return response;
    }
    
    /**
     * Authentifie un utilisateur existant et génère un token JWT.
     * 
     * Processus :
     * 1. Utilise AuthenticationManager pour valider les credentials
     *    - Charge l'utilisateur depuis la BDD via UserDetailsService
     *    - Compare le password fourni avec le hash BCrypt en BDD
     * 2. Si les credentials sont valides, récupère l'utilisateur
     * 3. Génère un token JWT contenant email et rôle
     * 4. Retourne le token et les informations utilisateur
     * 
     * @param request Les credentials de connexion (email, password)
     * @return La réponse contenant le token JWT et les infos utilisateur
     * @throws org.springframework.security.authentication.BadCredentialsException Si les credentials sont invalides
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Authentification via Spring Security
        // Lance une exception BadCredentialsException si email ou password incorrect
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        // 2. Si on arrive ici, l'authentification a réussi
        // Récupération des détails de l'utilisateur authentifié
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // 3. Récupération de l'utilisateur complet depuis la BDD
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // 4. Génération du token JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        // 5. Construction de la réponse avec id et nom complet
        String fullName = buildFullName(user.getFirstName(), user.getLastName());
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setId(user.getId());
        response.setName(fullName);        response.setProfilePicture(user.getProfilePicture());        return response;
    }
    
    /**
     * Construit le nom complet à partir du prénom et du nom.
     * Gère les cas où firstName ou lastName sont null/vides.
     * 
     * @param firstName Le prénom
     * @param lastName Le nom de famille
     * @return Le nom complet ou une chaîne vide si les deux sont null/vides
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
