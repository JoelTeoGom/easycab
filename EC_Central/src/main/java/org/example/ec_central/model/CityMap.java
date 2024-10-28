package org.example.ec_central.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * Represents a city map with a fixed size grid.
 */
@Getter
@Setter
public class CityMap {
    /**
     * The size of the city map grid.
     */
    private final int size = 20;
    /**
     * The 2D array representing the city map.
     */
    private final Cell[][] map;

    /**
     * Constructs a CityMap object and initializes the map with default values.
     */
    public CityMap() {
        this.map = new Cell[size][size];
        for (Cell[] row : map) {
            Arrays.fill(row, new Cell(Color.WHITE, ""));
        }
    }

    /**
     * Updates the content at a specific position in the map.
     *
     * @param x     the x-coordinate of the position
     * @param y     the y-coordinate of the position
     * @param data  the content to place at the specified position
     * @param color the color of the cell
     */
    public void updatePosition(int x, int y, String data, Color color) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            map[x][y] = new Cell(color, data);
        }
    }

    /**
     * Retrieves the content at a specific position in the map.
     *
     * @param x the x-coordinate of the position
     * @param y the y-coordinate of the position
     * @return the content at the specified position
     */
    public Cell getPosition(int x, int y) {
        return map[x][y];
    }

    @AllArgsConstructor
    @Getter
    public enum Color {
        RED("stopped"),
        GREEN("moving"),
        YELLOW("customer"),
        BLUE("location"),
        WHITE("empty");

        private final String value;

    }

    public record Cell(Color color, String data) {
    }
}