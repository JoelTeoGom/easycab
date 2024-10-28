package org.example.ec_central.repository;

import org.example.ec_central.model.entity.CustomerTaxiAssignment;
import org.example.ec_central.model.entity.CustomerTaxiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing CustomerTaxiAssignment entities.
 */
@Repository
public interface CustomerTaxiAssignmentRepository extends JpaRepository<CustomerTaxiAssignment, CustomerTaxiId> {

    /**
     * Retrieves a CustomerTaxiAssignment by the taxi ID.
     *
     * @param taxiId the ID of the taxi
     * @return an Optional containing the CustomerTaxiAssignment if found, or empty
     *         if not found
     */
    Optional<CustomerTaxiAssignment> findByIdTaxiId(Long taxiId);

    @Modifying
    void deleteAllByIdTaxiId(Long taxiId);

}
