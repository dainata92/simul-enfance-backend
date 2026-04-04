package com.municipal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

// ============================================
// 1. ENTITÉ CITY (Ville)
// ============================================
@Entity
@Table(name = "cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;
    
    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CalculationRule> calculationRules = new ArrayList<>();
    
    // Méthode utilitaire pour ajouter une règle
    public void addCalculationRule(CalculationRule rule) {
        calculationRules.add(rule);
        rule.setCity(this);
    }
}
