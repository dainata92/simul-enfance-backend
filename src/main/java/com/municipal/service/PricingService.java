package com.municipal.service;

import com.municipal.entity.*;
import com.municipal.repository.CalculationRuleRepository;
import com.municipal.dto.CalculationResult;
import com.municipal.exception.CalculationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.municipal.entity.UnitType;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PricingService {

    private final CalculationRuleRepository calculationRuleRepository;
    
    private static final BigDecimal MONTHLY_HOURS = new BigDecimal("160");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Calcule le prix horaire et mensuel pour un utilisateur
     * 
     * @param cityId ID de la ville
     * @param serviceType Type de service (CRECHE ou PERISCOLAIRE)
     * @param userQF Quotient Familial de l'utilisateur
     * @param childrenCount Nombre d'enfants à charge
     * @param frequency Fréquence/forfait (ex: nombre de goûters/semaine pour périscolaire)
     * @return Résultat du calcul avec prix horaire et mensuel
     * @throws CalculationException si aucune règle n'est trouvée ou si les données sont invalides
     */
    public CalculationResult calculatePrice(Long cityId, String serviceType, double userQF, 
                                           Integer childrenCount, Integer frequency) {
        
        log.info("Calcul du tarif pour ville={}, service={}, QF={}, enfants={}, fréquence={}", 
                 cityId, serviceType, userQF, childrenCount, frequency);
        
        // Validation des paramètres
        validateInputs(cityId, serviceType, userQF, childrenCount);
        
        // Conversion des paramètres
        ServiceType service = ServiceType.valueOf(serviceType.toUpperCase());
        BigDecimal quotientFamilial = BigDecimal.valueOf(userQF);
        
        // Par défaut frequency = 1 si non spécifié
        int effectiveFrequency = (frequency != null && frequency > 0) ? frequency : 1;
        
        // Récupération de la règle active
        CalculationRule rule = calculationRuleRepository
                .findByCityIdAndServiceTypeAndIsActiveTrue(cityId, service)
                .orElseThrow(() -> new CalculationException(
                    String.format("Aucune règle active trouvée pour la ville %d et le service %s", 
                                  cityId, serviceType)));
        
        // Calcul selon le type de règle
        CalculationResult result = switch (rule.getCalculationType()) {
            case TAUX_EFFORT -> {
                BigDecimal hourlyPrice = calculateWithEffortRate(rule, quotientFamilial, childrenCount);
                BigDecimal monthlyPrice = hourlyPrice.multiply(MONTHLY_HOURS).setScale(SCALE, ROUNDING);
                
                yield CalculationResult.builder()
                        .hourlyPrice(hourlyPrice)
                        .monthlyPrice(monthlyPrice)
                        .calculationType(rule.getCalculationType().name())
                        .cityName(rule.getCity().getName())
                        .serviceType(service.name())
                        .quotientFamilial(quotientFamilial)
                        .childrenCount(childrenCount)
                        .frequency(effectiveFrequency)
                        .build();
            }
            case TRANCHES -> calculateWithBrackets(rule, quotientFamilial, childrenCount, effectiveFrequency);
        };
        
        log.info("Tarif calculé : {}€/h ({}€/mois) - Catégorie: {}", 
                 result.getHourlyPrice(), result.getMonthlyPrice(), result.getBracketCategory());
        
        return result;
    }

    /**
     * Calcul selon le mode TAUX_EFFORT (PSU)
     * Formule : Prix = QF × Taux d'effort
     * Avec application du plancher (min) et plafond (max)
     */
    private BigDecimal calculateWithEffortRate(CalculationRule rule, BigDecimal qf, int childrenCount) {
        
        // Recherche du taux d'effort correspondant au nombre d'enfants
        TauxEffortConfig config = rule.getTauxEffortConfigs().stream()
                .filter(c -> c.getNumberOfChildren().equals(childrenCount))
                .findFirst()
                .orElseThrow(() -> new CalculationException(
                    String.format("Aucun taux d'effort configuré pour %d enfant(s)", childrenCount)));
        
        // Calcul brut : QF × Taux
        BigDecimal rawPrice = qf.multiply(config.getEffortRate())
                                .setScale(SCALE, ROUNDING);
        
        log.debug("Calcul PSU : {} × {} = {}", qf, config.getEffortRate(), rawPrice);
        
        // Détermination des planchers/plafonds à utiliser
        // Priorité : config spécifique > rule globale
        BigDecimal minPrice = config.getPriceMin() != null ? config.getPriceMin() : rule.getPriceMin();
        BigDecimal maxPrice = config.getPriceMax() != null ? config.getPriceMax() : rule.getPriceMax();
        
        // Application du plancher (minimum)
        BigDecimal finalPrice = rawPrice;
        if (minPrice != null && rawPrice.compareTo(minPrice) < 0) {
            finalPrice = minPrice;
            log.debug("Application du tarif plancher : {} (pour {} enfant(s))", finalPrice, childrenCount);
        }
        
        // Application du plafond (maximum)
        if (maxPrice != null && finalPrice.compareTo(maxPrice) > 0) {
            finalPrice = maxPrice;
            log.debug("Application du tarif plafond : {} (pour {} enfant(s))", finalPrice, childrenCount);
        }
        
        return finalPrice.setScale(SCALE, ROUNDING);
    }

    /**
     * Calcul selon le mode TRANCHES
     * Retourne le prix fixe de la tranche correspondant au QF
     * Multiplie par la fréquence si > 1 (ex: forfaits périscolaires)
     */
    private CalculationResult calculateWithBrackets(CalculationRule rule, BigDecimal qf, 
                                                   Integer childrenCount, int frequency) {
        
        // Recherche de la tranche contenant le QF
        PriceBracket bracket = rule.getPriceBrackets().stream()
                .filter(b -> qf.compareTo(b.getQfMin()) >= 0 && qf.compareTo(b.getQfMax()) <= 0)
                .findFirst()
                .orElseThrow(() -> new CalculationException(
                    String.format("Aucune tranche tarifaire trouvée pour QF = %.2f", qf)));
        
        log.debug("Tranche trouvée : [{} - {}] → {}€ × {} - Catégorie: {}", 
                  bracket.getQfMin(), bracket.getQfMax(), bracket.getFixedPrice(), 
                  frequency, bracket.getBracketCategory());
        
        // Prix de base (pour fréquence = 1)
        BigDecimal basePrice = bracket.getFixedPrice();
        
        // Application de la fréquence (multiplication)
        BigDecimal finalPrice = basePrice.multiply(BigDecimal.valueOf(frequency))
                                         .setScale(SCALE, ROUNDING);
        
        // Déterminer si c'est un tarif horaire ou par prestation
        boolean isHourly = rule.getUnitType() == null || rule.getUnitType() == UnitType.HOURLY;
        
        BigDecimal hourlyPrice = isHourly ? finalPrice : null;
        BigDecimal monthlyPrice = isHourly ? finalPrice.multiply(MONTHLY_HOURS).setScale(SCALE, ROUNDING) : null;
        BigDecimal servicePrice = isHourly ? null : finalPrice;
        
        return CalculationResult.builder()
                .hourlyPrice(hourlyPrice)
                .monthlyPrice(monthlyPrice)
                .servicePrice(servicePrice)
                .unitType(rule.getUnitType() != null ? rule.getUnitType().name() : "HOURLY")
                .calculationType(rule.getCalculationType().name())
                .cityName(rule.getCity().getName())
                .serviceType(rule.getServiceType().name())
                .quotientFamilial(qf)
                .childrenCount(childrenCount)
                .frequency(frequency)
                .bracketCategory(bracket.getBracketCategory())
                .bracketDescription(bracket.getDescription())
                .build();
    }

    /**
     * Validation des paramètres d'entrée
     */
    private void validateInputs(Long cityId, String serviceType, double userQF, int childrenCount) {
        
        if (cityId == null || cityId <= 0) {
            throw new CalculationException("L'ID de la ville est invalide");
        }
        
        if (serviceType == null || serviceType.isBlank()) {
            throw new CalculationException("Le type de service est requis");
        }
        
        try {
            ServiceType.valueOf(serviceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CalculationException(
                "Type de service invalide. Valeurs acceptées : CRECHE, PERISCOLAIRE");
        }
        
        if (userQF < 0) {
            throw new CalculationException("Le Quotient Familial ne peut pas être négatif");
        }
        
        if (childrenCount <= 0) {
            throw new CalculationException("Le nombre d'enfants doit être supérieur à 0");
        }
    }
}
