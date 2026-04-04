package com.municipal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse d'une simulation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponse {
    
    private Long id;
    private Long userId;
    private LocalDateTime simulationDate;
    private String cityName;
    private String serviceType;
    private Double monthlyPrice;
    private Double hourlyPrice;
    private Double quotientFamilial;
    private Integer childrenCount;
    private Integer frequency;  // Fréquence pour périscolaire (nombre de goûters/semaine)
}
