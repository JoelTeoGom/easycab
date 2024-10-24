package org.example.ec_central.repository;

import org.example.ec_central.model.Taxi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxiRepository extends JpaRepository<Taxi, Long> {
    Optional<Taxi> findByIdentifier(String identifier);

    List<Taxi>findAllByAvailable(boolean available);

    Taxi findTaxiByIdentifier(String identifier);

    @Query("UPDATE Taxi t SET t.destIdentifier = :destIdentifier WHERE t.identifier = :identifier")
    @Modifying
    void updateTaxiDestIdentifier(String identifier, String destIdentifier);
}
