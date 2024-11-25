package org.example.ec_de;

import lombok.extern.slf4j.Slf4j;
import org.example.ec_de.model.ShortestPathFinder;
import org.example.ec_de.services.KafkaService;
import org.example.ec_de.services.KafkaWaitForStart;
import org.example.ec_de.services.SensorService;
import org.example.ec_de.services.SocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class EcDeApplication implements CommandLineRunner {

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

    }
}
