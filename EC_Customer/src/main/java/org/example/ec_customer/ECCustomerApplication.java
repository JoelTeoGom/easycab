package org.example.ec_customer;

import ch.qos.logback.core.net.server.Client;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.ec_customer.service.ClientKafkaListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class ECCustomerApplication implements CommandLineRunner {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${client.id}")
    private String clientId;

    @Value("${spring.kafka.bootstrap-servers}")
    private String brokerAddress;

    @Value("${file}")
    private String serviceRequestsFile;

    public ECCustomerApplication(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(ECCustomerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        processServiceRequests();
    }

    private void processServiceRequests() {
        log.info("Sending service requests, using broker: {}", brokerAddress);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource(serviceRequestsFile).getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                ClientKafkaListener.semaphore.acquire();

                sendServiceRequest(line.trim());

                // Espera 4 segundos entre solicitudes
                TimeUnit.SECONDS.sleep(4);

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendServiceRequest(String destinationId) {
        String requestMessage = clientId + "#" + destinationId; // Formato del mensaje: "clientId#destinationId"
        String topic = "service_requests"; // Tópico al que se envían las solicitudes de servicio
        kafkaTemplate.send(topic, requestMessage);
        log.info("[topic{}] Sent service request for destination: {}", topic, destinationId);
    }
}