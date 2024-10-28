package org.example.ec_central.repository;

import org.example.ec_central.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing Location entities.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Retrieves a Location by its identifier.
     *
     * @param identifier the identifier of the location
     * @return an Optional containing the Location if found, or empty if not found
     */
    Optional<Location> findByIdentifier(String identifier);

    /**
     * Deletes all Location entities.
     */
    @Modifying
    void deleteAll();
}