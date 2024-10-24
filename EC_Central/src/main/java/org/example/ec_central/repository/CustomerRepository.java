package org.example.ec_central.repository;

import org.example.ec_central.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    // Puedes agregar métodos adicionales aquí si es necesario
    Optional<Customer> getCustomerByIdentifier(String identifier);
}
