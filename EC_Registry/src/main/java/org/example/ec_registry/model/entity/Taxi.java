package org.example.ec_registry.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.ec_registry.model.TaxiState;

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
     * Default constructor.
     */
    public Taxi() {
    }

    /**
     * Parameterized constructor.
     *
     * @param identifier   The unique identifier for the taxi.
     * @param available    Indicates whether the taxi is available.
     * @param x            The x-coordinate of the taxi's position.
     * @param y            The y-coordinate of the taxi's position.
     * @param destIdentifier The identifier for the taxi's destination.
     * @param state        The state of the taxi.
     */
    public Taxi(String identifier, boolean available, int x, int y, String destIdentifier, TaxiState state) {
        this.identifier = identifier;
        this.available = available;
        this.x = x;
        this.y = y;
        this.destIdentifier = destIdentifier;
        this.state = state;
    }

    /**
     * Returns the position of the taxi as a comma-separated string.
     *
     * @return the position of the taxi
     */
    public String getPosition() {
        return x + "," + y;
    }
}
