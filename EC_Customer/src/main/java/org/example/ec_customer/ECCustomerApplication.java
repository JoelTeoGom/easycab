package org.example.ec_customer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class ECCustomerApplication implements CommandLineRunner {

	private final KafkaTemplate<String, String> kafkaTemplate;

	@Value("${client.id}")
	private String clientId;  // ID del cliente que se pasará por argumentos

	@Value("${broker.address}")
	private String brokerAddress;  // Dirección del broker que se pasará por argumentos

	@Value("${file}")
	private String serviceRequestsFile;  // Ruta al archivo de solicitudes de servicio

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
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new ClassPathResource(serviceRequestsFile).getInputStream()))) {
			String line;
			while ((line = br.readLine()) != null) {
				sendServiceRequest(line.trim());
				// Espera 4 segundos entre solicitudes
				TimeUnit.SECONDS.sleep(4);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void sendServiceRequest(String destinationId) {
		String requestMessage = clientId + "#" + destinationId;  // Formato del mensaje: "clientId#destinationId"
		String topic = "service_requests";  // Tópico al que se envían las solicitudes de servicio
		kafkaTemplate.send(topic, requestMessage);
        log.info("[topic{}] Sent service request for destination: {}", topic, destinationId);
	}
}
