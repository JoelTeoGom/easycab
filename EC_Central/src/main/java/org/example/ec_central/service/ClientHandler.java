package org.example.ec_central.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for handling client (taxi) connections and interactions.
 */
@Service
@Slf4j
@Getter
@Setter
public class ClientHandler {

    private final TaxiService taxiService;
    private final MessageHandler messageHandler;
    private final ConcurrentHashMap<String, Socket> connectedTaxis = new ConcurrentHashMap<>();
    private final KafkaAdmin kafkaAdmin;

    /**
     * Constructs a new ClientHandler with the specified dependencies.
     *
     * @param taxiService the service for handling taxi-related operations
     * @param messageHandler the handler for processing messages
     * @param kafkaAdmin the Kafka admin for managing topics
     */
    public ClientHandler(TaxiService taxiService, MessageHandler messageHandler, @Qualifier("kafkaAdmin") KafkaAdmin kafkaAdmin) {
        this.taxiService = taxiService;
        this.messageHandler = messageHandler;
        this.kafkaAdmin = kafkaAdmin;
    }

    /**
     * Handles the connection with a taxi.
     *
     * @param taxiSocket the socket representing the taxi connection
     */
    public void handleTaxiConnection(Socket taxiSocket) {
        try (DataInputStream inputStream = new DataInputStream(taxiSocket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(taxiSocket.getOutputStream())) {
            String authMessage = inputStream.readUTF();
            log.info("Received authentication message: {}", authMessage);

            if (messageHandler.isValidAuthentication(authMessage)) {
                outputStream.writeUTF(messageHandler.buildAck(true)); // Respond with ACK if authentication is successful
                log.info("Taxi authenticated successfully.");

                String id = authMessage.split("#")[1];
                log.info("Updating connected taxis, current connected taxis: {}", connectedTaxis.values().stream().map(Socket::getInetAddress).toList());
                connectedTaxis.put(id, taxiSocket);
                log.info("Connected taxis updated, current connected taxis: {}", connectedTaxis.values().stream().map(Socket::getInetAddress).toList());
                String topicName = "taxi-start-service-" + id;
                NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
                kafkaAdmin.createOrModifyTopics(newTopic);

                log.info("Created topic for client: {}", topicName);

                handleTaxiRequests(inputStream, outputStream, id);

                connectedTaxis.remove(id);
            } else {
                outputStream.writeUTF(messageHandler.buildAck(false)); // Respond with NACK if authentication fails
                log.error("Taxi authentication failed.");
            }

        } catch (IOException e) {
            log.error("Error handling taxi connection: {}", e.getMessage());
        }
    }

    /**
     * Handles requests from authenticated taxis.
     *
     * @param inputStream the input stream to read requests from
     * @param outputStream the output stream to send responses to
     * @param id the identifier of the taxi
     * @throws IOException if an I/O error occurs
     */
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