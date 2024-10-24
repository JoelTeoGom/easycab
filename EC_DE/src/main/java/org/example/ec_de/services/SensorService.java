package org.example.ec_de.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.ec_de.model.SensorStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

@Service
@Slf4j
public class SensorService {
    private final KafkaService kafkaService;
    private ServerSocket serverSocket;
    private Socket socket;
    private final KafkaService kafka;

    public SensorService(KafkaService kafka, KafkaService kafkaService) {
        this.kafka = kafka;
        this.kafkaService = kafkaService;
    }

    @PostConstruct
    public void startReceiving() {
        // Ejecutar la recepción en un hilo separado
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(6969);
                log.info("Waiting for a connection...");
                while (true) {
                    socket = serverSocket.accept(); // Esperar a que un cliente se conecte
                    log.info("Client connected: {}", socket.getInetAddress());

                    // Escuchar mensajes entrantes
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        kafkaService.getShortestPathFinder().setStop(!inputLine.equals(SensorStatus.OK.name()));
                        log.info("Received: {}", inputLine);
                    }
                }
            } catch (IOException e) {
                log.error("Error al recibir datos del sensor");
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();
    }

    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Error al cerrar la conexión");
            e.printStackTrace();
        }
    }
}