package org.example.ec_ctc.controllers;

import org.example.ec_ctc.services.TrafficService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrafficController {

    private final TrafficService trafficService;

    public TrafficController(TrafficService trafficService) {
        this.trafficService = trafficService;
    }

    @GetMapping("/traffic")
    public String getTrafficStatus(@RequestParam String city) {
        return trafficService.getTrafficStatus(city);
    }
}
