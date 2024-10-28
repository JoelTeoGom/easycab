package org.example.ec_customer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for client-specific settings.
 */
@Configuration
@Slf4j
public class ClientConfig {

    @Value("${client.id}")
    private String clientId;

    /**
     * Creates a dynamic Kafka topic name based on the client ID.
     *
     * @return the dynamic Kafka topic name
     */
    @Bean
    public String clientIdTopic() {
        log.info("Client ID: {}", clientId);
        return "taxi-requests-" + clientId;  // Dynamic topic based on the client ID
    }
}