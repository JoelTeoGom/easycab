package org.example.ec_central.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing the assignment of a customer to a taxi.
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerTaxiAssignment {

    /**
     * The composite key for the customer-taxi assignment.
     */
    @EmbeddedId
    private CustomerTaxiId id;

    public CustomerTaxiAssignment(Long customerId, Long taxiId) {

        this.id = new CustomerTaxiId(customerId, taxiId);
    }
}