package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.connections.Player;
import com.github.q11hackermans.slakeoverflow_server.constants.FieldState;
import com.github.q11hackermans.slakeoverflow_server.game.Food;
import com.github.q11hackermans.slakeoverflow_server.game.GameObject;
import com.github.q11hackermans.slakeoverflow_server.game.Item;
import com.github.q11hackermans.slakeoverflow_server.game.Snake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.lang.Math.sqrt;

public class GameSession {
    private final List<Snake> snakeList;
    private final List<Item> itemList;
    private final int borderX;
    private final int borderY;
    private final int fovsizeX;
    private final int fovsizeY;

    public GameSession(int x, int y) {
        this.snakeList = new ArrayList<>();
        this.itemList = new ArrayList<>();
        this.borderX = x;
        this.borderY = y;
        this.fovsizeX = 50;
        this.fovsizeY = 50;
    }

    // TICK
    public void tick() {
        // RUNNING TICK ON SNAKES
        for(Snake snake : this.snakeList) {
            snake.tick();
        }
        this.spawnFood((int) sqrt(0.5*snakeList.size()));

        // SENDING PLAYERDATA TO SNAKES
        for(Snake snake : this.snakeList) {
            try {
                snake.getPlayer().getDataIOStreamHandler().writeUTF(this.getSendablePlayerData(snake));
            } catch(Exception e) {
                // THIS WILL BE FILLED SOON (i guess...)
            }
        }
    }

    /**
     * Tries to spawn the amount of food with the value of 1 if the fields are free
     * @param count Amount of food to be spawned
     */
    private void spawnFood(int count){
        for (int i = count; i > 0; i--){
            int posX = this.randomPosX();
            int posY = this.randomPosY();

            if(isFree(posX, posY)){
                Food food = new Food(posX, posY,1);
                this.itemList.add(food);
            }

        }
    }

    /**
     * Returns a random number on the fields x-axis
     * @return Random x coordinate
     */
    private int randomPosX(){
        return (int) ((Math.random() * ((this.borderX - 1) - 1)) + 1);
    }

    /**
     * Returns a random number on the fields y-axis
     * @return Random y coordinate
     */
    private int randomPosY(){
        return (int) ((Math.random() * ((this.borderY - 1) - 1)) + 1);
    }

    /**
     * This method returns the final String (in JSONObject format) which is ready for sending to the player.
     * @param snake The snake
     * @return String (in JSONObject format)
     */
    private String getSendablePlayerData(Snake snake) {
        JSONObject playerData = new JSONObject();
        playerData.put("cmd", "playerdata");

        JSONArray fields = new JSONArray();

        int minY = snake.getPosY() - this.fovsizeY;
        int maxY = snake.getPosY() + this.fovsizeY;

        if(minY < 0) {
            minY = 0;
        }
        if(maxY > this.borderY) {
            maxY = this.borderY;
        }

        for(int iy = minY; iy < maxY; iy++) {
            int minX = snake.getPosX() - this.fovsizeX;
            int maxX = snake.getPosX() + this.fovsizeX;

            if(minX < 0) {
                minX = 0;
            }
            if(maxX > this.borderX) {
                maxX = this.borderX;
            }

            JSONArray fieldsx = new JSONArray();

            for(int ix = minX; ix < maxX; ix++) {
                GameObject field = this.getField(ix, iy);
                if(field instanceof Snake) {
                    if(field == snake) {
                        if(Arrays.equals(field.getPos(), new int[]{ix, iy})) {
                            fieldsx.put(FieldState.PLAYER_HEAD_OWN);
                        } else {
                            fieldsx.put(FieldState.PLAYER_BODY_OWN);
                        }
                    } else {
                        if(Arrays.equals(field.getPos(), new int[]{ix, iy})) {
                            fieldsx.put(FieldState.PLAYER_HEAD_OTHER);
                        } else {
                            fieldsx.put(FieldState.PLAYER_BODY_OTHER);
                        }
                    }
                } else if(field instanceof Item) {
                    if(field instanceof Food) {
                        fieldsx.put(FieldState.ITEM_APPLE);
                    } else {
                        fieldsx.put(FieldState.ITEM_UNKNOWN);
                    }
                } else {
                    fieldsx.put(FieldState.EMPTY);
                }
            }

            fields.put(fieldsx);
        }

        playerData.put("fields", fields);

        return playerData.toString();
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
     * Returns true if the specified field is not another player
     * @param posX Position X
     * @param posY Position Y
     * @return boolean
     */
    public boolean isOtherPlayerFree(int posX, int posY, Snake snake) {
        GameObject fieldObject = this.getField(posX,posY);
        return (!(fieldObject instanceof Snake) || fieldObject == snake);
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
        for(Snake snake : this.snakeList) {
            if(snake.getPosX() == posX && snake.getPosY() == posY) {
                return snake;
            } else {
                for(int[] pos : snake.getBodyPositions()) {
                    if(pos[0] == posX && pos[1] == posY) {
                        return snake;
                    }
                }
            }
        }
        for(Item item : this.itemList) {
            if(item.getPosX() == posX && item.getPosY() == posY) {
                return item;
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
        return new int[]{this.borderX, this.borderY};
    }

    // SNAKE MANAGEMENT
    /**
     * This method will kill a snake.
     * This will remove the snake from the list.
     * @param snake
     */
    public void killSnake(Snake snake) {
        this.snakeList.remove(snake);
    }

    /**
     * Returns the snake of a specific player
     * @param player Player
     * @return Snake
     */
    public Snake getSnakeOfPlayer(Player player) {
        for(Snake snake : this.snakeList) {
            if(snake.getPlayer() == player) {
                return snake;
            }
        }
        return null;
    }
}
