package org.example.ec_de.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Service
@Slf4j
public class SocketService {

    @Value("${taxi.id}")
    private String taxiId;

    @Value("${central.ip}")
    private String centralIp;

    private final int PORT = 9090;
    private static final char STX = 0x02;
    private static final char ETX = 0x03;

    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public void logStartupInfo() {
        log.info("Taxi ID: {}", taxiId);
        log.info("Central IP: {}", centralIp);
        log.info("Central Port: {}", PORT);
    }

    public void initialize() {
        this.logStartupInfo();
        new Thread(() -> {
            while (true) {
                try {
                    if (authenticate()) {
                        log.info("Successfully authenticated with EC_Central.");
                        listenAndSendUpdates();
                    } else {
                        log.error("Failed to authenticate with EC_Central.");
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
                }
            }
        }).start();
    }

    public boolean authenticate() throws IOException {
        connectToCentral();
        // Format: AUTH#DigitalEngine#{token}#{taxiID}
        String authMessage = buildMessage(String.format("AUTH#%s#token123", taxiId));
        outputStream.writeUTF(authMessage);
        log.info("Sent authentication message: {}", authMessage);

        String response = inputStream.readUTF();
        if (isValidMessage(response)) {
            String data = extractData(response);
            if ("ACK".equals(data)) {
                log.info("Authentication successful.");
                return true;
            } else {
                log.error("Authentication failed: {}", data);
            }
        } else {
            log.error("Invalid response during authentication.");
        }
        return false;
    }

    private void connectToCentral() throws IOException {
        this.socket = new Socket(centralIp, PORT);
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.inputStream = new DataInputStream(socket.getInputStream());
        log.info("Connected to EC_Central.");
    }

    public void listenAndSendUpdates() {
        try {
            while (true) {
                // Simulación de enviar un mensaje de actualización de posición
                String positionUpdate = buildMessage("UPDATE#Position#5,5");
                outputStream.writeUTF(positionUpdate);
                log.info("Sent position update: " + positionUpdate);

                String response = inputStream.readUTF();
                if (isValidMessage(response)) {
                    String data = extractData(response);
                    log.info("Central response: " + data);
                }

                Thread.sleep(5000);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error in communication: " + e.getMessage());
            closeConnection();

        }
    }

    // Método para cerrar la conexión
    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                log.info("Connection closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Métodos auxiliares para construir y validar mensajes...
    private String buildMessage(String data) {
        char lrc = calculateLRC(data);
        return STX + data + ETX + lrc;
    }

    private char calculateLRC(String data) {
        char lrc = 0;
        for (char c : data.toCharArray()) {
            lrc ^= c;
        }
        return lrc;
    }

    private boolean isValidMessage(String message) {
        if (message.charAt(0) == STX && message.contains(String.valueOf(ETX))) {
            String data = extractData(message);
            char receivedLRC = message.charAt(message.length() - 1);
            char calculatedLRC = calculateLRC(data);
            return receivedLRC == calculatedLRC;
        }
        return false;
    }

    private String extractData(String message) {
        return message.substring(1, message.indexOf(ETX));
    }
}