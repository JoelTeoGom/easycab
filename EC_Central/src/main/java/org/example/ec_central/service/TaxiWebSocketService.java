package org.example.ec_central.service;

import lombok.extern.slf4j.Slf4j;
import org.example.ec_central.model.CityMap;
import org.example.ec_central.model.TaxiStatusDto;
import org.example.ec_central.utils.MappingUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service class for handling WebSocket communications related to taxi status updates.
 */
@Service
@Slf4j
public class TaxiWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructs a new TaxiWebSocketService with the specified messaging template.
     *
     * @param messagingTemplate the template for sending messages via WebSocket
     */
    public TaxiWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcasts a taxi status update to all connected clients.
     *
     * @param message the taxi status update message to be broadcasted
     */
    public void broadcastToClients(CityMap message) {


        String jsonMap = MappingUtils.map(message);


        messagingTemplate.convertAndSend("/topic/taxi-coordinates", jsonMap);
        //log.info("Message sent to clients: {}", message);
    }
}