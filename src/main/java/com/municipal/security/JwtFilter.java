package com.municipal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtre JWT pour l'authentification des requêtes HTTP.
 * 
 * Ce filtre est exécuté UNE FOIS par requête (OncePerRequestFilter) et a pour rôle de :
 * 1. Extraire le token JWT du header "Authorization" de la requête
 * 2. Valider le token (signature, expiration)
 * 3. Extraire les informations utilisateur (email, rôle) du token
 * 4. Créer un contexte de sécurité Spring Security avec ces informations
 * 
 * Workflow d'une requête avec JWT :
 * Client → Envoie requête avec "Authorization: Bearer <token>" 
 *       → JwtFilter intercepte et valide le token
 *       → SecurityContext est peuplé avec les infos utilisateur
 *       → Les contrôleurs peuvent accéder à l'utilisateur authentifié
 *       → Spring Security vérifie les autorisations (@PreAuthorize, SecurityConfig)
 * 
 * Si le token est invalide ou absent, la requête continue mais sans contexte de sécurité
 * (les endpoints protégés retourneront 401 Unauthorized ou 403 Forbidden).
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    /**
     * Méthode principale du filtre, exécutée pour chaque requête HTTP.
     * 
     * @param request La requête HTTP entrante
     * @param response La réponse HTTP sortante
     * @param filterChain La chaîne de filtres à appeler ensuite
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Extraction du header Authorization
        final String authorizationHeader = request.getHeader("Authorization");
        
        String email = null;
        String jwt = null;
        
        // 2. Vérification du format du header : "Bearer <token>"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // Extraction du token (suppression du préfixe "Bearer ")
            jwt = authorizationHeader.substring(7);
            
            try {
                // 3. Extraction de l'email depuis le token
                email = jwtUtil.extractEmail(jwt);
            } catch (Exception e) {
                // Token malformé ou signature invalide
                logger.error("Erreur lors de l'extraction de l'email du token JWT: " + e.getMessage());
            }
        }
        
        // 4. Si un email a été extrait et qu'aucun contexte de sécurité n'existe déjà
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                // 5. Extraction du rôle depuis le token
                String role = jwtUtil.extractRole(jwt);
                
                // 6. Création d'un objet UserDetails avec les informations du token
                // Note : Le mot de passe est vide car nous utilisons JWT (pas besoin de vérifier le mdp à chaque requête)
                UserDetails userDetails = User.builder()
                        .username(email)
                        .password("") // Pas de mot de passe nécessaire avec JWT
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                        .build();
                
                // 7. Validation du token (signature, expiration)
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    
                    // 8. Création d'un objet d'authentification Spring Security
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                            );
                    
                    // Ajout des détails de la requête (IP, session ID, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 9. Enregistrement de l'authentification dans le contexte de sécurité
                    // À partir de maintenant, Spring Security considère l'utilisateur comme authentifié
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Token invalide ou expiré
                logger.error("Erreur lors de la validation du token JWT: " + e.getMessage());
            }
        }
        
        // 10. Passage au filtre suivant dans la chaîne
        // (que le token soit valide ou non, la requête continue)
        filterChain.doFilter(request, response);
    }
}
