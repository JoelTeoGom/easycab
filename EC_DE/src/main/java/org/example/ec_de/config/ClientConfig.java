package org.example.ec_de.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ClientConfig {

    @Value("${taxi.id}")
    private String taxiId;

    @Bean
    public String taxiIdTopic() {
        return "taxi-start-service-" + taxiId;  // Tópico dinámico basado en el ID del cliente
    }
}