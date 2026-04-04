package com.municipal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "calculation_rules",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_city_service",
            columnNames = {"city_id", "service_type"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false)
    private CalculationType calculationType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type")
    private UnitType unitType = UnitType.HOURLY; // Par défaut : tarif horaire
    
    @Column(name = "price_min", precision = 10, scale = 2)
    private BigDecimal priceMin;
    
    @Column(name = "price_max", precision = 10, scale = 2)
    private BigDecimal priceMax;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Relations vers les configurations spécifiques
    @OneToMany(
        mappedBy = "calculationRule",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<PriceBracket> priceBrackets = new ArrayList<>();

    @OneToMany(
        mappedBy = "calculationRule",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<TauxEffortConfig> tauxEffortConfigs = new ArrayList<>();
    
    // Méthodes utilitaires
    public void addTauxEffortConfig(TauxEffortConfig config) {
        tauxEffortConfigs.add(config);
        config.setCalculationRule(this);
    }
    
    public void addPriceBracket(PriceBracket bracket) {
        priceBrackets.add(bracket);
        bracket.setCalculationRule(this);
    }
}

