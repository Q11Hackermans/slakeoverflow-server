package com.github.q11hackermans.slakeoverflow_server.game;

public class Food extends Item {
    private final int foodValue;

    public Food(int posx, int posy, int foodValue) {
        super(posx, posy);
        this.foodValue = foodValue;
    }

    public int getFoodValue() {
        return this.foodValue;
    }

    @Override
    public String getDescription() {
        return "FOOD";
    }
}
