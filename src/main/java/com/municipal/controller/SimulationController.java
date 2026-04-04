package com.municipal.controller;

import com.municipal.dto.SimulationRequest;
import com.municipal.dto.SimulationResponse;
import com.municipal.entity.User;
import com.municipal.repository.UserRepository;
import com.municipal.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des simulations.
 */
@RestController
@RequestMapping("/api/simulations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SimulationController {
    
    private final SimulationService simulationService;
    private final UserRepository userRepository;
    
    /**
     * Sauvegarde une nouvelle simulation pour l'utilisateur connecté.
     * 
     * POST /api/simulations
     * Header : Authorization: Bearer <token>
     * 
     * @param request Les données de la simulation
     * @param userDetails L'utilisateur authentifié
     * @return La simulation sauvegardée
     */
    @PostMapping
    public ResponseEntity<SimulationResponse> saveSimulation(
            @RequestBody SimulationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Récupère l'utilisateur depuis son email
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        SimulationResponse response = simulationService.saveSimulation(user.getId(), request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Récupère toutes les simulations de l'utilisateur connecté.
     * 
     * GET /api/simulations
     * Header : Authorization: Bearer <token>
     * 
     * @param userDetails L'utilisateur authentifié
     * @return La liste des simulations
     */
    @GetMapping
    public ResponseEntity<List<SimulationResponse>> getUserSimulations(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Récupère l'utilisateur depuis son email
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        List<SimulationResponse> simulations = simulationService.getUserSimulations(user.getId());
        return ResponseEntity.ok(simulations);
    }
    
    /**
     * Nettoie les anciennes simulations sans hourlyPrice.
     * 
     * DELETE /api/simulations/cleanup
     * 
     * @return Message de confirmation
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupOldSimulations() {
        simulationService.cleanupOldSimulations();
        return ResponseEntity.ok("Anciennes simulations supprimées avec succès");
    }
    
    /**
     * Supprime les simulations en double.
     * 
     * DELETE /api/simulations/remove-duplicates
     * 
     * @return Message de confirmation
     */
    @DeleteMapping("/remove-duplicates")
    public ResponseEntity<String> removeDuplicates() {
        int removed = simulationService.removeDuplicateSimulations();
        return ResponseEntity.ok(removed + " simulation(s) en double supprimée(s)");
    }
    
    /**
     * Supprime une simulation par son ID.
     * 
     * DELETE /api/simulations/{id}
     * 
     * @param id L'ID de la simulation à supprimer
     * @param userDetails L'utilisateur authentifié
     * @return Message de confirmation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSimulation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        simulationService.deleteSimulation(id, user.getId());
        return ResponseEntity.ok("Simulation supprimée avec succès");
    }
}
