package org.example.ec_central.controller;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller for handling WebSocket communications related to taxi coordinates.
 */
@Controller
public class TaxiWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructor for TaxiWebSocketController.
     *
     * @param messagingTemplate the SimpMessagingTemplate used to send messages to WebSocket clients
     */
    public TaxiWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Listens to the Kafka topic "taxi-coordinates" and broadcasts the received messages to WebSocket clients.
     *
     * @param message the message received from the Kafka topic
     */
    @KafkaListener(topics = "taxi-coordinates", groupId = "group")
    public void broadcastTaxiCoordinates(String message) {
        // Transmit the coordinates to clients connected to the WebSocket
        messagingTemplate.convertAndSend("/topic/taxi-coordinates", message);
    }
}