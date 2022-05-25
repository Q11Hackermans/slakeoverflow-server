package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.constants.AuthenticationState;
import com.github.q11hackermans.slakeoverflow_server.constants.Direction;
import com.github.q11hackermans.slakeoverflow_server.constants.FieldState;
import com.github.q11hackermans.slakeoverflow_server.data.SnakeData;
import com.github.q11hackermans.slakeoverflow_server.game.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameSession {
    private final List<Snake> snakeList;
    private final List<Item> itemList;
    private final int borderX;
    private final int borderY;
    private final int fovsizeX;
    private final int fovsizeY;
    private int nextItemDespawn;

    public GameSession(int x, int y) {
        this(x, y, 30, 20, 20, null, null);
    }

    public GameSession(int x, int y, int fovsizeX, int fovsizeY, int nextItemDespawn, List<SnakeData> snakeDataList, List<Item> itemList) {
        this.snakeList = new ArrayList<>();
        this.itemList = new ArrayList<>();

        this.borderX = x;
        this.borderY = y;

        this.fovsizeX = fovsizeX;
        this.fovsizeY = fovsizeY;

        this.nextItemDespawn = nextItemDespawn;

        if(snakeDataList != null) {
            for(SnakeData snakeData : snakeDataList) {
                this.snakeList.add(new Snake(snakeData.getConnection(), snakeData.getPosx(), snakeData.getPosy(), snakeData.getFacing(), snakeData.getBodyPositions(), this));
            }
        }
        if(itemList != null) {
            this.itemList.addAll(itemList);
        }
    }

    // TICK
    public void tick() {
        if(SlakeoverflowServer.getServer().getGameSession() == this) {
            // CHECK IF SNAKE IS ALIVE
            this.checkSnakes();

            // RUNNING TICK ON SNAKES
            for(Snake snake : this.snakeList) {
                snake.tick();
            }
            this.spawnFood(this.calcFoodSpawnTries());

            // SENDING PLAYERDATA TO SNAKES
            for(Snake snake : this.snakeList) {
                if(snake.getConnection() != null) {
                    snake.getConnection().sendUTF(this.getSendablePlayerData(snake, true));
                }
            }

            if(this.nextItemDespawn > 0) {
                this.nextItemDespawn--;
            } else {
                List<Item> itemListCopy = List.copyOf(this.itemList);
                for(Item item : itemListCopy) {
                    if(item.getDespawnTime() > 0) {
                        item.despawnCount();
                    } else {
                        this.killItem(this.itemList.indexOf(item));
                    }
                }
            }

            // ADD NEW SNAKES
            this.addNewSnakes();
        }
    }

    // ITEM MANAGEMENT

    /**
     * Return a number to use in the spawnFood function depending on the player-count
     */
    private int calcFoodSpawnTries() {
        if (randomIntInRange(1, 3) == 1) {
            return (int) (1 + Math.round(0.2 * snakeList.size()));
        }
        return 0;
    }

    /**
     * Tries X-times to spawn food with the value of 1-3 if the field is free
     * @param tries Times the Server tries to spawn food
     */
    private void spawnFood(int tries){
        for (int i = tries; i > 0; i--) {
            int[] rPos = this.posInRandomPlayerFov();
            int posX = rPos[0];
            int posY = rPos[1];

            if (posX > -1 && posY > -1 && isFree(posX, posY)) {
                synchronized (this.itemList) {
                    this.itemList.add(new Food(posX, posY, new Random().nextInt(SlakeoverflowServer.getServer().getConfigManager().getConfig().getMaxFoodValue() - SlakeoverflowServer.getServer().getConfigManager().getConfig().getMinFoodValue()) + SlakeoverflowServer.getServer().getConfigManager().getConfig().getMinFoodValue()));
                }
            }
        }
    }

    /**
     * Tries to spawn a SuperFood with this value at this field
     *
     * @param value Value of the super food to be spawned
     * @param posX  Position X where the super-food is spawned
     * @param posY  Position Y where the super-food is spawned
     */
    public void spawnSuperFoodAt(int value, int posX, int posY) {
        if(isFree(posX, posY)) {
            synchronized(this.itemList) {
                this.itemList.add(new SuperFood(posX, posY, value));
            }
        }
    }

    public void killItem(int index) {
        synchronized(this.itemList) {
            try {
                this.itemList.remove(index);
            } catch(IndexOutOfBoundsException ignored) {}
        }
    }

    public void killItems() {
        synchronized(this.itemList) {
            this.itemList.clear();
        }
    }

    // UTILITY

    /**
     * Returns a random number on the fields x-axis
     *
     * @return Random x coordinate
     */
    private int randomPosX() {
        return (int) ((Math.random() * ((this.borderX - 1) - 1)) + 1);
    }

    /**
     * Returns a random number on the fields y-axis
     * @return Random y coordinate
     */
    private int randomPosY(){
        return (int) ((Math.random() * ((this.borderY - 1) - 1)) + 1);
    }

    private void checkSnakes() {
        this.snakeList.removeIf(snake -> !snake.isAlive());
    }

    private void addNewSnakes() {
        for(ServerConnection connection : SlakeoverflowServer.getServer().getConnectionList()) {
            if(connection.isConnected() && connection.getAuthenticationState() == AuthenticationState.PLAYER && this.getSnakeOfConnection(connection) == null) {
                int posX = this.randomPosX();
                int posY = this.randomPosY();

                int length = SlakeoverflowServer.getServer().getConfigManager().getConfig().getDefaultSnakeLength();

                int count = 3;
                boolean success = false;
                while(count > 0) {
                    if(this.isAreaFree(posX, posY, length*2)) {
                        this.snakeList.add(new Snake(connection, posX, posY, Direction.NORTH, length, this));
                        count = 0;
                        success = true;
                        break;
                    } else {
                        count --;
                    }
                }

                if(!success) {
                    connection.unauthenticate();
                }
            }
        }
    }

    /**
     * This method returns the final String (in JSONObject format) which is ready for sending to the player.
     * @param snake The snake
     * @return String (in JSONObject format)
     */
    private String getSendablePlayerData(Snake snake, boolean relative) {
        JSONObject playerData = new JSONObject();
        playerData.put("cmd", "playerdata");
        playerData.put("fovx", this.fovsizeX);
        playerData.put("fovy", this.fovsizeY);

        JSONArray fields = new JSONArray();

        int minY = snake.getPosY() - this.fovsizeY;
        int maxY = snake.getPosY() + this.fovsizeY;

        if(minY < 0) {
            minY = 0;
        }
        if(maxY > this.borderY) {
            maxY = this.borderY;
        }

        int relativey = 0;
        for(int iy = minY; iy <= maxY; iy++) {
            int yvalue;
            if(relative) {
                yvalue = relativey;
            } else {
                yvalue = iy;
            }

            int minX = snake.getPosX() - this.fovsizeX;
            int maxX = snake.getPosX() + this.fovsizeX;

            if(minX < 0) {
                minX = 0;
            }
            if(maxX > this.borderX) {
                maxX = this.borderX;
            }

            int relativex = 0;
            for(int ix = minX; ix <= maxX; ix++) {
                int xvalue;
                if(relative) {
                    xvalue = relativex;
                } else {
                    xvalue = ix;
                }

                if(ix == 0) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else if(ix == this.borderX) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else if(iy == 0) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else if(iy == this.borderY) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else {
                    GameObject field = this.getField(ix, iy);
                    if(field instanceof Snake) {
                        if(field == snake) {
                            if(Arrays.equals(field.getPos(), new int[]{ix, iy})) {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.getPlayerHeadOwnValue(snake.getFacing())));
                            } else {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.PLAYER_BODY_OWN));
                            }
                        } else {
                            if(Arrays.equals(field.getPos(), new int[]{ix, iy})) {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.getPlayerHeadOtherValue(snake.getFacing())));
                            } else {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.PLAYER_BODY_OTHER));
                            }
                        }
                    } else if(field instanceof Item) {
                        if(field instanceof Food) {
                            fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.ITEM_FOOD));
                        } else if(field instanceof SuperFood) {
                            fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.ITEM_SUPER_FOOD));
                        } else {
                            // DO NOTHING
                            //fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.ITEM_UNKNOWN));
                        }
                    } else {
                        // DO NOTHING
                        //fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.EMPTY));
                    }
                }

                relativex++;
            }

            relativey++;
        }

        playerData.put("fields", fields);
        playerData.put("relative", relative);

        return playerData.toString();
    }

    /**
     * Creates a JSONArray [0,0,0,0]
     * 1. Value: X-Coordinates
     * 2. Value: Y-Coordinates
     * 3. Value: FieldState
     * @param x X-Coordinates
     * @param y Y-Coordinates
     * @param fieldState FieldState
     * @return JSONArray
     */
    private JSONArray createCoordsJSONArray(int x, int y, int fieldState) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(x);
        jsonArray.put(y);
        jsonArray.put(fieldState);
        return jsonArray;
    }

    // FIELD MANAGEMENT

    /**
     * Returns true if the specified field is free.
     * This also includes outside fields and the worldborder.
     * @param posX Position X
     * @param posY Position Y
     * @return boolean
     */
    public boolean isFree(int posX, int posY) {
        return (this.getField(posX, posY) == null) && (!this.isOutside(posX, posY));
    }

    /**
     * Returns an integer array with a position within a random players FOV or [-1|-1] if there is no free field within the FOV or no player.
     *
     * @return int[]
     */
    private int[] posInRandomPlayerFov() {
        if (snakeList.size() > 0) {
            Snake rSnake = snakeList.get(randomIntInRange(0, snakeList.size() - 1));
            int[] randomSnakePos = new int[]{rSnake.getPosX(), rSnake.getPosY()};

            for (int i = 0; i < 20; i++) {
                int rPosX = this.randomIntInRange(randomSnakePos[0] - Math.round(this.fovsizeX / 2), randomSnakePos[0] + Math.round(this.fovsizeX / 2));
                int rPosY = this.randomIntInRange(randomSnakePos[1] - Math.round(this.fovsizeY / 2), randomSnakePos[1] + Math.round(this.fovsizeY / 2));
                if (isFree(rPosX, rPosY)) {
                    return new int[]{rPosX, rPosY};
                }
            }
        }
        return new int[]{-1, -1};
    }

    /**
     * Returns if a specific area is free.
     * The x and y coordinates are the center field coordinates.
     *
     * @param x    center field x
     * @param y    center field y
     * @param area area
     * @return boolean
     */
    public boolean isAreaFree(int x, int y, int area) {
        if(x < 0 || x >= this.borderX) {
            return false;
        }
        if(y < 0 || y >= this.borderY) {
            return false;
        }

        int left = x - Math.round(area/2);
        int top = y - Math.round(area/2);

        if(left < 0) {
            left = 0;
        }
        if(top < 0) {
            top = 0;
        }

        int right = x + Math.round(area/2);
        int bottom = y + Math.round(area/2);

        if(right >= this.borderX) {
            right = this.borderX - 1;
        }
        if(bottom >= this.borderY) {
            bottom = this.borderY - 1;
        }

        for(int iy = top; iy < bottom; iy++) {
            for(int ix = left; ix < right; ix++) {
                if(!this.isFree(ix, iy)) {
                    return false;
                }
            }
        }
        return true;
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
     *
     * @param posX Position X
     * @param posy Position Y
     * @return int (!=0 when food, =0 when no food)
     */
    public int getFoodValue(int posX, int posy) {
        GameObject gameObject = this.getField(posX, posy);
        if (gameObject instanceof Food) {
            return ((Food) gameObject).getFoodValue();
        } else if (gameObject instanceof SuperFood) {
            return ((SuperFood) gameObject).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Returns the GameObject that is on the specified field.
     * If the specified field is empty, this will return null.
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
        synchronized(this.itemList) {
            for (Item item : this.itemList) {
                if (item.getPosX() == posX && item.getPosY() == posY) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a field is on the world border
     * @param posX X Coordinates
     * @param posY Y Coordinates
     * @return true if the field is on the worldborder
     */
    public boolean isBorder(int posX, int posY) {
        if(posX == 0 || posX == this.borderX || posY == 0 || posY == this.borderY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a field is outside (world border included) of the world border
     * @param posX X Coordinates
     * @param posY Y Coordinates
     * @return true (if outside)
     */
    public boolean isOutside(int posX, int posY) {
        if(posX <= 0 || posX >= this.borderX || posY <= 0 || posY >= this.borderY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a random int from 1 to the upper bound
     *
     * @param upperBound upper bound of the random int
     * @return int
     */
    private int randomIntInRange(int lowerBound, int upperBound) throws IllegalArgumentException {

        if (lowerBound >= upperBound) {
            // IllegalArgumentException e = new IllegalArgumentException("The upper bound is smaller than the lower bound");
            // SlakeoverflowServer.getServer().getLogger().debug("RANDOM-INT-GENERATOR", "Exception: " + e);
            upperBound = lowerBound;
        }
        return new Random().nextInt((upperBound - lowerBound) + 1) + lowerBound;
    }

    /**
     * Get the world border
     * The world border is from X: 0-getBorder()[0], Y: 0-getBorder()[1].
     *
     * @return int[]{posX, posY}
     */
    public int[] getBorder() {
        return new int[]{this.borderX, this.borderY};
    }

    public int[] getPlayerFOV() {
        return new int[]{this.fovsizeX, this.fovsizeY};
    }

    // SNAKE MANAGEMENT

    /**
     * Returns the snake of a specific player
     * @param connection Player
     * @return Snake
     */
    public Snake getSnakeOfConnection(ServerConnection connection) {
        for(Snake snake : this.snakeList) {
            if(snake.getConnection() == connection) {
                return snake;
            }
        }
        return null;
    }

    /**
     * Returns the snake with a specific list index
     * @param index List index (snake id)
     * @return Snake (if it is available, else null)
     */
    public Snake getSnake(int index) {
        try {
            return this.snakeList.get(index);
        } catch(IndexOutOfBoundsException ignored) {}
        return null;
    }

    public int getSnakeId(Snake snake) {
        if(this.snakeList.contains(snake)) {
            return this.snakeList.indexOf(snake);
        }
        return -1;
    }

    /**
     * Get a copy of the snake list.
     * @return copy of snake list
     */
    public List<Snake> getSnakeList() {
        return List.copyOf(this.snakeList);
    }

    /**
     * Get a copy of the item list.
     * @return copy of item list
     */
    public List<Item> getItemList() {
        return List.copyOf(this.itemList);
    }

    /**
     * Return the entire game as a JSONObject with all game data in it
     * @return JSONObject (savegame)
     */
    public JSONObject getSaveString() {
        JSONObject savegame = new JSONObject();

        // SAVE GAME SETTINGS
        JSONObject settings = new JSONObject();
        settings.put("border_x", this.borderX);
        settings.put("border_y", this.borderY);
        settings.put("fovsize_x", this.fovsizeX);
        settings.put("fovsize_y", this.fovsizeY);
        settings.put("next_item_despawn", this.nextItemDespawn);
        savegame.put("settings", settings);

        // SAVE SNAKES
        JSONArray snakes = new JSONArray();
        for(Snake snake : this.snakeList) {
            JSONObject snakeData = new JSONObject();
            snakeData.put("id", this.getSnakeId(snake));
            snakeData.put("x", snake.getPosX());
            snakeData.put("y" , snake.getPosY());
            snakeData.put("facing", snake.getFacing());
            snakeData.put("movein", snake.getMoveIn());

            JSONArray bodyPositions = new JSONArray();
            for(int[] body : snake.getBodyPositions()) {
                JSONArray bodyArray = new JSONArray();
                bodyArray.put(body[0]);
                bodyArray.put(body[1]);

                bodyPositions.put(bodyArray);
            }

            snakeData.put("bodies", bodyPositions);

            snakes.put(snakeData);
        }
        savegame.put("snakes", snakes);

        // SAVE ITEMS
        JSONArray items = new JSONArray();
        for(Item item : this.itemList) {
            JSONObject itemData = new JSONObject();

            itemData.put("x", item.getPosX());
            itemData.put("y", item.getPosY());
            itemData.put("description", item.getDescription());
            itemData.put("despawnTime", item.getDespawnTime());

            if(item instanceof Food) {
                itemData.put("food_value", ((Food) item).getFoodValue());
            } else if(item instanceof SuperFood) {
                itemData.put("superfood_value", ((SuperFood) item).getValue());
            }

            items.put(itemData);
        }
        savegame.put("items", items);

        return savegame;
    }
}
