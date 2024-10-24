package org.example.ec_de.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortestPathFinder {

    private int currentX = 0;
    private int currentY = 0;
    private boolean stop = false;

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

        if (currentY == endY && currentX == endX){
            this.stop = true;
        }
        return new int[]{currentX, currentY};
    }


}
