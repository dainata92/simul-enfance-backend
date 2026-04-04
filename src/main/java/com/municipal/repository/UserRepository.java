package com.municipal.repository;

import com.municipal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des utilisateurs en base de données.
 * 
 * Fournit les opérations CRUD standard héritées de JpaRepository
 * plus une méthode de recherche personnalisée par email.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Recherche un utilisateur par son adresse email.
     * 
     * @param email L'adresse email de l'utilisateur
     * @return Un Optional contenant l'utilisateur s'il existe, Optional.empty() sinon
     * 
     * Utilisé principalement pour :
     * - L'authentification lors du login
     * - La vérification d'unicité de l'email lors de l'inscription
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Vérifie si un utilisateur avec cet email existe déjà.
     * 
     * @param email L'adresse email à vérifier
     * @return true si l'email existe déjà, false sinon
     */
    boolean existsByEmail(String email);
}
