package com.municipal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuration centrale de Spring Security pour l'application.
 * 
 * Cette classe configure :
 * 1. L'authentification : Comment les utilisateurs se connectent (JWT)
 * 2. L'autorisation : Qui peut accéder à quelles routes
 * 3. La gestion des sessions : Mode stateless (sans session) pour JWT
 * 4. L'encodage des mots de passe : BCrypt pour la sécurité
 * 5. Les filtres de sécurité : JwtFilter pour valider les tokens
 * 
 * Architecture de sécurité :
 * 
 * Requête HTTP → JwtFilter (validation token) → SecurityFilterChain (vérification accès)
 *                     ↓                                    ↓
 *              SecurityContext                        Controller
 *            (utilisateur authentifié)            (traitement métier)
 * 
 * Règles d'accès définies :
 * - /api/auth/** : Accessible à tous (login, register)
 * - /api/admin/** : Réservé aux ROLE_ADMIN uniquement
 * - /api/user/** : Accessible aux utilisateurs authentifiés
 * - Toutes les autres routes : Authentification requise
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Active les annotations @PreAuthorize, @Secured, etc.
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtFilter jwtFilter;
    private final UserDetailsServiceImpl userDetailsService;
    
    /**
     * Configuration principale de la sécurité HTTP.
     * 
     * Définit les règles d'autorisation pour chaque route et configure
     * le mode de fonctionnement de Spring Security (stateless pour JWT).
     * 
     * @param http L'objet HttpSecurity pour configurer la sécurité
     * @return La chaîne de filtres de sécurité configurée
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactivation de CSRF car nous utilisons JWT (tokens stateless)
                // CSRF est surtout utile pour les sessions avec cookies
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configuration des autorisations par route
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques : login et inscription accessibles sans authentification
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // Routes de calcul et villes accessibles publiquement (pour le frontend)
                        .requestMatchers("/api/calculate/**", "/api/cities/**", "/api/pricing/**").permitAll()
                        
                        // Routes de nettoyage et initialisation accessibles publiquement (temporaire pour développement)
                        .requestMatchers("/api/simulations/cleanup", "/api/simulations/remove-duplicates").permitAll()
                        .requestMatchers("/api/admin/init-mercredi-perreux", "/api/admin/init-restauration-perreux").permitAll()  // Init des données
                        
                        // Routes admin : réservées aux utilisateurs avec le rôle ROLE_ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Routes utilisateur : accessibles à tous les utilisateurs authentifiés
                        .requestMatchers("/api/user/**", "/api/users/**", "/api/simulations/**").authenticated()
                        
                        // Toutes les autres routes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                
                // Configuration de la gestion des sessions
                // STATELESS : Aucune session HTTP n'est créée (tout repose sur le token JWT)
                // Chaque requête est indépendante et doit fournir un token valide
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Configuration du provider d'authentification personnalisé
                .authenticationProvider(authenticationProvider())
                
                // Ajout du filtre JWT AVANT le filtre d'authentification standard
                // Ordre d'exécution : JwtFilter → UsernamePasswordAuthenticationFilter → Controller
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Configuration CORS pour autoriser les requêtes depuis le frontend Angular.
     * 
     * Autorise :
     * - Origin : http://localhost:4200 (frontend Angular)
     * - Méthodes : GET, POST, PUT, DELETE, OPTIONS
     * - Headers : Authorization, Content-Type, etc.
     * - Credentials : Cookies et headers d'authentification
     * 
     * @return La source de configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * Encodeur de mots de passe utilisant l'algorithme BCrypt.
     * 
     * BCrypt :
     * - Génère un hash unique à chaque encodage (grâce au salt aléatoire)
     * - Lent par design pour ralentir les attaques par force brute
     * - Produit un hash de 60 caractères incluant : algorithme, coût, salt et hash
     * 
     * Exemple de hash BCrypt :
     * $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
     * └─┬┘└┬┘└───────────────┬───────────────┘└──────────────┬──────────────┘
     *  Algo Coût        Salt (22 chars)            Hash (31 chars)
     * 
     * @return L'encodeur BCrypt configuré
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Provider d'authentification qui combine UserDetailsService et PasswordEncoder.
     * 
     * Processus d'authentification lors du login :
     * 1. AuthenticationManager reçoit email + password
     * 2. DaoAuthenticationProvider charge l'utilisateur via UserDetailsService
     * 3. Compare le password fourni avec celui en base (après encodage BCrypt)
     * 4. Si OK → Retourne Authentication avec les roles
     *    Si KO → Lance une exception BadCredentialsException
     * 
     * @return Le provider d'authentification configuré
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * Manager d'authentification utilisé pour valider les credentials lors du login.
     * 
     * Exposé en tant que Bean pour être injecté dans AuthService
     * et permettre l'authentification programmatique.
     * 
     * @param config Configuration d'authentification de Spring Security
     * @return Le manager d'authentification
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
