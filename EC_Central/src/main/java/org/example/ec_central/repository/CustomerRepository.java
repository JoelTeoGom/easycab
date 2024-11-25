package org.example.ec_central.repository;

import org.example.ec_central.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing Customer entities.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Retrieves a customer by their identifier.
     *
     * @param identifier the identifier of the customer
     * @return an Optional containing the customer if found, or empty if not found
     */
    Optional<Customer> getCustomerByIdentifier(String identifier);
}