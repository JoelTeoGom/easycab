package org.example.ec_central.model;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing a city map.
 */
@Data
public class CityMapDto {
    /**
     * The 2D array representing the map.
     */
    private final Cell[][] map;

    /**
     * Enum representing the possible colors of a cell.
     */
    public enum Color {
        RED,
        GREEN,
        YELLOW,
        BLUE,
        WHITE
    }

    /**
     * Record representing a cell in the map.
     */
    public record Cell(Color color, String data) {
    }
}