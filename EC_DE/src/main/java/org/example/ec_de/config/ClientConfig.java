package org.example.ec_de.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for taxi-specific settings.
 */
@Configuration
public class ClientConfig {

    @Value("${taxi.id}")
    private String taxiId;

    /**
     * Creates a dynamic Kafka topic name based on the taxi ID.
     *
     * @return the dynamic Kafka topic name
     */
    @Bean
    public String taxiIdTopic() {
        return "taxi-start-service-" + taxiId;  // Dynamic topic based on the taxi ID
    }
}