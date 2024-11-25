package org.example.ec_central.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable class representing the composite key for CustomerTaxiAssignment.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CustomerTaxiId implements Serializable {

    /**
     * The ID of the customer.
     */
    @Column(name = "customer_id")
    private Long customerId;

    /**
     * The ID of the taxi.
     */
    @Column(name = "taxi_id")
    private Long taxiId;
}