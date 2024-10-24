package org.example.ec_central.controller;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class TaxiWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public TaxiWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "taxi-coordinates", groupId = "central-group")
    public void broadcastTaxiCoordinates(String message) {
        // Transmitir las coordenadas a los clientes conectados al WebSocket
        messagingTemplate.convertAndSend("/topic/taxi-coordinates", message);
    }
}
