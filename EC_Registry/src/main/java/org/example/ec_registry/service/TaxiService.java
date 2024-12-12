package org.example.ec_registry.service;

import org.example.ec_registry.model.entity.Taxi;
import org.example.ec_registry.model.TaxiState;
import org.example.ec_registry.repository.TaxiRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaxiService {

    private final TaxiRepository taxiRepository;

    public TaxiService(TaxiRepository taxiRepository) {
        this.taxiRepository = taxiRepository;
    }

    public Taxi registerTaxi(String identifier) {
        if (taxiRepository.findByIdentifier(identifier).isPresent()) {
            throw new IllegalArgumentException("Taxi ya registrado");
        }
        System.out.println("ID: -----------------"+identifier);
        Taxi taxi = new Taxi(
                identifier,  // Identifier único
                true,        // Disponible
                1,           // Posición inicial X
                1,           // Posición inicial Y
                null,        // Sin destino inicial
                TaxiState.IDLE // Estado inicial
        );
        return taxiRepository.save(taxi);
    }

    public void unregisterTaxi(String identifier) {
        Taxi taxi = taxiRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Taxi no encontrado"));

        taxiRepository.delete(taxi);
    }


    public Optional<Taxi> getTaxi(String identifier) {
        return taxiRepository.findByIdentifier(identifier);
    }
}
