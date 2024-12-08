package org.example.ec_central.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

@Service
@Slf4j
public class TrafficService {

    private String selectedCity = "London"; // Ciudad inicial predeterminada
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${CTC_URL}") // Obtiene la URL desde la variable de entorno
    private String trafficApiUrl;

    public void startMonitoring() {
        Thread monitoringThread = new Thread(() -> {
            while (true) {
                try {
                    String url = String.format("%s?city=%s", trafficApiUrl, selectedCity);
                    String trafficStatus = restTemplate.getForObject(url, String.class);
                    System.out.println("Traffic status for " + selectedCity + ": " + trafficStatus);
                    log.info("Traffic status for " + selectedCity + ": " + trafficStatus);
                    // Esperar 10 segundos antes de la próxima consulta
                    Thread.sleep(10000);
                } catch (Exception e) {
                    System.err.println("Error consuming /traffic API: " + e.getMessage());
                }
            }
        });

        monitoringThread.setDaemon(true); // Hilo secundario, se detendrá con la aplicación
        monitoringThread.start();

//        Scanner scanner = new Scanner(System.in);
//        while (true) {
//            System.out.println("Enter a new city (current: " + selectedCity + "): ");
//            String input = scanner.nextLine();
//            if (!input.isEmpty()) {
//                selectedCity = input;
//                System.out.println("City updated to: " + selectedCity);
//            }
//        }
    }
}
