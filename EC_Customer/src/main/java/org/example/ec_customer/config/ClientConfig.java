package org.example.ec_customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ClientConfig {

    @Value("${client.id}")
    private String clientId;

    @Bean
    public String clientIdTopic() {
        return "taxi-requests-" + clientId;  // Tópico dinámico basado en el ID del cliente
    }
}