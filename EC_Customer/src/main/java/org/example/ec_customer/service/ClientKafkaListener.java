package org.example.ec_customer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClientKafkaListener {

    // Escuchar mensajes en el tópico específico del cliente
    @KafkaListener(topics = "#{@clientIdTopic}", groupId = "client-group")
    public void listenToClientResponses(String message) {
        log.info("Received message from server: {}", message);

        // Procesar el mensaje recibido
        processMessage(message);
    }

    private void processMessage(String message) {
        if (message.startsWith("OK")) {
            log.info("Taxi assigned successfully: {}", message);
        } else {
            log.error("Failed to assign taxi: {}", message);
        }
    }
}
