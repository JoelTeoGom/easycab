package org.example.ec_central.model;

import lombok.Getter;

import java.util.Arrays;

/**
 * Represents a city map with a fixed size grid.
 */
@Getter
public class CityMap {

    /**
     * The size of the city map grid.
     */
    private final int size = 20;

    /**
     * The 2D array representing the city map.
     */
    private final String[][] map;

    /**
     * Constructs a CityMap object and initializes the map with default values.
     */
    public CityMap() {
        this.map = new String[size][size];
        for (String[] row : map) {
            Arrays.fill(row, ".");
        }
    }

    /**
     * Updates the content at a specific position in the map.
     *
     * @param x the x-coordinate of the position
     * @param y the y-coordinate of the position
     * @param content the content to place at the specified position
     */
    public void updatePosition(int x, int y, String content) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            map[x][y] = content;
        }
    }

    /**
     * Retrieves the content at a specific position in the map.
     *
     * @param x the x-coordinate of the position
     * @param y the y-coordinate of the position
     * @return the content at the specified position
     */
    public String getPosition(int x, int y) {
        return map[x][y];
    }

    /**
     * Returns a string representation of the city map.
     *
     * @return a string representation of the city map
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String[] row : map) {
            for (String cell : row) {
                sb.append(cell).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}