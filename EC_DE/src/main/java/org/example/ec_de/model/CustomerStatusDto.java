package org.example.ec_de.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object (DTO) for representing the status of a customer.
 * Extends the TaxiStatusDto class to include customer-specific coordinates.
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerStatusDto extends TaxiStatusDto {

    /**
     * The X coordinate of the customer's current location.
     */
    private int customerX;

    /**
     * The Y coordinate of the customer's current location.
     */
    private int customerY;

    /**
     * The X coordinate of the customer's destination.
     */
    private int destX;

    /**
     * The Y coordinate of the customer's destination.
     */
    private int destY;
}