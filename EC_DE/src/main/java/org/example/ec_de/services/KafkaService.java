package org.example.ec_de.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.ec_de.model.SensorStatus;
import org.example.ec_de.model.ShortestPathFinder;
import org.example.ec_de.model.TaxiStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
public class KafkaService {
    @Value("${taxi.id}")
    private String taxiId;


    private final KafkaTemplate<String, String> kafkaTemplate;



    private final ShortestPathFinder shortestPathFinder = new ShortestPathFinder();


    // Publicar los datos del sensor en Kafka
    public void publishSensorData(String sensorData) {

        kafkaTemplate.send("sensor-data-topic", sensorData);
        log.info("Published Kafka message: {}", sensorData);
    }

    public void publishTaxiAvailableEvent(String taxiIdentifier) {
        String message = "Taxi " + taxiIdentifier + " has arrived at destination and is available";
        kafkaTemplate.send("taxi-status", message);
        log.info("Published Kafka event: {}", message);
    }

    public void publishDirection(TaxiStatusDto direction) {
        String taxiId = direction.getTaxiId();
        int x = direction.getX();
        int y = direction.getY();
        String status = direction.getStatus();

        // Formato: taxiId,x,y,status
        String message = taxiId + "," + x + "," + y + "," + status;

        kafkaTemplate.send("taxi-directions", message);
    }
    @KafkaListener(topics = "taxi-status", groupId = "digital-engine-group")
    public void listenTaxiStatusUpdates(String message) {
        log.info("Received Taxi Status Update: " + message);
    }

    @KafkaListener(topics = "taxi-map-updates", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenTaxiMap(String message) {
        log.info("Received Taxi Status Update: " + message);
    }

    @KafkaListener(topics = "#{@taxiIdTopic}", groupId = "digital-engine-group")
    public void listenToClientResponses(String message) {
        System.out.println("Recibido mensaje: " + message);

        try {
            String[] coords = message.replace("\"", "").split(",");

            if (coords.length != 2) {
                throw new IllegalArgumentException("El mensaje no contiene coordenadas válidas");
            }

            int x = Integer.parseInt(coords[0].trim());
            int y = Integer.parseInt(coords[1].trim());

            shortestPathFinder.setCurrentX(1);
            shortestPathFinder.setCurrentY(1);

            while (!shortestPathFinder.isStop()) {
                int[] xy = shortestPathFinder.getNextPosition(x, y);

                TaxiStatusDto taxiStatusDto = new TaxiStatusDto(taxiId, xy[0], xy[1], SensorStatus.OK.name());
                publishDirection(taxiStatusDto);

                System.out.println("["+xy[0]+","+xy[1]+"]");
                Thread.sleep(1000);
            }

            TaxiStatusDto taxiStatusDto = new TaxiStatusDto(taxiId, x, y, SensorStatus.KO.name());
            publishDirection(taxiStatusDto);

        } catch (NumberFormatException e) {
            log.error("Error al parsear las coordenadas: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Formato de mensaje incorrecto: " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("Error al dormir el hilo: " + e.getMessage());
            Thread.currentThread().interrupt();  // Restaurar el estado interrumpido
        } catch (Exception e) {
            log.error("Error inesperado: " + e.getMessage());
        }
    }


}
