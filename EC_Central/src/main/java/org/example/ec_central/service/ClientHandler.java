package org.example.ec_central.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.example.ec_central.repository.TaxiRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private final TaxiRepository taxiRepository;
    private final EncryptionService encryptionService;
    //mapa para registrar los tokens
    private Map<String, String> tokenRegistry = new HashMap<>();

    /**
     * Constructs a new ClientHandler with the specified dependencies.
     *
     * @param taxiService the service for handling taxi-related operations
     * @param messageHandler the handler for processing messages
     * @param kafkaAdmin the Kafka admin for managing topics
     */
    public ClientHandler(TaxiService taxiService, MessageHandler messageHandler, @Qualifier("kafkaAdmin") KafkaAdmin kafkaAdmin, TaxiRepository taxiRepository, EncryptionService encryptionService) {
        this.taxiService = taxiService;
        this.messageHandler = messageHandler;
        this.kafkaAdmin = kafkaAdmin;
        this.taxiRepository = taxiRepository;
        this.encryptionService = encryptionService;
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
                String id = authMessage.split("#")[1];
                // Verificar si el taxi está registrado en la base de datos a través del módulo EC_Registry
                if (!isTaxiRegistered(id)) {
                    outputStream.writeUTF(messageHandler.buildAck(false)); // Responder con NACK si no está registrado
                    log.error("Taxi {} is not registered. Connection rejected.", id);
                    return; // Finalizar el manejo de la conexión
                }

                // Intercambiar claves públicas
                exchangePublicKeys(inputStream, outputStream, id);


                // Responder con el token
                String token = UUID.randomUUID().toString();
                String ackMessage = messageHandler.buildAckWithToken(token);
                outputStream.writeUTF(ackMessage);
                log.info("Taxi authenticated successfully. Token: {}", token);


                log.info("Updating connected taxis, current connected taxis: {}", connectedTaxis.values().stream().map(Socket::getInetAddress).toList());


                connectedTaxis.put(id, taxiSocket);
                tokenRegistry.put(id, token); // tokenRegistry es un mapa id -> token

                log.info("Connected taxis updated, current connected taxis: {}", connectedTaxis.values().stream().map(Socket::getInetAddress).toList());
                String topicName = "taxi-start-service-" + id;
                NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
                kafkaAdmin.createOrModifyTopics(newTopic);
                log.info("Created topic for client: {}", topicName);

                handleTaxiRequests(inputStream, outputStream, id);

                connectedTaxis.remove(id);
                tokenRegistry.remove(id);
            } else {
                outputStream.writeUTF(messageHandler.buildAck(false)); // Respond with NACK if authentication fails
                log.error("Taxi authentication failed.");
            }

        } catch (IOException e) {
            log.error("Error handling taxi connection: {}", e.getMessage());
        }
    }



    private void exchangePublicKeys(DataInputStream inputStream, DataOutputStream outputStream, String taxiId) throws IOException {
        try {
            // Recibir la clave pública del taxi
            String taxiPublicKeyBase64 = inputStream.readUTF();
            byte[] taxiPublicKeyBytes = Base64.getDecoder().decode(taxiPublicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey taxiPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(taxiPublicKeyBytes));
            encryptionService.registerTaxiPublicKey(taxiId, taxiPublicKey);
            log.info("Received and registered public key for taxi: {}", taxiId);
            log.info("PUBLIC KEY: {}", taxiPublicKey);

            // Enviar la clave pública de Central al taxi
            String centralPublicKeyBase64 = Base64.getEncoder().encodeToString(encryptionService.getCentralPublicKey().getEncoded());
            outputStream.writeUTF(centralPublicKeyBase64);
            log.info("Sent public key to taxi: {}", taxiId);

        } catch (Exception e) {
            log.error("Error exchanging public keys with taxi {}: {}", taxiId, e.getMessage());
            throw new IOException("Public key exchange failed", e);
        }
    }




    /**
     * Verifica si el taxi está registrado en la base de datos a través de EC_Registry.
     *
     * @param taxiId el ID del taxi a verificar
     * @return true si el taxi está registrado, false en caso contrario
     */
    private boolean isTaxiRegistered(String taxiId) {
        try {
            return taxiRepository.findByIdentifier(taxiId).isPresent();
        } catch (Exception e) {
            log.error("Error checking taxi registration status: {}", e.getMessage());
            return false;
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