package com.municipal.repository;

import com.municipal.entity.CalculationRule;
import com.municipal.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalculationRuleRepository extends JpaRepository<CalculationRule, Long> {
    
    // Pour récupérer le type de calcul seulement
    @Query("SELECT cr FROM CalculationRule cr " +
           "JOIN FETCH cr.city " +
           "WHERE cr.city.id = :cityId " +
           "AND cr.serviceType = :serviceType " +
           "AND cr.isActive = true")
    Optional<CalculationRule> findByCityIdAndServiceTypeAndIsActiveTrue(
            @Param("cityId") Long cityId,
            @Param("serviceType") ServiceType serviceType
    );
    
    // Pour le calcul par TAUX_EFFORT
    @Query("SELECT cr FROM CalculationRule cr " +
           "JOIN FETCH cr.city " +
           "LEFT JOIN FETCH cr.tauxEffortConfigs " +
           "WHERE cr.city.id = :cityId " +
           "AND cr.serviceType = :serviceType " +
           "AND cr.isActive = true")
    Optional<CalculationRule> findWithTauxEffort(
            @Param("cityId") Long cityId,
            @Param("serviceType") ServiceType serviceType
    );
    
    // Pour le calcul par TRANCHES
    @Query("SELECT cr FROM CalculationRule cr " +
           "JOIN FETCH cr.city " +
           "LEFT JOIN FETCH cr.priceBrackets " +
           "WHERE cr.city.id = :cityId " +
           "AND cr.serviceType = :serviceType " +
           "AND cr.isActive = true")
    Optional<CalculationRule> findWithPriceBrackets(
            @Param("cityId") Long cityId,
            @Param("serviceType") ServiceType serviceType
    );
}