package com.github.q11hackermans.slakeoverflow_server.game;

/**
 * This class is the same as Food, it does not extend Food because of the instanceof check in GameSession
 */
public class SuperFood extends Item {
    private int value;

    public SuperFood(int posx, int posy) {
        super(posx, posy);
    }

    public int getSuperFoodValue() {
        return this.value;
    }
}
