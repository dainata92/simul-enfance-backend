package com.municipal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "taux_effort_configs", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"calculation_rule_id", "number_of_children"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TauxEffortConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculation_rule_id", nullable = false)
    private CalculationRule calculationRule;
    
    @Column(name = "number_of_children", nullable = false)
    private Integer numberOfChildren;
    
    @Column(name = "effort_rate", nullable = false, precision = 6, scale = 6)
    private BigDecimal effortRate;
    
    // Plancher et plafond spécifiques par nombre d'enfants (optionnel)
    // Si null, utilise les valeurs de CalculationRule
    @Column(name = "price_min", precision = 10, scale = 2)
    private BigDecimal priceMin;
    
    @Column(name = "price_max", precision = 10, scale = 2)
    private BigDecimal priceMax;
    
    @Column(length = 500)
    private String description;
}

