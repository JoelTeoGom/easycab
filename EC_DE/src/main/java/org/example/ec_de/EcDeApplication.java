package org.example.ec_de;

import lombok.extern.slf4j.Slf4j;
import org.example.ec_de.model.ShortestPathFinder;
import org.example.ec_de.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

@SpringBootApplication
@Slf4j
public class EcDeApplication implements CommandLineRunner {
    @Value("${taxi.id}")
    private String taxiId;
    @Autowired
    private KafkaWaitForStart kafkaWaitForStart;
    @Autowired
    private SensorService sensor;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private SocketService socketService;

    private ShortestPathFinder shortestPathFinder = new ShortestPathFinder();

    public static void main(String[] args) {
        SpringApplication.run(EcDeApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("DE started with id {}", kafkaService.getTaxiId());
        socketService.initialize();

        // Ejecutar el menú interactivo
        launchInteractiveMenu();
    }



    private void launchInteractiveMenu() {
        try {
            String registryUrl = System.getenv("REGISTRY_URL"); // Leer la URL del servidor del entorno
            if (registryUrl == null) {
                throw new IllegalArgumentException("REGISTRY_URL no configurado en el entorno");
            }

            DigitalEngineClient client = new DigitalEngineClient(registryUrl);

//            System.out.println("DANDO DE BAJA");
//            client.registerTaxi(taxiId);



            System.out.println("--- Menú EC_DE ---");
            System.out.println("1. Registrar Taxi");
            System.out.println("2. Dar de Baja Taxi");
            System.out.println("3. Consultar Estado del Taxi");
            System.out.println("4. Salir");

            java.util.Scanner scanner = new java.util.Scanner(System.in);
            while (true) {
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine(); // Consumir la nueva línea

                if (option == 4) {
                    System.out.println("Saliendo...");
                    break;
                }

                switch (option) {
                    case 1:
                        client.registerTaxi(taxiId);
                        break;
                    case 2:
                        client.unregisterTaxi(taxiId);
                        socketService.closeConnection();
                        break;
                    case 3:
                        client.getTaxiStatus(taxiId);
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
