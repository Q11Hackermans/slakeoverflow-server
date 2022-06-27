package com.github.q11hackermans.slakeoverflow_server.game;

import com.github.q11hackermans.slakeoverflow_server.GameSession;

public class Food extends Item {
    private final int foodValue;

    public Food(GameSession gameSession, int posx, int posy, int foodValue) {
        super(gameSession, posx, posy);
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
