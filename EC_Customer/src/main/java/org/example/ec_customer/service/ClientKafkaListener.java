package org.example.ec_customer.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

@Service
@Slf4j
public class ClientKafkaListener {

    public static Semaphore semaphore = new Semaphore(1);

    @KafkaListener(topics = "#{@clientIdTopic}", groupId = "group")
    public void listenToClientResponses(String message) {
        log.info("Received message from server: {}", message);

        processMessage(message);
    }

    private void processMessage(String message) {
        if (message.startsWith("OK")) {
            log.info("Taxi assigned successfully: {}", message);
        } else {
            semaphore.release();
            log.error("Failed to assign taxi: {}", message);
        }
        if (message.equals("END")) {
            semaphore.release();
        }
    }
}