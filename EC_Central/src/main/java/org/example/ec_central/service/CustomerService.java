package org.example.ec_central.service;

import lombok.AllArgsConstructor;
import org.example.ec_central.model.Customer;
import org.example.ec_central.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Customer getCustomerByIdentifier(String identifier) {
        return customerRepository.getCustomerByIdentifier(identifier).orElseThrow(() -> new RuntimeException("Customer not found"));
    }


}
