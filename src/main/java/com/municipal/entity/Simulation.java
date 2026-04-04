package com.municipal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant une simulation de tarification sauvegardée.
 */
@Entity
@Table(name = "simulations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Simulation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID de l'utilisateur qui a effectué la simulation.
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * Date et heure de la simulation.
     */
    @Column(nullable = false)
    private LocalDateTime simulationDate;
    
    /**
     * Nom de la ville.
     */
    @Column(nullable = false)
    private String cityName;
    
    /**
     * Type de service (CRECHE, PERISCOLAIRE, etc.).
     */
    @Column(nullable = false)
    private String serviceType;
    
    /**
     * Prix mensuel calculé.
     */
    @Column(nullable = true)
    private Double monthlyPrice;
    
    /**
     * Prix unitaire (horaire ou par repas selon le service).
     */
    @Column(nullable = true)
    private Double hourlyPrice;
    
    /**
     * Quotient familial utilisé pour le calcul.
     */
    @Column(nullable = false)
    private Double quotientFamilial;
    
    /**
     * Nombre d'enfants à charge.
     */
    @Column(nullable = false)
    private Integer childrenCount;
    
    /**
     * Fréquence/forfait pour périscolaire (nombre de goûters/semaine).
     */
    @Column(nullable = true)
    private Integer frequency;
}
