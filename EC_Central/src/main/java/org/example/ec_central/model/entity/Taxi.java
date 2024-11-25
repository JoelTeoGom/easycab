package org.example.ec_central.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.example.ec_central.model.TaxiState;

/**
 * Entity class representing a Taxi.
 */
@Entity
@Getter
@Setter
public class Taxi {

    /**
     * The unique identifier for the taxi.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The identifier for the taxi.
     */
    private String identifier;

    /**
     * Indicates whether the taxi is available.
     */
    private boolean available;

    /**
     * The x-coordinate of the taxi's position.
     */
    private int x;

    /**
     * The y-coordinate of the taxi's position.
     */
    private int y;

    /**
     * The identifier for the taxi's destination.
     */
    private String destIdentifier;

    /**
     * The state of the taxi.
     */
    @Enumerated(EnumType.STRING)
    private TaxiState state;

    /**
     * Returns the position of the taxi as a comma-separated string.
     *
     * @return the position of the taxi
     */
    public String getPosition() {
        return x + "," + y;
    }
}