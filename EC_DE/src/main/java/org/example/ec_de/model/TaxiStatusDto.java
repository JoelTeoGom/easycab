package org.example.ec_de.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object (DTO) for representing the status of a taxi.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaxiStatusDto {

    /**
     * The unique identifier of the taxi.
     */
    private String taxiId;

    /**
     * The X coordinate of the taxi's current location.
     */
    private int x;

    /**
     * The Y coordinate of the taxi's current location.
     */
    private int y;

    /**
     * The current status of the taxi.
     */
    private TaxiState status;

    private String token;


}