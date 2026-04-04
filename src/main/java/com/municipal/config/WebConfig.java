package com.municipal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // Configuration Web MVC supplémentaire si nécessaire
    // Pour l'instant, le CorsConfig suffit
    
    // Vous pouvez ajouter d'autres configurations ici :
    // - Intercepteurs
    // - Formatters personnalisés
    // - Content negotiation
    // - etc.
}