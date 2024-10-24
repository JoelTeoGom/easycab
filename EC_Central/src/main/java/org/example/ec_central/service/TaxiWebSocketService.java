package org.example.ec_central.service;
import lombok.extern.slf4j.Slf4j;
import org.example.ec_central.model.TaxiStatusDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaxiWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public TaxiWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastToClients(TaxiStatusDto message) {
        log.info("Sending message to clients: {}", message);
        messagingTemplate.convertAndSend("/topic/taxi-coordinates", message);
        log.info("Message sent to clients: {}", message);
    }
}