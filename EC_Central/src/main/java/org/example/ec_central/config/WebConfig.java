package org.example.ec_central.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Afecta a todas las rutas
                .allowedOriginPatterns("http://*", "https://*") // Permite todos los orígenes con HTTP y HTTPS
                .allowCredentials(true) // Permite credenciales
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                .allowedHeaders("*") // Permite todas las cabeceras
                .maxAge(3600); // Duración máxima de la caché de pre-flight
    }
}
