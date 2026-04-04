package com.municipal.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequest {
    
    @NotNull(message = "L'ID de la ville est requis")
    @Positive(message = "L'ID de la ville doit être positif")
    private Long cityId;
    
    @NotBlank(message = "Le type de service est requis")
    @Pattern(regexp = "CRECHE|PERISCOLAIRE|ACCUEIL_MATIN|ACCUEIL_SOIR|ACCUEIL_SOIR_SPECIFIQUE|REPAS|MERCREDI", 
             message = "Le type de service doit être CRECHE, PERISCOLAIRE, ACCUEIL_MATIN, ACCUEIL_SOIR, ACCUEIL_SOIR_SPECIFIQUE, REPAS ou MERCREDI")
    private String serviceType;
    
    @NotNull(message = "Le Quotient Familial est requis")
    @DecimalMin(value = "0.0", message = "Le Quotient Familial ne peut pas être négatif")
    @DecimalMax(value = "999999.99", message = "Le Quotient Familial est trop élevé")
    private Double quotientFamilial;
    
    // childrenCount optionnel : requis uniquement pour la crèche (taux d'effort CNAF)
    @Min(value = 1, message = "Le nombre d'enfants doit être au minimum 1")
    @Max(value = 10, message = "Le nombre d'enfants ne peut pas dépasser 10")
    private Integer childrenCount;
    
    // Fréquence/forfait : pour les services périscolaires (nombre de jours par semaine ou goûters)
    // Par exemple : 1-4 goûters/semaine pour l'accueil du soir à Paris
    @Min(value = 1, message = "La fréquence doit être au minimum 1")
    @Max(value = 7, message = "La fréquence ne peut pas dépasser 7")
    private Integer frequency;
}
