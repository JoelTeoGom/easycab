package org.example.ec_central.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.ec_central.model.entity.Location;
import org.example.ec_central.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Service class for managing locations.
 */
@Service
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    /**
     * Path to the locations file, injected from application properties.
     */
    @Value("${locations.file.path}")
    private String filePath;

    /**
     * Constructor for LocationService.
     *
     * @param locationRepository the repository for managing Location entities
     */
    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Loads locations from a file and saves them to the repository.
     * This method is called after the bean is constructed.
     */
    @PostConstruct
    @Transactional
    public void loadLocationsFromFile() {
        log.info("Loading locations from file: {}", filePath);
        log.info("Deleting all locations from the database");
        locationRepository.deleteAll();
        log.info("All locations deleted from the database");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(filePath))))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    String identifier = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    Location location = new Location();
                    location.setIdentifier(identifier);
                    location.setX(x);
                    location.setY(y);

                    locationRepository.save(location);
                    log.debug("Saved location: {} at {},{}", identifier, x, y);
                }
            }
        } catch (IOException e) {
            log.error("Error reading locations file: {}", e.getMessage());
        }
    }

    /**
     * Finds a location by its identifier.
     *
     * @param identifier the identifier of the location
     * @return the Location object
     * @throws RuntimeException if the location is not found
     */
    public Location findLocationByIdentifier(String identifier) {
        return locationRepository.findByIdentifier(identifier)
                       .orElseThrow(() -> new RuntimeException("Location not found: " + identifier));
    }


}