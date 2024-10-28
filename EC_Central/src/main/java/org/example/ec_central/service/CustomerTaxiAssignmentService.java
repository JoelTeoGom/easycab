package org.example.ec_central.service;

import lombok.RequiredArgsConstructor;
import org.example.ec_central.model.entity.CustomerTaxiAssignment;
import org.example.ec_central.repository.CustomerRepository;
import org.example.ec_central.repository.CustomerTaxiAssignmentRepository;
import org.example.ec_central.repository.TaxiRepository;
import org.springframework.stereotype.Service;

/**
 * Service class for handling customer-taxi assignment operations.
 */
@Service
@RequiredArgsConstructor
public class CustomerTaxiAssignmentService {
    private final CustomerTaxiAssignmentRepository customerTaxiAssignmentRepository;
    private final CustomerRepository customerRepository;
    private final TaxiRepository taxiRepository;

    /**
     * Creates a new CustomerTaxiAssignment.
     *
     * @param customerId the ID of the customer
     * @param taxiId     the ID of the taxi
     * @return the created CustomerTaxiAssignment
     * @throws RuntimeException if the customer or taxi is not found
     */
    public CustomerTaxiAssignment createCustomerTaxiAssignment(Long customerId, Long taxiId) {
        return this.customerTaxiAssignmentRepository.save(new CustomerTaxiAssignment(customerId, taxiId));
    }

    public CustomerTaxiAssignment findByTaxiId(Long taxiId) {
        return this.customerTaxiAssignmentRepository.findByIdTaxiId(taxiId).orElseThrow();
    }




}
