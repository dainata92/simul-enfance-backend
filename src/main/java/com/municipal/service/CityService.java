package com.municipal.service;

import com.municipal.entity.City;
import com.municipal.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityService {
    
    private final CityRepository cityRepository;
    
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
    
    public City getCityById(Long id) {
        return cityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ville non trouvée"));
    }
    
    @Transactional
    public City saveCity(City city) {
        return cityRepository.save(city);
    }
    
    @Transactional
    public void deleteCity(Long id) {
        cityRepository.deleteById(id);
    }
    
    @Transactional
    public void initCities() {
        // Supprimer toutes les villes existantes
        cityRepository.deleteAll();
        
        // Créer les 3 villes
        City paris = new City();
        paris.setName("Paris");
        paris.setPostalCode("75000");
        cityRepository.save(paris);
        
        City villeB = new City();
        villeB.setName("Ville B - Trancheville");
        villeB.setPostalCode("69001");
        cityRepository.save(villeB);
        
        City perreux = new City();
        perreux.setName("Le Perreux-sur-Marne");
        perreux.setPostalCode("94170");
        cityRepository.save(perreux);
    }
}
