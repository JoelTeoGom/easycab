package org.example.ec_central.service;

import org.example.ec_central.model.CityMap;
import org.example.ec_central.model.Taxi;
import org.example.ec_central.model.TaxiStatusDto;
import org.example.ec_central.repository.TaxiRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaxiService {

    private final TaxiRepository taxiRepository;

    public TaxiService(TaxiRepository taxiRepository) {
        this.taxiRepository = taxiRepository;
    }

    public CityMap populateMapWithTaxis() {
        CityMap cityMap = new CityMap();
        List<Taxi> taxis = taxiRepository.findAll();

        for (Taxi taxi : taxis) {
            int x = taxi.getX();
            int y = taxi.getY();

            // Usa el identificador del taxi como "T-ID" para actualizar el mapa.
            String identifier = "T-" + taxi.getId();
            cityMap.updatePosition(x, y, identifier);
        }

        return cityMap;
    }

    public Optional<Taxi> assignAvailableTaxi(int x, int y) {
        Optional<Taxi> availableTaxi = taxiRepository.findAll()
                                               .stream()
                                               .filter(Taxi::isAvailable)
                                               .findFirst();

        availableTaxi.ifPresent(taxi -> {
            taxi.setAvailable(false);
            taxi.setX(x);
            taxi.setY(y);
            taxiRepository.save(taxi);
        });

        return availableTaxi;
    }

    public List<Taxi>findAllAvailableTaxis(){
        return taxiRepository.findAllByAvailable(true);
    }

    public void releaseTaxi(Taxi taxi) {
        taxi.setAvailable(true);
        taxiRepository.save(taxi);
    }

    public Optional<Taxi> findTaxiByIdentifier(String identifier) {
        return taxiRepository.findAll()
                       .stream()
                       .filter(taxi -> taxi.getIdentifier().equals(identifier))
                       .findFirst();
    }

    public void updateTaxiLocationByIdentifier(TaxiStatusDto taxiStatusDto) {
        Taxi taxi = taxiRepository.findTaxiByIdentifier(taxiStatusDto.getTaxiId());
        taxi.setX(taxiStatusDto.getX());
        taxi.setY(taxiStatusDto.getY());

        taxiRepository.save(taxi);
    }


 }