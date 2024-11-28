package org.example.ec_central;

import jakarta.annotation.PostConstruct;
import org.example.ec_central.service.TrafficService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcCentralApplication {

    private final TrafficService trafficService;

    public EcCentralApplication(TrafficService trafficService) {
        this.trafficService = trafficService;
    }

    public static void main(String[] args) {
        SpringApplication.run(EcCentralApplication.class, args);
    }

    @PostConstruct
    public void startMonitoring() {
        trafficService.startMonitoring();
    }
}
