package com.github.q11hackermans.slakeoverflow_server.game;

import com.github.q11hackermans.slakeoverflow_server.GameSession;
import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;

/**
 * This class is the same as Food, it does not extend Food because of the instanceof check in GameSession
 */
public class SuperFood extends Item {

    private final int value;

    public SuperFood(GameSession gameSession, int posx, int posy, int value) {
        super(gameSession, posx, posy, gameSession.getServer().getConfigManager().getConfig().getItemSuperFoodDespawnTime());
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
