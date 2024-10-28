package org.example.ec_s;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
@Slf4j
public class EC_S implements CommandLineRunner {

    @Value("${de.ip}")
    private String deIp;

    @Value("${de.port}")
    private int dePort;

    private SensorSocketClient sensorClient;
    private boolean running = true;
    private boolean incidencia = false;

    public static void main(String[] args) {
        SpringApplication.run(EC_S.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (deIp == null || deIp.isEmpty() || dePort == 0) {
            log.warn("Por favor, configura las variables de entorno 'de.ip' y 'de.port'.");
            return;
        }
        log.info("Using socket IP: {}", deIp);
        log.info("Listening on port {}", dePort);

        sensorClient = new SensorSocketClient(deIp, dePort);

        new Thread(() -> {
            while (running) {
                if (incidencia) {
                    sensorClient.sendSensorData(SensorStatus.KO.name());
                    log.info("Enviado KO");
                } else {
                    sensorClient.sendSensorData(SensorStatus.OK.name());
                    log.info("Enviado OK");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Error al dormir el hilo de envío de mensajes de estado");
                    e.printStackTrace();
                }
            }
        }).start();

        listenForIncidents();
    }

    private void listenForIncidents() {
        Scanner scanner = new Scanner(System.in);
        log.info("Presiona 'i' para simular una incidencia (KO) o 'q' para salir.");
        while (running) {
            String input = scanner.nextLine();
            if ("i".equalsIgnoreCase(input)) {
                incidencia = !incidencia;
                log.info("Incidencia simulada: KO enviado.");
            } else if ("q".equalsIgnoreCase(input)) {
                running = false;
                log.info("Saliendo de la aplicación de sensores.");
            }
        }
        scanner.close();
    }
}