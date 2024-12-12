package org.example.ec_registry.controller;

import org.example.ec_registry.service.TaxiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/taxis")
public class TaxiController {

    private final TaxiService taxiService;

    public TaxiController(TaxiService taxiService) {
        this.taxiService = taxiService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerTaxi(@RequestBody Map<String, String> request) {
        String identifier = request.get("id");
        taxiService.registerTaxi(identifier);
        return ResponseEntity.ok("Taxi registered successfully.");
    }


    @DeleteMapping("/unregister/{id}")
    public ResponseEntity<String> deregisterTaxi(@PathVariable String id) {
        taxiService.unregisterTaxi(id);
        return ResponseEntity.ok("Taxi deregistered successfully.");
    }
}
