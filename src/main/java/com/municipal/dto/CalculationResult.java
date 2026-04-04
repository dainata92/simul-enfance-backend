package com.municipal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationResult {
    
    private BigDecimal hourlyPrice;
    private BigDecimal monthlyPrice;
    private BigDecimal servicePrice;  // Prix par prestation (journée, matin, soir, etc.)
    private String unitType;          // HOURLY ou PER_SERVICE
    private String calculationType;
    private String cityName;
    private String serviceType;
    private BigDecimal quotientFamilial;
    private Integer childrenCount;
    private Integer frequency;           // Fréquence/forfait (ex: nombre de goûters/semaine)
    private String bracketCategory;  // Catégorie (A, B, C, D, E, F, G)
    private String bracketDescription; // Description de la tranche
    
    public String getFormattedHourlyPrice() {
        return hourlyPrice != null ? String.format("%.2f €", hourlyPrice) : "N/A";
    }
    
    public String getFormattedMonthlyPrice() {
        return monthlyPrice != null ? String.format("%.2f €", monthlyPrice) : "N/A";
    }
    
    public String getFormattedServicePrice() {
        return servicePrice != null ? String.format("%.2f €", servicePrice) : "N/A";
    }
}

