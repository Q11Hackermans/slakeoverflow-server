package com.github.q11hackermans.slakeoverflow_server.game;

/**
 * This class is the same as Food, it does not extend Food because of the instanceof check in GameSession
 */
public class SuperFood extends Item {

    private final int value;

    public SuperFood(int posx, int posy, int value) {
        super(posx, posy);
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public String getDescription() {
        return "SUPERFOOD";
    }
}
