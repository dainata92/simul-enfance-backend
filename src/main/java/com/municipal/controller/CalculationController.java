package com.municipal.controller;

import com.municipal.dto.CalculationRequest;
import com.municipal.dto.CalculationResult;
import com.municipal.service.PricingService;
import com.municipal.exception.CalculationException;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculate")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class CalculationController {

    private final PricingService pricingService;

    /**
     * Endpoint de calcul du tarif municipal
     * 
     * @param request Requête contenant les paramètres de calcul
     * @return Résultat du calcul avec prix horaire et mensuel
     */
    @PostMapping
    public ResponseEntity<CalculationResult> calculatePrice(@Valid @RequestBody CalculationRequest request) {
        
        log.info("Requête de calcul reçue : {}", request);
        
        try {
            // Pour les services autres que CRECHE, childrenCount peut être null (valeur par défaut : 1)
            Integer childrenCount = request.getChildrenCount() != null ? request.getChildrenCount() : 1;
            
            // Fréquence/forfait (ex: nombre de goûters pour périscolaire)
            Integer frequency = request.getFrequency();
            
            CalculationResult result = pricingService.calculatePrice(
                request.getCityId(),
                request.getServiceType(),
                request.getQuotientFamilial(),
                childrenCount,
                frequency
            );
            
            return ResponseEntity.ok(result);
            
        } catch (CalculationException e) {
            log.error("Erreur lors du calcul : {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Gestion globale des exceptions
     */
    @ExceptionHandler(CalculationException.class)
    public ResponseEntity<ErrorResponse> handleCalculationException(CalculationException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
        
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erreur inattendue", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Une erreur interne est survenue")
                .timestamp(System.currentTimeMillis())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @Data
    @Builder
    static class ErrorResponse {
        private int status;
        private String message;
        private long timestamp;
    }
}