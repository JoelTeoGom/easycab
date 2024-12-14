package org.example.ec_de.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.ec_de.utils.MappingUtils;
import org.example.ec_de.model.CustomerStatusDto;
import org.example.ec_de.model.ShortestPathFinder;
import org.example.ec_de.model.TaxiState;
import org.example.ec_de.model.TaxiStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.PublicKey;

/**
 * Service class for handling Kafka operations related to taxi directions and
 * status updates.
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
    private final SocketService socketService;
    private final EncryptionService encryptionService;

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
        try {
            // Obtener la clave pública de Central
            PublicKey centralPublicKey = encryptionService.getCentralPublicKey();
            if (centralPublicKey == null) {
                throw new IllegalArgumentException("Clave pública de Central no encontrada.");
            }

            // Convertir el DTO a String
            direction.setToken(socketService.getAuthToken());
            log.info("TOKEN BEFORE SENDING : {}", socketService.getAuthToken());

            String message = MappingUtils.map(direction);

            // Generar clave AES
            SecretKey aesKey = encryptionService.generateAESKey();

            // Cifrar el mensaje con AES
            String encryptedMessage = encryptionService.encryptWithAES(message, aesKey);

            // Cifrar la clave AES con RSA
            String encryptedAESKey = encryptionService.encryptWithRSA(encryptionService.encodeKey(aesKey),
                    centralPublicKey);

            // Formar el payload: clave AES cifrada + mensaje cifrado
            String payload = encryptedAESKey + "#" + encryptedMessage;

            // Publicar el payload cifrado en Kafka
            kafkaTemplate.send("taxi-directions", payload);
            log.info("Published encrypted Kafka event to taxi-directions: {}", payload);

        } catch (Exception e) {
            log.error("Error encrypting or publishing message to taxi-directions: {}", e.getMessage());
        }
    }

    /**
     * Listens for client responses from a dynamically resolved Kafka topic.
     *
     * @param encryptedPayload the message received from the Kafka topic
     */
    @KafkaListener(topics = "#{@taxiIdTopic}", groupId = "group")
    public void listenToClientResponses(String encryptedPayload) {
        log.debug("Recibido mensaje cifrado: {}", encryptedPayload);

        try {

            // Dividir el payload en clave AES cifrada y mensaje cifrado
            String[] parts = encryptedPayload.split("#", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Payload inválido. Se esperaba clave AES y mensaje cifrados separados por '#'.");
            }
            String encryptedAESKey = parts[0];
            String encryptedMessage = parts[1];

            // Descifrar la clave AES con la clave privada del taxi
            String aesKeyEncoded = encryptionService.decryptWithRSA(encryptedAESKey);
            SecretKey aesKey = encryptionService.decodeKey(aesKeyEncoded);

            // Descifrar el mensaje con AES
            String decryptedMessage = encryptionService.decryptWithAES(encryptedMessage, aesKey);

            // Mapear el mensaje descifrado a un objeto DTO
            CustomerStatusDto customerStatusDto = MappingUtils.mapFromString(decryptedMessage, CustomerStatusDto.class);

            shortestPathFinder.setCurrentX(customerStatusDto.getX());
            shortestPathFinder.setCurrentY(customerStatusDto.getY());

            // If taxi has to return to base, use this logic
            if (customerStatusDto.getStatus() == TaxiState.RETURNING_TO_BASE) {
                shortestPathFinder.setTaxiState(TaxiState.RETURNING_TO_BASE);
                while (shortestPathFinder.getTaxiState() == TaxiState.RETURNING_TO_BASE
                        || shortestPathFinder.getTaxiState() == TaxiState.STOPPED) {
                    int[] xy = shortestPathFinder.getNextPosition(1, 1);

                    if (shortestPathFinder.isStop()) {
                        shortestPathFinder.setStop(false);
                        break;
                    }

                    TaxiStatusDto taxiStatusDto;
                    if (shortestPathFinder.getTaxiState() == TaxiState.RETURNING_TO_BASE) {
                        taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.RETURNING_TO_BASE, null);
                    } else {
                        taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.STOPPED, null);
                    }
                    log.info("Enviando posición: " + xy[0] + "," + xy[1]);
                    publishDirection(taxiStatusDto);
                    Thread.sleep(1000);
                }

                TaxiStatusDto taxiStatusDto = new TaxiStatusDto(taxiId, shortestPathFinder.getCurrentX(),
                        shortestPathFinder.getCurrentY(), TaxiState.RETURNING_TO_BASE, null);
                log.info("Enviando posición: " + shortestPathFinder.getCurrentX() + ","
                        + shortestPathFinder.getCurrentY());
                publishDirection(taxiStatusDto);
                return;
            }

            shortestPathFinder.setTaxiState(TaxiState.EN_ROUTE_TO_PICKUP);

            while (shortestPathFinder.getTaxiState() == TaxiState.EN_ROUTE_TO_PICKUP
                    || shortestPathFinder.getTaxiState() == TaxiState.STOPPED) {
                int[] xy = shortestPathFinder.getNextPosition(customerStatusDto.getCustomerX(),
                        customerStatusDto.getCustomerY());

                if (shortestPathFinder.isStop()) {
                    shortestPathFinder.setStop(false);
                    break;
                }

                TaxiStatusDto taxiStatusDto;
                if (shortestPathFinder.getTaxiState() == TaxiState.EN_ROUTE_TO_PICKUP) {
                    taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.EN_ROUTE_TO_PICKUP, null);
                } else {
                    taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.STOPPED, null);
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
                        TaxiState.PICKUP,
                        null);
                log.info("Enviando posición: " + shortestPathFinder.getCurrentX() + ","
                        + shortestPathFinder.getCurrentY());
                publishDirection(taxiStatusDto);
                shortestPathFinder.setTaxiState(TaxiState.EN_ROUTE_TO_DESTINATION);
            }

            while (shortestPathFinder.getTaxiState() == TaxiState.EN_ROUTE_TO_DESTINATION
                    || shortestPathFinder.getTaxiState() == TaxiState.STOPPED) {
                int[] xy = shortestPathFinder.getNextPosition(customerStatusDto.getDestX(),
                        customerStatusDto.getDestY());

                TaxiStatusDto taxiStatusDto;
                if (shortestPathFinder.getTaxiState() == TaxiState.EN_ROUTE_TO_DESTINATION) {
                    taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.EN_ROUTE_TO_DESTINATION, null);
                } else {
                    taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], TaxiState.STOPPED, null);
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

            TaxiStatusDto taxiStatusDto = new TaxiStatusDto(taxiId, shortestPathFinder.getCurrentX(),
                    shortestPathFinder.getCurrentY(), TaxiState.DESTINATION_REACHED, null);
            publishDirection(taxiStatusDto);

        } catch (NumberFormatException e) {
            log.error("Error al parsear las coordenadas: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Formato de mensaje incorrecto: {}", e.getMessage());
        } catch (InterruptedException e) {
            log.error("Error al dormir el hilo: {}", e.getMessage());
            Thread.currentThread().interrupt(); // Restaurar el estado interrumpido
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
        }
    }
}