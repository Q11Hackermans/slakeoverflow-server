package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.game.Food;
import com.github.q11hackermans.slakeoverflow_server.game.GameObject;
import com.github.q11hackermans.slakeoverflow_server.game.Snake;

import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private final List<GameObject> gameObjects;
    private final int borderx;
    private final int bordery;

    public GameSession(int x, int y){
        this.gameObjects = new ArrayList<>();
        this.borderx = x;
        this.bordery = y;
    }

    public int[] getBorder() {
        return new int[]{this.borderx, this.bordery};
    }

    /**
     * Returns true if the specified field is free
     * @param posX Position X
     * @param posY Position Y
     * @return
     */
    public boolean isFree(int posX, int posY) {
        return this.getField(posX, posY) == null;
    }
}
