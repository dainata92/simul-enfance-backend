package com.municipal.repository;

import com.municipal.entity.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository pour la gestion des simulations en base de données.
 */
@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {
    
    /**
     * Récupère toutes les simulations d'un utilisateur, triées par date décroissante.
     * 
     * @param userId L'ID de l'utilisateur
     * @return La liste des simulations de l'utilisateur
     */
    List<Simulation> findByUserIdOrderBySimulationDateDesc(Long userId);
    
    /**
     * Supprime toutes les simulations qui n'ont pas de hourlyPrice (anciennes simulations).
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Simulation s WHERE s.hourlyPrice IS NULL")
    void deleteSimulationsWithoutHourlyPrice();
    
    /**
     * Trouve les simulations en double pour un utilisateur donné.
     */
    @Query("SELECT s FROM Simulation s WHERE s.userId = :userId AND s.hourlyPrice IS NOT NULL ORDER BY s.simulationDate DESC")
    List<Simulation> findAllByUserId(Long userId);
}
