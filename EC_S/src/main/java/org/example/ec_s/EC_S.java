package org.example.ec_s;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
@Slf4j
public class EC_S implements CommandLineRunner {

    private SensorSocketClient sensorClient;
    private boolean running = true;

    public static void main(String[] args) {
        SpringApplication.run(EC_S.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 2) {
            log.warn("Por favor, proporciona la IP y el puerto del Digital Engine (EC_DE).");
            return;
        }
        log.info("Using socket IP: {}", args[0]);
        log.info("Listening on port {}", args[1]);
        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        sensorClient = new SensorSocketClient(ip, port);

        new Thread(() -> {
            while (running) {
                sensorClient.sendSensorData(SensorStatus.OK.name());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Error al dormir el hilo de envío de mensajes de estado");
                    e.printStackTrace();
                }
            }
        }).start();

        // Escuchar la entrada del usuario para simular incidencias
        listenForIncidents();
    }

    private void listenForIncidents() {
        Scanner scanner = new Scanner(System.in);
        log.info("Presiona 'i' para simular una incidencia (KO) o 'q' para salir.");
        while (running) {
            String input = scanner.nextLine();
            if ("i".equalsIgnoreCase(input)) {
                sensorClient.sendSensorData(SensorStatus.KO.name());
                log.info("Incidencia simulada: KO enviado.");
            } else if ("q".equalsIgnoreCase(input)) {
                running = false;
                log.info("Saliendo de la aplicación de sensores.");
            }
        }
        scanner.close();
    }
}
