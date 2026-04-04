package com.municipal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la sauvegarde d'une simulation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {
    
    private String cityName;
    private String serviceType;
    private Double monthlyPrice;
    private Double hourlyPrice;
    private Double quotientFamilial;
    private Integer childrenCount;
    private Integer frequency;  // Fréquence pour périscolaire (nombre de goûters/semaine)
}
