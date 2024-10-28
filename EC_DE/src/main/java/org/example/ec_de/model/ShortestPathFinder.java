package org.example.ec_de.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Class for finding the shortest path for a taxi.
 */
@Getter
@Setter
public class ShortestPathFinder {

    /**
     * The current X coordinate of the taxi.
     */
    private int currentX = 0;

    /**
     * The current Y coordinate of the taxi.
     */
    private int currentY = 0;

    /**
     * Flag indicating whether the taxi should stop.
     */
    private boolean stop = false;

    /**
     * The current state of the taxi.
     */
    private TaxiState taxiState = TaxiState.ASSIGNED;

    /**
     * Calculates the next position of the taxi based on the end coordinates.
     *
     * @param endX the X coordinate of the destination
     * @param endY the Y coordinate of the destination
     * @return an array containing the next X and Y coordinates of the taxi
     */
    public int[] getNextPosition(int endX, int endY) {

        if (endX < currentX) {
            this.currentX = currentX - 1;
        } else if (endX > currentX) {
            this.currentX = currentX + 1;
        }

        if (endY < currentY) {
            this.currentY = currentY - 1;
        } else if (endY > currentY) {
            this.currentY = currentY + 1;
        }

        if (currentY == endY && currentX == endX) {
            this.stop = true;
        }
        return new int[]{currentX, currentY};
    }
}