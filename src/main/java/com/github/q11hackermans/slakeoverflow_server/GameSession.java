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

    // FIELD MANAGEMENT
    /**
     * Returns true if the specified field is free
     * @param posX Position X
     * @param posY Position Y
     * @return boolean
     */
    public boolean isFree(int posX, int posY) {
        return this.getField(posX, posY) == null;
    }

    /**
     * Returns true if the specified field is not a player
     * @param posX Position X
     * @param posY Position Y
     * @return boolean
     */
    public boolean isPlayerFree(int posX, int posY) {
        return !(this.getField(posX, posY) instanceof Snake);
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

    /**
     * Returns the food value if food is on the specified field.
     * Returns 0 if there is no food on the specified field.
     * @param posX Position X
     * @param posy Position Y
     * @return int (!=0 when food, =0 when no food)
     */
    public int getAppleValue(int posX, int posy) {
        GameObject gameObject = this.getField(posX, posy);
        if(gameObject instanceof Food) {
            return ((Food) gameObject).getFoodValue();
        } else {
            return 0;
        }
    }

    /**
     * Returns the GameObject that is on the specified field.
     * If the specified field is empty, this will return 0.
     * @param posX Position X
     * @param posY Position Y
     * @return GameObject
     */
    public GameObject getField(int posX, int posY) {
        for(GameObject gameObject : this.gameObjects) {
            if(gameObject.getPosX() == posX && gameObject.getPosY() == posY) {
                return gameObject;
            }
        }
        return null;
    }

    /**
     * Get the world border
     * The world border is from X: 0-getBorder()[0], Y: 0-getBorder()[1].
     * @return int[]{posX, posY}
     */
    public int[] getBorder() {
        return new int[]{this.borderx, this.bordery};
    }

    // SNAKE MANAGEMENT
    /**
     * This method will kill a snake.
     * This will remove the snake from the list.
     * @param snake
     */
    public void killSnake(Snake snake) {
        this.gameObjects.remove(snake);
    }
}
