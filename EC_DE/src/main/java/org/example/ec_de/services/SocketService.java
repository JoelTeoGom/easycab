package org.example.ec_de.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.KeyFactory;
import java.security.PublicKey;

/**
 * Service class for handling socket communication with EC_Central.
 */
@Service
@Slf4j
public class SocketService {

    private final EncryptionService encryptionService;

    /**
     * Taxi token
     */
    private String authToken;

    /**
     * Start of Text character.
     */
    private static final char STX = 0x02;

    /**
     * End of Text character.
     */
    private static final char ETX = 0x03;

    /**
     * Port number for the socket connection.
     */
    @Value("${central.port}")
    private final int PORT = 9090;

    /**
     * The unique identifier of the taxi, injected from application properties.
     */
    @Value("${taxi.id}")
    private String taxiId;

    /**
     * The IP address of the central server, injected from application properties.
     */
    @Value("${central.ip}")
    private String centralIp;

    /**
     * Socket for the connection to the central server.
     */
    private Socket socket;

    /**
     * Output stream for sending data to the central server.
     */
    private DataOutputStream outputStream;

    /**
     * Input stream for receiving data from the central server.
     */
    private DataInputStream inputStream;

    public SocketService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    /**
     * Logs startup information including taxi ID, central IP, and port.
     */
    public void logStartupInfo() {
        log.info("Taxi ID: {}", taxiId);
        log.info("Central IP: {}", centralIp);
        log.info("Central Port: {}", PORT);
    }

    /**
     * Initializes the socket service and starts the connection process in a new thread.
     */
    public void initialize() {
        this.logStartupInfo();
        new Thread(() -> {
            while (true) {

                try {
                    if (authenticate()) {
                        log.info("Successfully authenticated with EC_Central.");
                        keepAlive();
                    } else {
                        log.error("Failed to authenticate with EC_Central.");
                        Thread.sleep(5000);

                    }

                } catch (IOException e) {
                    log.error("Error during connection setup: {}", e.getMessage());
                    closeConnection();
                    try {
                        log.info("Retrying connection in 5 seconds...");
                        Thread.sleep(5000);
                    } catch (InterruptedException interruptedException) {
                        log.error("Interrupted while waiting to reconnect: {}", interruptedException.getMessage());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * Authenticates the connection with the central server.
     *
     * @return true if authentication is successful, false otherwise
     * @throws IOException if an I/O error occurs during authentication
     */
    /**
     * Authenticates the connection with the central server.
     *
     * @return true if authentication is successful, false otherwise
     * @throws IOException if an I/O error occurs during authentication
     */
    public boolean authenticate() throws IOException {
        connectToCentral();
        // Formato del mensaje de autenticación: AUTH#DigitalEngine#{token}#{taxiID}
        String authMessage = buildMessage(String.format("AUTH#%s#token123", taxiId));
        outputStream.writeUTF(authMessage);
        log.info("Sent authentication message: {}", authMessage);

        exchangePublicKeys();

        // Leer la respuesta del servidor
        String response = inputStream.readUTF();
        log.info("Received response: {}", response);

        if (isValidMessage(response)) {
            // Extraer los datos de la respuesta (sin STX, ETX y LRC)
            String data = extractData(response);
            log.info("Extracted data: {}", data);

            // Dividir los datos recibidos por el separador (FIELD_SEPARATOR: #)
            String[] parts = data.split("#");

            // Verificar si la respuesta es un ACK con token
            if (parts.length == 2 && "ACK".equals(parts[0])) {
                String token = parts[1];
                this.authToken = token;
                log.info("Authentication successful. Token: {}", token);
                return true;
            } else {
                log.error("Authentication failed or invalid response format: {}", data);
            }
        } else {
            log.error("Invalid message received during authentication.");
        }
        return false;
    }


    /**
     * Intercambia claves públicas con EC_Central durante la autenticación.
     *
     * @throws IOException si ocurre un error de E/S
     */
    private void exchangePublicKeys() throws IOException {
        try {
            // Enviar clave pública del taxi (EC_DE)
            String taxiPublicKeyBase64 = Base64.getEncoder().encodeToString(encryptionService.getTaxiPublicKey().getEncoded());
            outputStream.writeUTF(taxiPublicKeyBase64);
            log.info("Sent taxi public key to EC_Central.");

            // Recibir clave pública de Central
            String centralPublicKeyBase64 = inputStream.readUTF();
            byte[] centralPublicKeyBytes = Base64.getDecoder().decode(centralPublicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey centralPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(centralPublicKeyBytes));
            encryptionService.setCentralPublicKey(centralPublicKey);
            log.info("Received and stored EC_Central's public key.{}", centralPublicKey);
        } catch (Exception e) {
            log.error("Error during key exchange with EC_Central: {}", e.getMessage());
            throw new IOException("Public key exchange failed", e);
        }
    }




    /**
     * Connects to the central server.
     *
     * @throws IOException if an I/O error occurs during connection
     */
    private void connectToCentral() throws IOException {
        this.socket = new Socket(centralIp, PORT);
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.inputStream = new DataInputStream(socket.getInputStream());
        log.info("Connected to EC_Central.");
    }

    /**
     * Keeps the connection alive by sleeping the thread.
     */
    public void keepAlive() {
        try {
            while (true) {
                if (socket == null || socket.isClosed() || !socket.isConnected()) {
                    log.error("Connection lost. Attempting to reconnect...");
                    closeConnection();
                    initialize(); // Intentar reconectar
                    break; // Sal del bucle y vuelve a iniciar la conexión
                }
                log.info("Connection is alive.");
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            log.error("Thread interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt(); // Restablece el estado interrumpido
            closeConnection();
        }
    }


    /**
     * Closes the socket connection.
     */
    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                log.info("Connection closed.");
            }
        } catch (IOException e) {
            log.error("Error while closing the connection: {}", e.getMessage());
        } finally {
            authToken = null; // Limpiar el token al cerrar la conexión
            log.info("Authentication token cleared.");
        }
    }


    /**
     * Builds a message with the given data, including STX, ETX, and LRC.
     *
     * @param data the data to include in the message
     * @return the constructed message
     */
    private String buildMessage(String data) {
        char lrc = calculateLRC(data);
        return STX + data + ETX + lrc;
    }

    /**
     * Calculates the Longitudinal Redundancy Check (LRC) for the given data.
     *
     * @param data the data to calculate the LRC for
     * @return the calculated LRC
     */
    private char calculateLRC(String data) {
        char lrc = 0;
        for (char c : data.toCharArray()) {
            lrc ^= c;
        }
        return lrc;
    }

    /**
     * Validates the given message by checking STX, ETX, and LRC.
     *
     * @param message the message to validate
     * @return true if the message is valid, false otherwise
     */
    private boolean isValidMessage(String message) {
        if (message.charAt(0) == STX && message.contains(String.valueOf(ETX))) {
            String data = extractData(message);
            char receivedLRC = message.charAt(message.length() - 1);
            char calculatedLRC = calculateLRC(data);
            return receivedLRC == calculatedLRC;
        }
        return false;
    }

    /**
     * Extracts the data from the given message, excluding STX and ETX.
     *
     * @param message the message to extract data from
     * @return the extracted data
     */
    private String extractData(String message) {
        return message.substring(1, message.indexOf(ETX));
    }

    /**
     * Returns the authentication token.
     *
     * @return the authentication token
     */
    public String getAuthToken() {
        return authToken;
    }
}