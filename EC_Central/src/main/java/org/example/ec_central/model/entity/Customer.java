package org.example.ec_central.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.example.ec_central.model.CustomerState;

/**
 * Entity class representing a Customer.
 */
@Entity
@Getter
@Setter
public class Customer {

    /**
     * The unique identifier for the customer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The identifier for the customer.
     */
    private String identifier;

    /**
     * The x-coordinate of the customer's position.
     */
    private int x;

    /**
     * The y-coordinate of the customer's position.
     */
    private int y;

    /**
     * The identifier for the customer's destination.
     */
    private String destIdentifier;

    /**
     * The state of the customer.
     */
    @Enumerated(EnumType.STRING)
    private CustomerState state;

    /**
     * Returns the position of the customer as a comma-separated string.
     *
     * @return the position of the customer
     */
    public String getPosition() {
        return x + "," + y;
    }
}