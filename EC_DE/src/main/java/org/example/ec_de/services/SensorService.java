package org.example.ec_de.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.ec_de.model.SensorStatus;
import org.example.ec_de.model.TaxiState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Service class for handling sensor data reception and processing.
 */
@Service
@Slf4j
public class SensorService {
    /**
     * KafkaService instance for publishing sensor data.
     */
    private final KafkaService kafkaService;

    /**
     * ServerSocket for listening to sensor connections.
     */
    private ServerSocket serverSocket;

    /**
     * Socket for handling individual sensor connections.
     */
    private Socket socket;

    /**
     * Port number for the sensor server, injected from application properties.
     */
    @Value("${sensor.port}")
    private int sensorPort;

    /**
     * Constructor for SensorService.
     *
     * @param kafkaService the KafkaService instance for publishing sensor data
     */
    public SensorService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    /**
     * Initializes the sensor data reception process.
     * This method is called after the bean is constructed.
     */
    @PostConstruct
    public void startReceiving() {
        // Execute the reception in a separate thread
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(sensorPort);
                log.info("Waiting for a connection...");
                while (true) {
                    socket = serverSocket.accept(); // Wait for a client to connect
                    log.info("Client connected: {}", socket.getInetAddress());

                    // Listen for incoming messages
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        if (!inputLine.equals(SensorStatus.OK.name())) {
                            kafkaService.getShortestPathFinder().setTaxiState(TaxiState.STOPPED);
                        }
                        log.info("Received: {}", inputLine);
                    }
                }
            } catch (IOException e) {
                log.error("Error receiving sensor data");
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();
    }

    /**
     * Closes the sensor connection.
     */
    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Error closing the connection");
            e.printStackTrace();
        }
    }
}