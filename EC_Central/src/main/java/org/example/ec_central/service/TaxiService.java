package org.example.ec_central.service;

import org.example.ec_central.model.entity.Taxi;
import org.example.ec_central.model.TaxiStatusDto;
import org.example.ec_central.repository.TaxiRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for handling taxi-related operations.
 */
@Service
public class TaxiService {

    private final TaxiRepository taxiRepository;

    /**
     * Constructs a new TaxiService with the specified repository.
     *
     * @param taxiRepository the repository for managing taxi entities
     */
    public TaxiService(TaxiRepository taxiRepository) {
        this.taxiRepository = taxiRepository;
    }

    /**
     * Assigns an available taxi to the specified coordinates.
     *
     * @param x the x-coordinate to assign the taxi to
     * @param y the y-coordinate to assign the taxi to
     * @return an Optional containing the assigned Taxi if available, otherwise an empty Optional
     */
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

    /**
     * Retrieves all available taxis.
     *
     * @return a list of available taxis
     */
    public List<Taxi> findAllAvailableTaxis() {
        return taxiRepository.findAllByAvailable(true);
    }

    /**
     * Releases a taxi, making it available for assignment.
     *
     * @param taxi the taxi to be released
     */
    public void releaseTaxi(Taxi taxi) {
        taxi.setAvailable(true);
        taxiRepository.save(taxi);
    }

    /**
     * Finds a taxi by its identifier.
     *
     * @param identifier the identifier of the taxi
     * @return an Optional containing the Taxi if found, otherwise an empty Optional
     */
    public Optional<Taxi> findTaxiByIdentifier(String identifier) {
        return taxiRepository.findAll()
                       .stream()
                       .filter(taxi -> taxi.getIdentifier().equals(identifier))
                       .findFirst();
    }

    /**
     * Updates the location and status of a taxi based on the provided TaxiStatusDto.
     *
     * @param taxiStatusDto the data transfer object containing the taxi status and location information
     */
    public void updateTaxiLocationByIdentifier(TaxiStatusDto taxiStatusDto) {
        Taxi taxi = taxiRepository.findTaxiByIdentifier(taxiStatusDto.getTaxiId());
        taxi.setX(taxiStatusDto.getX());
        taxi.setY(taxiStatusDto.getY());
        taxi.setState(taxiStatusDto.getStatus());
        taxiRepository.save(taxi);
    }
}