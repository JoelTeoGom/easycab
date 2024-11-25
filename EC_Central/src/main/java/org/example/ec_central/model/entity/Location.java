package org.example.ec_central.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity class representing a location.
 */
@Entity
@Getter
@Setter
public class Location {

    /**
     * The unique identifier for the location.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The identifier for the location.
     */
    private String identifier;

    /**
     * The x-coordinate of the location.
     */
    private int x;

    /**
     * The y-coordinate of the location.
     */
    private int y;

    /**
     * Returns the position of the location as a string in the format "x,y".
     *
     * @return the position of the location
     */
    public String getPosition() {
        return x + "," + y;
    }
}