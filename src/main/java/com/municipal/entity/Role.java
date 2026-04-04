package com.municipal.entity;

/**
 * Enum représentant les rôles des utilisateurs dans le système.
 * 
 * - ROLE_USER : Rôle standard pour les utilisateurs normaux, 
 *               peut accéder à ses propres données via /api/user/**
 * 
 * - ROLE_ADMIN : Rôle administrateur avec accès complet,
 *                peut accéder à toutes les routes administratives via /api/admin/**
 * 
 * Note : Le préfixe "ROLE_" est requis par Spring Security pour les rôles.
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
