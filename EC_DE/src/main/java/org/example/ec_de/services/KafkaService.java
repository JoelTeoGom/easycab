package org.example.ec_de.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.ec_central.utils.MappingUtils;
import org.example.ec_de.model.CustomerStatusDto;
import org.example.ec_de.model.ShortestPathFinder;
import org.example.ec_de.model.TaxiState;
import org.example.ec_de.model.TaxiStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service class for handling Kafka operations related to taxi directions and status updates.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
public class KafkaService {
    /**
     * KafkaTemplate for sending messages to Kafka topics.
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * ShortestPathFinder instance for calculating the taxi's path.
     */
    private final ShortestPathFinder shortestPathFinder = new ShortestPathFinder();

    /**
     * The unique identifier of the taxi, injected from application properties.
     */
    @Value("${taxi.id}")
    private String taxiId;

    /**
     * Publishes the direction of the taxi to the Kafka topic "taxi-directions".
     *
     * @param direction the TaxiStatusDto containing the direction information
     */
    public void publishDirection(TaxiStatusDto direction) {
        // Formato: taxiId,x,y,status
        String message = MappingUtils.map(direction);
        log.info("Published Kafka event to taxi-directions: {}", message);
        kafkaTemplate.send("taxi-directions", message);
    }

    /**
     * Listens for taxi status updates from the Kafka topic "taxi-status".
     *
     * @param message the message received from the Kafka topic
     */
    @KafkaListener(topics = "taxi-status", groupId = "group")
    public void listenTaxiStatusUpdates(String message) {
        log.info("Received Taxi Status Update: " + message);
    }

    /**
     * Listens for client responses from a dynamically resolved Kafka topic.
     *
     * @param customerStatus the message received from the Kafka topic
     */
    @KafkaListener(topics = "#{@taxiIdTopic}", groupId = "group")
    public void listenToClientResponses(String customerStatus) {
        log.debug("Recibido mensaje: {}", customerStatus);

        try {
            CustomerStatusDto customerStatusDto = MappingUtils.mapFromString(customerStatus, CustomerStatusDto.class);
            shortestPathFinder.setCurrentX(customerStatusDto.getX());
            shortestPathFinder.setCurrentY(customerStatusDto.getY());

            // If taxi has to return to base, use this logic
            if (customerStatusDto.getStatus() == TaxiState.RETURNING_TO_BASE) {
                shortestPathFinder.setTaxiState(TaxiState.RETURNING_TO_BASE);
                while (shortestPathFinder.getTaxiState() == TaxiState.RETURNING_TO_BASE || shortestPathFinder.getTaxiState() == TaxiState.STOPPED) {
                    int[] xy = shortestPathFinder.getNextPosition(1, 1);

                    if (shortestPathFinder.isStop()) {
                        shortestPathFinder.setStop(false);
                        break;
                    }

                    TaxiStatusDto taxiStatusDto;
                    if (shortestPathFinder.getTaxiState() == TaxiState.RETURNING_TO_BASE) {
                        taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.RETURNING_TO_BASE);
                    } else {
                        taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.STOPPED);
                    }
                    log.info("Enviando posición: " + xy[0] + "," + xy[1]);
                    publishDirection(taxiStatusDto);
                    Thread.sleep(1000);
                }

                TaxiStatusDto taxiStatusDto = new TaxiStatusDto(taxiId, shortestPathFinder.getCurrentX(), shortestPathFinder.getCurrentY(), TaxiState.RETURNING_TO_BASE);
                log.info("Enviando posición: " + shortestPathFinder.getCurrentX() + "," + shortestPathFinder.getCurrentY());
                publishDirection(taxiStatusDto);
                return;
            }

            shortestPathFinder.setTaxiState(TaxiState.EN_ROUTE_TO_PICKUP);

            while (shortestPathFinder.getTaxiState() == TaxiState.EN_ROUTE_TO_PICKUP || shortestPathFinder.getTaxiState() == TaxiState.STOPPED) {
                int[] xy = shortestPathFinder.getNextPosition(customerStatusDto.getCustomerX(), customerStatusDto.getCustomerY());

                if (shortestPathFinder.isStop()) {
                    shortestPathFinder.setStop(false);
                    break;
                }

                TaxiStatusDto taxiStatusDto;
                if (shortestPathFinder.getTaxiState() == TaxiState.EN_ROUTE_TO_PICKUP) {
                    taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.EN_ROUTE_TO_PICKUP);
                } else {
                    taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.STOPPED);
                }
                log.info("Enviando posición: " + xy[0] + "," + xy[1]);
                publishDirection(taxiStatusDto);

                log.info("Enviando posición: " + xy[0] + "," + xy[1]);
                Thread.sleep(1000);
            }

            {
                TaxiStatusDto taxiStatusDto = new TaxiStatusDto(taxiId,
                        shortestPathFinder.getCurrentX(),
                        shortestPathFinder.getCurrentY(),
                        TaxiState.PICKUP);
                log.info("Enviando posición: " + shortestPathFinder.getCurrentX() + "," + shortestPathFinder.getCurrentY());
                publishDirection(taxiStatusDto);
                shortestPathFinder.setTaxiState(TaxiState.EN_ROUTE_TO_DESTINATION);
            }

            while (shortestPathFinder.getTaxiState() == TaxiState.EN_ROUTE_TO_DESTINATION || shortestPathFinder.getTaxiState() == TaxiState.STOPPED) {
                int[] xy = shortestPathFinder.getNextPosition(customerStatusDto.getDestX(), customerStatusDto.getDestY());

                TaxiStatusDto taxiStatusDto;
                if (shortestPathFinder.getTaxiState() == TaxiState.EN_ROUTE_TO_DESTINATION) {
                    taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.EN_ROUTE_TO_DESTINATION);
                } else {
                    taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.STOPPED);
                }

                publishDirection(taxiStatusDto);

                if (shortestPathFinder.isStop()) {
                    shortestPathFinder.setStop(false);
                    break;
                }

                log.info("Enviando posición: " + xy[0] + "," + xy[1]);
                Thread.sleep(1000);
            }

            shortestPathFinder.setTaxiState(TaxiState.DESTINATION_REACHED);

            TaxiStatusDto taxiStatusDto = new TaxiStatusDto(taxiId, shortestPathFinder.getCurrentX(), shortestPathFinder.getCurrentY(), TaxiState.DESTINATION_REACHED);
            publishDirection(taxiStatusDto);

        } catch (NumberFormatException e) {
            log.error("Error al parsear las coordenadas: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Formato de mensaje incorrecto: {}", e.getMessage());
        } catch (InterruptedException e) {
            log.error("Error al dormir el hilo: {}", e.getMessage());
            Thread.currentThread().interrupt();  // Restaurar el estado interrumpido
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
        }
    }
}