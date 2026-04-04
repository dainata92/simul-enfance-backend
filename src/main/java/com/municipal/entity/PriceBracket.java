package com.municipal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "price_brackets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceBracket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculation_rule_id", nullable = false)
    private CalculationRule calculationRule;
    
    @Column(name = "qf_min", nullable = false, precision = 10, scale = 2)
    private BigDecimal qfMin;
    
    @Column(name = "qf_max", nullable = false, precision = 10, scale = 2)
    private BigDecimal qfMax;
    
    @Column(name = "fixed_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal fixedPrice;
    
    @Column(length = 500)
    private String description;
    
    // Ordre d'affichage (optionnel)
    @Column(name = "display_order")
    private Integer displayOrder;
    
    // Catégorie tarifaire (A, B, C, D, E, F, G pour Le Perreux)
    @Column(name = "bracket_category", length = 10)
    private String bracketCategory;
}
