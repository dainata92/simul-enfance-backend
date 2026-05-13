package com.municipal.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
 * - /auth/** : Accessible à tous (login, register)
 * - /api/** : Réservé aux ROLE_USER ou ROLE_ADMIN
 * - Toutes les autres routes : Authentification requise
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${allowCorsOrigin}")
    private String allowCorsOrigin;
    
    @Autowired
    private JwtFilter jwtFilter;
    
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Désactivation de CSRF car nous utilisons JWT (tokens stateless)
                // CSRF est surtout utile pour les sessions avec cookies
                .csrf(csrf -> csrf.disable())
                
                // Désactivation de HTTP Basic (nous utilisons uniquement JWT)
                .httpBasic(httpBasic -> httpBasic.disable())
                
                // Désactivation de Form Login (nous utilisons uniquement JWT)
                .formLogin(formLogin -> formLogin.disable())
                
                // Configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configuration des autorisations par route
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight : autoriser toutes les requêtes OPTIONS sans authentification
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Routes publiques : login et inscription accessibles sans authentification
                        .requestMatchers("/auth/**").permitAll()
                        
                        // Routes publiques : calculateur et liste des villes accessibles sans authentification
                        .requestMatchers("/cities/**", "/calculate/**").permitAll()
                        
                        // Routes admin : réservées aux utilisateurs avec ROLE_ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // Toutes les autres routes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                
                // Configuration de la gestion des sessions
                // STATELESS : Aucune session HTTP n'est créée (tout repose sur le token JWT)
                // Chaque requête est indépendante et doit fournir un token valide
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        // Ajout du filtre JWT AVANT le filtre d'authentification standard
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
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
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowCorsOrigin));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
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
