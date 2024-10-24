package org.example.ec_central.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.example.ec_central.model.Customer;
import org.example.ec_central.model.Location;
import org.example.ec_central.model.Taxi;
import org.example.ec_central.model.TaxiStatusDto;
import org.example.ec_central.repository.CustomerRepository;
import org.example.ec_central.repository.LocationRepository;
import org.example.ec_central.repository.TaxiRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {
    private final KafkaAdmin kafkaAdmin;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Map<String, String> clientTopics = new ConcurrentHashMap<>();
    private final TaxiService taxiService;
    private final TaxiWebSocketService taxiWebSocketService;
    private final ClientHandler clientHandler;


    private final TaxiRepository taxiRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;


    public void publishTaxiAssignedEvent(Taxi taxi) {
        String message = "Taxi " + taxi.getIdentifier() + " assigned to position " + taxi.getPosition();
        kafkaTemplate.send("taxi-status", message);
        this.logKafkaSend("Published Kafka event: " + message, "taxi-status");
    }

    @KafkaListener(topics = "taxi-status", groupId = "central-group")
    public void listenTaxiStatusUpdates(String message) {
        log.info("Received Kafka message: {}", message);

        if (message.contains("has arrived at destination")) {
            String taxiIdentifier = extractTaxiIdentifier(message);
            taxiService.findTaxiByIdentifier(taxiIdentifier)
                    .ifPresent(taxiService::releaseTaxi);
            log.info("Taxi {} has been released.", taxiIdentifier);
        }
    }

    private String extractTaxiIdentifier(String message) {
        return message.split(" ")[1];
    }


    @KafkaListener(topics = "service_requests", groupId = "central-group",  containerFactory = "stringKafkaListenerContainerFactory")
    public void listenServiceRequest(String message) {
        String[] parts = message.split("#");
        String clientId = parts[0];
        String destination = parts[1];
        Customer customer = customerService.getCustomerByIdentifier(clientId);

        customer.setDestIdentifier(destination);
        customerRepository.save(customer);
        createClientTopic(clientId);


        boolean taxiAssigned = assignTaxiToClient(customer, destination);

        String response;

        if (taxiAssigned) {
            response = "OK: Taxi asignado";
            log.info("OK: Taxi assigned: {}", message);

        } else {
            response = "KO: Taxis no disponible";
            log.error("KO: Taxi not available: {}", message);
        }
        publishToClient(customer, response);
    }

    public void createClientTopic(String clientId) {
        String topicName = "taxi-requests-" + clientId;
        if (!clientTopics.containsKey(clientId)) {
            NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
            kafkaAdmin.createOrModifyTopics(newTopic);
            clientTopics.put(clientId, topicName);
            log.info("Tópico creado para el cliente: {}", topicName);
        }
    }

    public void publishToClient(Customer customer , String message) {
        String topicName = "taxi-requests-"+ customer.getIdentifier();
        kafkaTemplate.send(topicName, message);
    }

    public void publishToTaxi(Taxi taxi, String message) {
        String topicName = "taxi-start-service-" + taxi.getIdentifier();
        kafkaTemplate.send(topicName, message);
    }

    private boolean assignTaxiToClient(Customer customer, String destination) {
        log.info("Asignando taxi para el cliente {} hacia {}", customer.getIdentifier(), destination);

        List<Taxi> availableTaxis = taxiRepository.findAllByAvailable(true);

        for (Taxi taxi : availableTaxis) {
            if (clientHandler.getConnectedTaxis().containsKey(taxi.getIdentifier())) {
                if (clientHandler.getConnectedTaxis().get(taxi.getIdentifier()).isConnected()) {
                    Optional<Location> location = locationRepository.findByIdentifier(destination);
                    if (location.isEmpty()) {
                        continue;
                    }
                    String loc = customer.getX() + "," + customer.getY();
                    taxi.setAvailable(false);
                    taxi.setDestIdentifier(customer.getIdentifier());
                    taxiRepository.save(taxi);

                    publishToTaxi(taxi, loc);
                    return true;
                }
            }
        }
        log.error("NOT CONTAINS");
        return false;
    }

    @KafkaListener(topics = "taxi-directions", groupId = "central-group", containerFactory = "kafkaListenerContainerFactory")
    public void listenTaxiDirections(String taxiStatusMessage) {
        log.info("Received Taxi Status Update: {}", taxiStatusMessage);

        // Dividir el mensaje por las comas
        String[] fields = taxiStatusMessage.split(",");

        // Validar que el mensaje tenga el formato correcto
        if (fields.length != 4) {
            log.error("Invalid message format: {}", taxiStatusMessage);
            return;
        }

        // Crear un nuevo objeto TaxiStatusDto con los valores recibidos
        // Crear un nuevo objeto TaxiStatusDto usando el constructor
        TaxiStatusDto taxiStatusDto = new TaxiStatusDto(
                fields[0],                            // taxiId
                Integer.parseInt(fields[1]),           // x
                Integer.parseInt(fields[2]),           // y
                fields[3]                              // status
        );

        // Actualizar la localización del taxi en tu sistema
        taxiService.updateTaxiLocationByIdentifier(taxiStatusDto);

        log.info("Taxi {} location updated to [{}, {}]",
                taxiStatusDto.getTaxiId(), taxiStatusDto.getX(), taxiStatusDto.getY());

        // Notificar a los clientes conectados mediante WebSocket
        taxiWebSocketService.broadcastToClients(taxiStatusDto);
    }
    private void logKafkaSend(String message, String topic) {
        log.info("[Topic: {}] {}", topic, message);
    }
}





