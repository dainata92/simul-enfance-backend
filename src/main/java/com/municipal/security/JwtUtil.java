package com.municipal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilitaire pour la gestion des tokens JWT (JSON Web Token).
 * 
 * Cette classe est responsable de :
 * - Générer des tokens JWT sécurisés pour l'authentification
 * - Extraire les informations (claims) des tokens
 * - Valider l'authenticité et la validité des tokens
 * 
 * Le JWT contient :
 * - subject : l'email de l'utilisateur
 * - role : le rôle de l'utilisateur (ROLE_USER ou ROLE_ADMIN)
 * - iat (issued at) : date de création du token
 * - exp (expiration) : date d'expiration du token
 * 
 * Structure d'un JWT : header.payload.signature
 * Exemple : eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.signature
 */
@Component
public class JwtUtil {
    
    /**
     * Clé secrète utilisée pour signer les tokens JWT.
     * Elle est injectée depuis le fichier application.properties.
     * IMPORTANT : Cette clé doit être longue (minimum 256 bits pour HS256) et gardée secrète.
     */
    @Value("${jwt.secret}")
    private String secret;
    
    /**
     * Durée de validité du token en millisecondes.
     * Par défaut : 86400000 ms = 24 heures
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;
    
    /**
     * Génère la clé de signature à partir du secret.
     * Utilise l'algorithme HMAC-SHA256.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Génère un token JWT pour un utilisateur authentifié.
     * 
     * @param email L'adresse email de l'utilisateur (utilisée comme subject)
     * @param role Le rôle de l'utilisateur (ajouté dans les claims)
     * @return Le token JWT signé sous forme de chaîne de caractères
     */
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Extrait l'email (subject) du token JWT.
     * 
     * @param token Le token JWT
     * @return L'email de l'utilisateur
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrait le rôle de l'utilisateur du token JWT.
     * 
     * @param token Le token JWT
     * @return Le rôle de l'utilisateur (ex: "ROLE_ADMIN")
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    /**
     * Extrait la date d'expiration du token.
     * 
     * @param token Le token JWT
     * @return La date d'expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrait une claim spécifique du token en utilisant une fonction de résolution.
     * 
     * @param token Le token JWT
     * @param claimsResolver Fonction pour extraire la claim souhaitée
     * @return La valeur de la claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extrait toutes les claims du token JWT.
     * Vérifie la signature et décode le payload.
     * 
     * @param token Le token JWT
     * @return L'ensemble des claims contenues dans le token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Vérifie si le token a expiré.
     * 
     * @param token Le token JWT
     * @return true si le token est expiré, false sinon
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Valide le token JWT en vérifiant :
     * 1. Que l'email dans le token correspond à celui de l'utilisateur
     * 2. Que le token n'est pas expiré
     * 
     * @param token Le token JWT à valider
     * @param userDetails Les détails de l'utilisateur à comparer
     * @return true si le token est valide, false sinon
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
