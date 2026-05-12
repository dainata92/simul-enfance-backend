package com.municipal.controller;

import com.municipal.entity.City;
import com.municipal.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST pour gérer les villes.
 * 
 * Endpoints :
 * - GET /cities : Récupère toutes les villes
 * - GET /cities/{id} : Récupère une ville par son ID
 */
@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CityController {

    private final CityService cityService;

    /**
     * Récupère la liste de toutes les villes.
     * 
     * @return Liste des villes avec leurs règles de calcul
     */
    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }
    
    /**
     * Récupère une ville par son ID.
     * 
     * @param id ID de la ville
     * @return La ville correspondante
     */
    @GetMapping("/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getCityById(id));
    }
    
    /**
     * Initialise les villes de base.
     * 
     * @return Message de succès
     */
    @PostMapping("/init")
    public ResponseEntity<String> initCities() {
        cityService.initCities();
        return ResponseEntity.ok("✅ Villes initialisées avec succès");
    }
}
