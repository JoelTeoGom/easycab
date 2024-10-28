package org.example.ec_central.repository;

import org.example.ec_central.model.entity.Taxi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing Taxi entities.
 */
@Repository
public interface TaxiRepository extends JpaRepository<Taxi, Long> {

    /**
     * Retrieves a Taxi by its identifier.
     *
     * @param identifier the identifier of the taxi
     * @return an Optional containing the Taxi if found, or empty if not found
     */
    Optional<Taxi> findByIdentifier(String identifier);

    /**
     * Retrieves all Taxis that are available.
     *
     * @param available the availability status of the taxis
     * @return a list of Taxis that are available
     */
    List<Taxi> findAllByAvailable(boolean available);

    /**
     * Retrieves a Taxi by its identifier.
     *
     * @param identifier the identifier of the taxi
     * @return the Taxi with the given identifier
     */
    Taxi findTaxiByIdentifier(String identifier);

    /**
     * Updates the destination identifier of a Taxi.
     *
     * @param identifier the identifier of the taxi
     * @param destIdentifier the new destination identifier
     */
    @Query("UPDATE Taxi t SET t.destIdentifier = :destIdentifier WHERE t.identifier = :identifier")
    @Modifying
    void updateTaxiDestIdentifier(String identifier, String destIdentifier);
}