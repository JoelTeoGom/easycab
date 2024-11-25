package org.example.ec_central.service;

import lombok.AllArgsConstructor;
import org.example.ec_central.model.entity.Customer;
import org.example.ec_central.repository.CustomerRepository;
import org.springframework.stereotype.Service;

/**
 * Service class for handling customer-related operations.
 */
@Service
@AllArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    /**
     * Retrieves a customer by their identifier.
     *
     * @param identifier the identifier of the customer
     * @return the Customer with the given identifier
     * @throws RuntimeException if the customer is not found
     */
    public Customer getCustomerByIdentifier(String identifier) {
        return customerRepository.getCustomerByIdentifier(identifier).orElseThrow(() -> new RuntimeException("Customer not found"));
    }
}