package org.example.ec_central.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.example.ec_central.model.ConnectedTaxis;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Getter
@Setter
public class ClientHandler {

    private final TaxiService taxiService;
    private final MessageHandler messageHandler;
    private final ConcurrentHashMap<String, Socket> connectedTaxis = new ConcurrentHashMap<>();
    private final KafkaAdmin kafkaAdmin;


    public ClientHandler(TaxiService taxiService, MessageHandler messageHandler, @Qualifier("kafkaAdmin") KafkaAdmin kafkaAdmin) {
        this.taxiService = taxiService;
        this.messageHandler = messageHandler;
        this.kafkaAdmin = kafkaAdmin;
    }

    // Método para manejar la interacción con el taxi
    public void handleTaxiConnection(Socket taxiSocket) {
        try (DataInputStream inputStream = new DataInputStream(taxiSocket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(taxiSocket.getOutputStream())) {
            String authMessage = inputStream.readUTF();
            log.info("Received authentication message: {}", authMessage);

            if (messageHandler.isValidAuthentication(authMessage)) {
                outputStream.writeUTF(messageHandler.buildAck(true)); // Responder con ACK si autenticación exitosa
                log.info("Taxi authenticated successfully.");

                String id = authMessage.split("#")[1];
                connectedTaxis.put(id, taxiSocket);

                String topicName = "taxi-start-service-" + id;
                NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
                kafkaAdmin.createOrModifyTopics(newTopic);

                log.info("Created topic for client: {}", topicName);

                handleTaxiRequests(inputStream, outputStream, id);

                connectedTaxis.remove(id);
            } else {
                outputStream.writeUTF(messageHandler.buildAck(false)); // Responder con NACK si autenticación fallida
                log.error("Taxi authentication failed.");
            }

        } catch (IOException e) {
            log.error("Error handling taxi connection: {}", e.getMessage());
        }
    }

    // Método para manejar solicitudes de taxis autenticados
    private void handleTaxiRequests(DataInputStream inputStream, DataOutputStream outputStream, String id) throws IOException {
        String request;

        while (true) {
            request = inputStream.readUTF();
            log.info("Received request: {}", request);

            if (messageHandler.isValidMessage(request)) {
                String data = messageHandler.extractData(request);

                outputStream.writeUTF(messageHandler.buildAck(true));
                log.info("Processed request from taxi: {}", data);
            } else {
                outputStream.writeUTF(messageHandler.buildAck(false));
                log.error("Invalid request received from taxi.");
            }

            if (request.contains("EOT")) {
                log.info("End of transmission received. Closing connection.");
                break;
            }
        }
    }
}