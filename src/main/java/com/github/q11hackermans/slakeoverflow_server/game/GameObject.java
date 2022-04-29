package com.github.q11hackermans.slakeoverflow_server.game;

public interface GameObject {
    /**
     * This method should return the X and Y position of the GameObject.
     * The first value int[0] is the X position, the second value int[1] the Y position.
     * @return int[] (length=2)
     */
    int[] getPos();
    int getPosX();
    int getPosY();
}
