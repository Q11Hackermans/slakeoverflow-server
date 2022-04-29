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

    /**
     * Returns if the specified field is a food item
     * @param posX Position X
     * @param posY Position Y
     * @return boolean (true = is a food item)
     */
    public boolean hasApple(int posX, int posY) {
        GameObject gameObject = this.getField(posX, posY);
        return (gameObject instanceof Food);
    }

    public int getAppleValue(int posX, int posy) {
        GameObject gameObject = this.getField(posX, posy);
        if(gameObject instanceof Food) {
            return ((Food) gameObject).getFoodValue();
        } else {
            return 0;
        }
    }

    private GameObject getField(int posX, int posY) {
        for(GameObject gameObject : this.gameObjects) {
            if(gameObject.getPosX() == posX && gameObject.getPosY() == posY) {
                return gameObject;
            }
        }
        return null;
    }

    public void kill(Snake snake) {
        this.gameObjects.remove(snake);
    }
}
