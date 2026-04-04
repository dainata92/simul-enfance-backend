package com.municipal.controller;

import com.municipal.entity.*;
import com.municipal.repository.CalculationRuleRepository;
import com.municipal.repository.CityRepository;
import com.municipal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrôleur pour les endpoints administrateur.
 * 
 * Routes /api/admin/** accessibles uniquement aux utilisateurs avec ROLE_ADMIN.
 * L'accès est contrôlé par SecurityConfig (hasRole("ADMIN")).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AdminController {
    
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CalculationRuleRepository calculationRuleRepository;
    
    /**
     * Liste tous les utilisateurs du système.
     * Réservé aux administrateurs.
     * 
     * GET /api/admin/users
     * Header : Authorization: Bearer <token>
     * 
     * @return La liste de tous les utilisateurs
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    /**
     * Supprime un utilisateur par son ID.
     * Réservé aux administrateurs.
     * 
     * DELETE /api/admin/users/{userId}
     * 
     * @param userId L'ID de l'utilisateur à supprimer
     * @return Un message de confirmation
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userRepository.deleteById(userId);
        return ResponseEntity.ok()
                .body("Utilisateur " + userId + " supprimé avec succès");
    }
    
    /**
     * Endpoint de test pour vérifier l'accès admin.
     * 
     * GET /api/admin/dashboard
     * 
     * @return Un message de bienvenue
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> adminDashboard() {
        return ResponseEntity.ok()
                .body("Bienvenue sur le tableau de bord administrateur");
    }
    
    /**
     * Initialise les données du centre de loisirs du mercredi pour Le Perreux-sur-Marne
     * 
     * POST /api/admin/init-mercredi-perreux
     * 
     * @return Message de confirmation
     */
    @PostMapping("/init-mercredi-perreux")
    @Transactional
    public ResponseEntity<String> initMercrediPerreux() {
        try {
            log.info("Initialisation des données du centre de loisirs du mercredi pour Le Perreux-sur-Marne");
            
            // 1. Récupérer la ville Le Perreux-sur-Marne
            City perreux = cityRepository.findByName("Le Perreux-sur-Marne")
                    .orElseThrow(() -> new RuntimeException("Ville Le Perreux-sur-Marne non trouvée"));
            
            // 2. Créer ou mettre à jour la règle pour MERCREDI
            CalculationRule rule = calculationRuleRepository
                    .findByCityIdAndServiceTypeAndIsActiveTrue(perreux.getId(), ServiceType.MERCREDI)
                    .orElse(new CalculationRule());
            
            rule.setCity(perreux);
            rule.setServiceType(ServiceType.MERCREDI);
            rule.setCalculationType(CalculationType.TRANCHES);
            rule.setUnitType(UnitType.PER_SERVICE);
            rule.setIsActive(true);
            
            // Supprimer les anciennes tranches
            rule.getPriceBrackets().clear();
            
            // 3. Ajouter les tranches tarifaires selon le barème 2025-2026
            addBracket(rule, 942, 999999, 17.84, "A", "Journée complète avec repas", 1);
            addBracket(rule, 823, 941.99, 16.59, "B", "Journée complète avec repas", 2);
            addBracket(rule, 715, 822.99, 14.82, "C", "Journée complète avec repas", 3);
            addBracket(rule, 616, 714.99, 13.08, "D", "Journée complète avec repas", 4);
            addBracket(rule, 565, 615.99, 10.83, "E", "Journée complète avec repas", 5);
            addBracket(rule, 530, 564.99, 8.25, "F", "Journée complète avec repas", 6);
            addBracket(rule, 0, 529.99, 5.04, "G", "Journée complète avec repas", 7);
            
            // Sauvegarder
            calculationRuleRepository.save(rule);
            
            log.info("✅ Données initialisées avec succès : {} tranches tarifaires ajoutées", rule.getPriceBrackets().size());
            
            return ResponseEntity.ok(String.format(
                "✅ Données du centre de loisirs du mercredi initialisées avec succès\n\n" +
                "Ville : Le Perreux-sur-Marne\n" +
                "Service : MERCREDI\n" +
                "Type de calcul : %s\n" +
                "Type d'unité : %s\n" +
                "Nombre de tranches : %d\n\n" +
                "Test : QF 1710 devrait maintenant donner 17.84€ (catégorie A)",
                rule.getCalculationType(),
                rule.getUnitType(),
                rule.getPriceBrackets().size()
            ));
            
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation", e);
            return ResponseEntity.internalServerError()
                    .body("❌ Erreur : " + e.getMessage());
        }
    }
    
    /**
     * Initialise les données de restauration scolaire pour Le Perreux-sur-Marne
     * 
     * @return Message de confirmation
     */
    @PostMapping("/init-restauration-perreux")
    @Transactional
    public ResponseEntity<String> initRestaurationPerreux() {
        try {
            log.info("Initialisation des données de restauration scolaire pour Le Perreux-sur-Marne");
            
            // 1. Récupérer la ville Le Perreux-sur-Marne
            City perreux = cityRepository.findByName("Le Perreux-sur-Marne")
                    .orElseThrow(() -> new RuntimeException("Ville Le Perreux-sur-Marne non trouvée"));
            
            // 2. Service REPAS (Prix du repas)
            CalculationRule ruleRepas = calculationRuleRepository
                    .findByCityIdAndServiceTypeAndIsActiveTrue(perreux.getId(), ServiceType.REPAS)
                    .orElse(new CalculationRule());
            
            ruleRepas.setCity(perreux);
            ruleRepas.setServiceType(ServiceType.REPAS);
            ruleRepas.setCalculationType(CalculationType.TRANCHES);
            ruleRepas.setUnitType(UnitType.PER_SERVICE);
            ruleRepas.setIsActive(true);
            ruleRepas.getPriceBrackets().clear();
            
            // Barème REPAS (Prix du repas)
            addBracket(ruleRepas, 942, 999999, 6.56, "A", "Prix du repas", 1);
            addBracket(ruleRepas, 823, 941.99, 6.02, "B", "Prix du repas", 2);
            addBracket(ruleRepas, 715, 822.99, 5.29, "C", "Prix du repas", 3);
            addBracket(ruleRepas, 616, 714.99, 4.42, "D", "Prix du repas", 4);
            addBracket(ruleRepas, 565, 615.99, 3.80, "E", "Prix du repas", 5);
            addBracket(ruleRepas, 530, 564.99, 2.81, "F", "Prix du repas", 6);
            addBracket(ruleRepas, 0, 529.99, 1.58, "G", "Prix du repas", 7);
            
            calculationRuleRepository.save(ruleRepas);
            
            log.info("✅ Données de restauration initialisées avec succès");
            
            return ResponseEntity.ok(String.format(
                "✅ Données de restauration scolaire initialisées avec succès\n\n" +
                "Ville : Le Perreux-sur-Marne\n" +
                "Service créé :\n" +
                "  REPAS - %d tranches tarifaires\n\n" +
                "Type de calcul : TRANCHES\n" +
                "Type d'unité : PER_SERVICE (prix par repas)\n\n" +
                "Exemples :\n" +
                "  - QF 942 → REPAS: 6,56€ (catégorie A)\n" +
                "  - QF 530 → REPAS: 2,81€ (catégorie F)",
                ruleRepas.getPriceBrackets().size()
            ));
            
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation", e);
            return ResponseEntity.internalServerError()
                    .body("❌ Erreur : " + e.getMessage());
        }
    }
    
    private void addBracket(CalculationRule rule, double qfMin, double qfMax, double price, 
                           String category, String description, int order) {
        PriceBracket bracket = new PriceBracket();
        bracket.setQfMin(BigDecimal.valueOf(qfMin));
        bracket.setQfMax(BigDecimal.valueOf(qfMax));
        bracket.setFixedPrice(BigDecimal.valueOf(price));
        bracket.setBracketCategory(category);
        bracket.setDescription(description);
        bracket.setDisplayOrder(order);
        rule.addPriceBracket(bracket);
    }
}
