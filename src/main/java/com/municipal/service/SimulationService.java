package com.municipal.service;

import com.municipal.dto.SimulationRequest;
import com.municipal.dto.SimulationResponse;
import com.municipal.entity.Simulation;
import com.municipal.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des simulations.
 */
@Service
@RequiredArgsConstructor
public class SimulationService {
    
    private final SimulationRepository simulationRepository;
    
    /**
     * Sauvegarde une nouvelle simulation pour un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur
     * @param request Les données de la simulation
     * @return La simulation sauvegardée
     */
    public SimulationResponse saveSimulation(Long userId, SimulationRequest request) {
        Simulation simulation = new Simulation();
        simulation.setUserId(userId);
        simulation.setSimulationDate(LocalDateTime.now());
        simulation.setCityName(request.getCityName());
        simulation.setServiceType(request.getServiceType());
        simulation.setMonthlyPrice(request.getMonthlyPrice());
        simulation.setHourlyPrice(request.getHourlyPrice());
        simulation.setQuotientFamilial(request.getQuotientFamilial());
        simulation.setChildrenCount(request.getChildrenCount());
        simulation.setFrequency(request.getFrequency());  // Fréquence pour périscolaire
        
        Simulation saved = simulationRepository.save(simulation);
        return mapToResponse(saved);
    }
    
    /**
     * Récupère toutes les simulations d'un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur
     * @return La liste des simulations
     */
    public List<SimulationResponse> getUserSimulations(Long userId) {
        return simulationRepository.findByUserIdOrderBySimulationDateDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Supprime toutes les anciennes simulations sans hourlyPrice.
     */
    public void cleanupOldSimulations() {
        simulationRepository.deleteSimulationsWithoutHourlyPrice();
    }
    
    /**
     * Supprime les simulations en double.
     * On garde la plus récente et on supprime les autres qui ont les mêmes caractéristiques.
     * 
     * @return Le nombre de simulations supprimées
     */
    public int removeDuplicateSimulations() {
        List<Simulation> allSimulations = simulationRepository.findAll();
        int removedCount = 0;
        
        // Grouper par clé unique (userId + cityName + serviceType + date proche)
        for (int i = 0; i < allSimulations.size(); i++) {
            Simulation sim1 = allSimulations.get(i);
            if (sim1.getId() == null) continue; // Déjà supprimée
            
            for (int j = i + 1; j < allSimulations.size(); j++) {
                Simulation sim2 = allSimulations.get(j);
                if (sim2.getId() == null) continue; // Déjà supprimée
                
                // Vérifier si c'est un doublon
                if (sim1.getUserId().equals(sim2.getUserId()) &&
                    sim1.getCityName().equals(sim2.getCityName()) &&
                    sim1.getServiceType().equals(sim2.getServiceType()) &&
                    sim1.getQuotientFamilial().equals(sim2.getQuotientFamilial()) &&
                    Math.abs(sim1.getSimulationDate().toEpochSecond(java.time.ZoneOffset.UTC) - 
                             sim2.getSimulationDate().toEpochSecond(java.time.ZoneOffset.UTC)) < 60) { // Moins d'1 minute d'écart
                    
                    // Supprimer la plus ancienne
                    Simulation toDelete = sim1.getSimulationDate().isBefore(sim2.getSimulationDate()) ? sim1 : sim2;
                    simulationRepository.delete(toDelete);
                    removedCount++;
                    
                    // Marquer comme supprimée
                    if (toDelete == sim1) {
                        sim1.setId(null);
                    } else {
                        sim2.setId(null);
                    }
                }
            }
        }
        
        return removedCount;
    }
    
    /**
     * Supprime une simulation par son ID.
     * Vérifie que la simulation appartient à l'utilisateur avant de la supprimer.
     * 
     * @param simulationId L'ID de la simulation
     * @param userId L'ID de l'utilisateur
     */
    public void deleteSimulation(Long simulationId, Long userId) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée"));
        
        // Vérifier que la simulation appartient à l'utilisateur
        if (!simulation.getUserId().equals(userId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette simulation");
        }
        
        simulationRepository.delete(simulation);
    }
    
    /**
     * Mappe une entité Simulation vers un DTO SimulationResponse.
     */
    private SimulationResponse mapToResponse(Simulation simulation) {
        return new SimulationResponse(
                simulation.getId(),
                simulation.getUserId(),
                simulation.getSimulationDate(),
                simulation.getCityName(),
                simulation.getServiceType(),
                simulation.getMonthlyPrice(),
                simulation.getHourlyPrice(),
                simulation.getQuotientFamilial(),
                simulation.getChildrenCount(),
                simulation.getFrequency()  // Fréquence pour périscolaire
        );
    }
}
