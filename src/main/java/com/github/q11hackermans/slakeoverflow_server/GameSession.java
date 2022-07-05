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

    private final SlakeoverflowServer server;
    private final List<Snake> snakeList;
    private final List<Item> itemList;
    private final int borderX;
    private final int borderY;
    private final int fovsizeX;
    private final int fovsizeY;
    private int nextItemDespawn;
    private int nextSpectatorUpdate;

    public GameSession(SlakeoverflowServer server, int x, int y) {
        this(server, x, y, 60, 40, 20, null, null);
    }

    public GameSession(SlakeoverflowServer server, int x, int y, int fovsizeX, int fovsizeY, int nextItemDespawn, List<SnakeData> snakeDataList, List<Item> itemList) {
        this.server = server;

        this.snakeList = new ArrayList<>();
        this.itemList = new ArrayList<>();

        this.borderX = x;
        this.borderY = y;

        this.fovsizeX = fovsizeX;
        this.fovsizeY = fovsizeY;

        this.nextItemDespawn = nextItemDespawn;
        this.nextSpectatorUpdate = 200;

        if (snakeDataList != null) {
            for (SnakeData snakeData : snakeDataList) {
                this.snakeList.add(new Snake(this, snakeData.getConnection(), snakeData.getPosx(), snakeData.getPosy(), snakeData.getFacing(), snakeData.getBodyPositions()));
            }
        }
        if (itemList != null) {
            this.itemList.addAll(itemList);
        }
    }

    // TICK
    public void tick() {
        if (this.server.getGameSession() == this) {
            // CHECK IF SNAKE IS ALIVE
            this.checkSnakes();

            // TICK 1
            this.onTick1();

            // RUNNING TICK ON SNAKES
            for (Snake snake : this.snakeList) {
                snake.tick();
            }
            this.spawnFood(this.calcFoodSpawnTries());

            // TICK 2
            this.onTick2();

            // SENDING PLAYERDATA TO SNAKES
            for (Snake snake : this.snakeList) {
                if (snake.getConnection() != null && snake.getConnection().getAuthenticationState() == AuthenticationState.PLAYER) {
                    if(snake.isFixedFovPlayerdataSystem()) {
                        snake.getConnection().sendUTF(this.getSendablePlayerdataFixedFov2(snake));
                    } else {
                        snake.getConnection().sendUTF(this.getSendablePlayerData(snake, true));
                    }
                }
            }

            if (this.nextItemDespawn > 0) {
                this.nextItemDespawn--;
            } else {
                this.nextItemDespawn = 20;

                List<Item> itemListCopy = List.copyOf(this.itemList);
                for (Item item : itemListCopy) {
                    if (item.getDespawnTime() > 0) {
                        item.despawnCount();
                    } else {
                        this.killItem(this.itemList.indexOf(item));
                    }
                }
            }

            // TICK 3
            this.onTick3();

            if(this.server.getConfigManager().getConfig().isEnableSpectator()) {
                if(this.nextSpectatorUpdate <= 0) {
                    this.nextSpectatorUpdate = this.server.getConfigManager().getConfig().getSpectatorUpdateInterval();

                    String spectatorData = this.getSendableSpectatorData();

                    for(ServerConnection connection : this.server.getConnectionList()) {
                        if(connection.getAuthenticationState() == AuthenticationState.SPECTATOR) {
                            connection.sendUTF(spectatorData);
                        }
                    }
                } else {
                    this.nextSpectatorUpdate--;
                }
            }

            // TICK 4
            this.onTick4();

            // ADD NEW SNAKES
            this.addNewSnakes();
        }
    }

    // ITEM MANAGEMENT

    /**
     * Return a number to use in the spawnFood function depending on the player-count
     */
    private int calcFoodSpawnTries() {
        if (randomIntInRange(1, 50) == 1) {
            return (int) (1 + Math.round(0.2 * snakeList.size()));
        }
        return 0;
    }

    /**
     * Tries X-times to spawn food with the value of 1-3 if the field is free
     *
     * @param tries Times the Server tries to spawn food
     */
    private void spawnFood(int tries) {
        for (int i = tries; i > 0; i--) {
            int[] rPos = this.posInRandomPlayerFov();
            int posX = rPos[0];
            int posY = rPos[1];

            if (posX > -1 && posY > -1 && isFree(posX, posY)) {
                synchronized (this.itemList) {
                    this.itemList.add(new Food(this, posX, posY, new Random().nextInt(this.server.getConfigManager().getConfig().getMaxFoodValue() - this.server.getConfigManager().getConfig().getMinFoodValue()) + this.server.getConfigManager().getConfig().getMinFoodValue()));
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
        if (isFree(posX, posY)) {
            synchronized (this.itemList) {
                this.itemList.add(new SuperFood(this, posX, posY, value));
            }
        }
    }

    public void killItem(int index) {
        synchronized (this.itemList) {
            try {
                this.itemList.remove(index);
            } catch (IndexOutOfBoundsException ignored) {}
        }
    }

    public void killItem(Item item) {
        synchronized (this.itemList) {
            try {
                this.itemList.remove(item);
            } catch (IndexOutOfBoundsException ignored) {}
        }
    }

    public void killItems() {
        synchronized (this.itemList) {
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
     *
     * @return Random y coordinate
     */
    private int randomPosY() {
        return (int) ((Math.random() * ((this.borderY - 1) - 1)) + 1);
    }

    private void checkSnakes() {
        this.snakeList.removeIf(snake -> !snake.isAlive());
    }

    private void addNewSnakes() {
        for (ServerConnection connection : this.server.getConnectionList()) {
            if (connection.isConnected() && connection.getAuthenticationState() == AuthenticationState.PLAYER && this.getSnakeOfConnection(connection) == null) {
                int posX = this.randomPosX();
                int posY = this.randomPosY();

                int length = this.server.getConfigManager().getConfig().getDefaultSnakeLength();

                int count = 3;
                boolean success = false;
                while (count > 0) {
                    if (this.isAreaFree(posX, posY, length * 2)) {
                        this.snakeList.add(new Snake(this, connection, posX, posY, Direction.NORTH, length));
                        count = 0;
                        success = true;
                        break;
                    } else {
                        count--;
                    }
                }

                if (!success) {
                    connection.unauthenticate();
                }
            }
        }
    }

    /**
     * This method returns the final String (in JSONObject format) which is ready for sending to the player.
     *
     * @param snake The snake
     * @return String (in JSONObject format)
     */
    private String getSendablePlayerData(Snake snake, boolean relative) {
        JSONObject playerData = new JSONObject();
        playerData.put("cmd", "playerdata");
        playerData.put("fovx", this.fovsizeX/2);
        playerData.put("fovy", this.fovsizeY/2);

        JSONArray fields = new JSONArray();

        int minY = snake.getPosY() - (this.fovsizeY/2);
        int maxY = snake.getPosY() + (this.fovsizeY/2);

        if (minY < 0) {
            minY = 0;
        }
        if (maxY > this.borderY) {
            maxY = this.borderY;
        }

        int minX = snake.getPosX() - (this.fovsizeX/2);
        int maxX = snake.getPosX() + (this.fovsizeX/2);

        if (minX < 0) {
            minX = 0;
        }
        if (maxX > this.borderX) {
            maxX = this.borderX;
        }

        /*
        int relativey = 0;
        for (int iy = minY; iy <= maxY; iy++) {
            int yvalue;
            if (relative) {
                yvalue = relativey;
            } else {
                yvalue = iy;
            }

            int minX = snake.getPosX() - (this.fovsizeX/2);
            int maxX = snake.getPosX() + (this.fovsizeX/2);

            if (minX < 0) {
                minX = 0;
            }
            if (maxX > this.borderX) {
                maxX = this.borderX;
            }

            int relativex = 0;
            for (int ix = minX; ix <= maxX; ix++) {
                int xvalue;
                if (relative) {
                    xvalue = relativex;
                } else {
                    xvalue = ix;
                }

                if (ix == 0) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else if (ix == this.borderX) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else if (iy == 0) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else if (iy == this.borderY) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else {
                    GameObject field = this.getField(ix, iy);
                    if (field instanceof Snake) {
                        Snake fieldSnake = (Snake) field;
                        if (field == snake) {
                            if (Arrays.equals(field.getPos(), new int[]{ix, iy})) {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.getPlayerHeadOwnValue(fieldSnake.getFacing()), fieldSnake.isHasMoved()));
                            } else {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.PLAYER_BODY_OWN));
                            }
                        } else {
                            if (Arrays.equals(field.getPos(), new int[]{ix, iy})) {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.getPlayerHeadOtherValue(fieldSnake.getFacing())));
                            } else {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.PLAYER_BODY_OTHER));
                            }
                        }
                    } else if (field instanceof Item) {
                        if (field instanceof Food) {
                            fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.ITEM_FOOD));
                        } else if (field instanceof SuperFood) {
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

         */

        playerData.put("fields", this.getFields(minY, maxY, minX, maxX, true, snake));
        playerData.put("relative", relative);
        playerData.put("fixed_fov", false);

        return playerData.toString();
    }

    private String getSendablePlayerdataFixedFOV(Snake snake) {
        JSONObject playerData = new JSONObject();
        playerData.put("cmd", "playerdata");
        playerData.put("fovx", this.fovsizeX);
        playerData.put("fovy", this.fovsizeY);

        int fovCountX = this.borderX / this.fovsizeX + 1;
        int fovCountY = this.borderY / this.fovsizeY + 1;

        int fovX = -1;
        int currentFovPos1X = -1;
        int currentFovPos2X = -1;

        for(int i = 0; i < fovCountX; i++) {
            int fovPos1 = this.fovsizeX * i;
            int fovPos2 = fovPos1 + this.fovsizeX;

            if(snake.getPosX() >= fovPos1 && snake.getPosX() < fovPos2) {
                fovX = i;
                currentFovPos1X = fovPos1;
                currentFovPos2X = fovPos2;
                break;
            }
        }

        if(currentFovPos1X < 0 || currentFovPos2X < 0) {
            return playerData.toString();
        }

        int fovY = -1;
        int currentFovPos1Y = -1;
        int currentFovPos2Y = -1;

        for(int i = 0; i < fovCountY; i++) {
            int fovPos1 = this.fovsizeY * i;
            int fovPos2 = fovPos1 + this.fovsizeY;

            if(snake.getPosY() >= fovPos1 && snake.getPosY() < fovPos2) {
                fovY = i;
                currentFovPos1Y = fovPos1;
                currentFovPos2Y = fovPos2;
                break;
            }
        }

        if(currentFovPos1Y < 0 || currentFovPos2Y < 0) {
            return playerData.toString();
        }

        int minY = currentFovPos1Y;
        int maxY = currentFovPos2Y;

        if(minY < 0) {
            minY = 0;
        }

        if(maxY > this.borderY) {
            maxY = this.borderY;
        }

        int minX = currentFovPos1X;
        int maxX = currentFovPos2X;

        if(minX < 0) {
            minX = 0;
        }

        if(maxX > this.borderX) {
            maxX = this.borderX;
        }

        playerData.put("fields", this.getFields(minY, maxY, minX, maxX, true, snake));
        playerData.put("relative", true);
        playerData.put("fixed_fov", true);

        return playerData.toString();
    }

    private String getSendablePlayerdataFixedFov2(Snake snake) {
        JSONObject playerData = new JSONObject();
        playerData.put("cmd", "playerdata");
        playerData.put("fovx", this.fovsizeX);
        playerData.put("fovy", this.fovsizeY);

        int fovCountX = (this.borderX / this.fovsizeX + 1) * 2;
        int fovCountY = (this.borderY / this.fovsizeY + 1) * 2;

        int fovX = -1;
        int snakeFovPos1X = -1;
        int snakeFovPos2X = -1;

        for(int i = 0; i < fovCountX; i++) {
            int fovPos1 = (this.fovsizeX / 2) * i;
            int fovPos2 = fovPos1 + this.fovsizeX;

            int transitionFovBeforePos1 = fovPos1 - (this.fovsizeX / 2);
            int transitionFovBeforePos2 = transitionFovBeforePos1 + this.fovsizeX;

            int transitionFovAfterPos1 = fovPos1 + (this.fovsizeX / 2);
            int transitionFovAfterPos2 = transitionFovAfterPos1 + this.fovsizeX;

            boolean fovCondition = (snake.getPosX() >= fovPos1 && snake.getPosX() < fovPos2);
            boolean transitionFovConditionBefore = (snake.getPosX() >= transitionFovBeforePos1 && snake.getPosX() < transitionFovBeforePos2);
            boolean transitionFovConditionAfter = (snake.getPosX() >= transitionFovAfterPos1 && snake.getPosX() < transitionFovAfterPos2);

            if(fovCondition && transitionFovConditionBefore && !transitionFovConditionAfter) {

                /*
                If the distance between the snake and the start of the current fov is higher than the distance between the snake and the end of the before fov, use the current fov
                 */
                if((snake.getPosX() - fovPos1) <= (transitionFovBeforePos1 - snake.getPosX())) {

                    fovX = i - 1;
                    snakeFovPos1X = transitionFovBeforePos1;
                    snakeFovPos2X = transitionFovBeforePos2;
                    break;

                } else if((snake.getPosX() - fovPos1) > (transitionFovBeforePos1 - snake.getPosX())) {

                    fovX = i;
                    snakeFovPos1X = fovPos1;
                    snakeFovPos2X = fovPos2;
                    break;

                }
            } else if(fovCondition && transitionFovConditionAfter && !transitionFovConditionBefore) {

                if((snake.getPosX() - transitionFovAfterPos1) <= (fovPos2 - snake.getPosX())) {

                    fovX = i;
                    snakeFovPos1X = fovPos1;
                    snakeFovPos2X = fovPos2;
                    break;

                } else if ((snake.getPosX() - transitionFovAfterPos1) > (fovPos2 - snake.getPosX())) {

                    fovX = i + 1;
                    snakeFovPos1X = transitionFovAfterPos1;
                    snakeFovPos2X = transitionFovAfterPos2;
                    break;

                }

            }
        }

        if(snakeFovPos1X < 0 || snakeFovPos2X < 0) {
            return playerData.toString();
        }

        int fovY = -1;
        int snakeFovPos1Y = -1;
        int snakeFovPos2Y = -1;

        for(int i = 0; i < fovCountY; i++) {
            int fovPos1 = (this.fovsizeY / 2) * i;
            int fovPos2 = fovPos1 + this.fovsizeY;

            int transitionFovBeforePos1 = fovPos1 - (this.fovsizeY / 2);
            int transitionFovBeforePos2 = transitionFovBeforePos1 + this.fovsizeY;

            int transitionFovAfterPos1 = fovPos1 + (this.fovsizeY / 2);
            int transitionFovAfterPos2 = transitionFovAfterPos1 + this.fovsizeY;

            boolean fovCondition = (snake.getPosY() >= fovPos1 && snake.getPosY() < fovPos2);
            boolean transitionFovConditionBefore = (snake.getPosY() >= transitionFovBeforePos1 && snake.getPosY() < transitionFovBeforePos2);
            boolean transitionFovConditionAfter = (snake.getPosY() >= transitionFovAfterPos1 && snake.getPosY() < transitionFovAfterPos2);

            if(fovCondition && transitionFovConditionBefore && !transitionFovConditionAfter) {

                /*
                If the distance between the snake and the start of the current fov is higher than the distance between the snake and the end of the before fov, use the current fov
                 */
                if((snake.getPosY() - fovPos1) <= (transitionFovBeforePos1 - snake.getPosY())) {

                    fovY = i - 1;
                    snakeFovPos1Y = transitionFovBeforePos1;
                    snakeFovPos2Y = transitionFovBeforePos2;
                    break;

                } else if((snake.getPosY() - fovPos1) > (transitionFovBeforePos1 - snake.getPosY())) {

                    fovY = i;
                    snakeFovPos1Y = fovPos1;
                    snakeFovPos2Y = fovPos2;
                    break;

                }
            } else if(fovCondition && transitionFovConditionAfter && !transitionFovConditionBefore) {

                if((snake.getPosY() - transitionFovAfterPos1) <= (fovPos2 - snake.getPosY())) {

                    fovY = i;
                    snakeFovPos1Y = fovPos1;
                    snakeFovPos2Y = fovPos2;
                    break;

                } else if ((snake.getPosY() - transitionFovAfterPos1) > (fovPos2 - snake.getPosY())) {

                    fovY = i + 1;
                    snakeFovPos1Y = transitionFovAfterPos1;
                    snakeFovPos2Y = transitionFovAfterPos2;
                    break;

                }

            }
        }

        if(snakeFovPos1Y < 0 || snakeFovPos2Y < 0) {
            return playerData.toString();
        }

        int minY = snakeFovPos1Y;
        int maxY = snakeFovPos2Y;

        if(minY < 0) {
            minY = 0;
        }

        if(maxY > this.borderY) {
            maxY = this.borderY;
        }

        int minX = snakeFovPos1X;
        int maxX = snakeFovPos2X;

        if(minX < 0) {
            minX = 0;
        }

        if(maxX > this.borderX) {
            maxX = this.borderX;
        }

        playerData.put("fields", this.getFields(minY, maxY, minX, maxX, true, snake));
        playerData.put("relative", true);
        playerData.put("fixed_fov", true);

        return playerData.toString();
    }

    private JSONArray getFields(int minY, int maxY, int minX, int maxX, boolean relative, Snake snake) {
        JSONArray fields = new JSONArray();

        int relativey = 0;
        for (int iy = minY; iy <= maxY; iy++) {
            int yvalue;
            if (relative) {
                yvalue = relativey;
            } else {
                yvalue = iy;
            }

            int relativex = 0;
            for (int ix = minX; ix <= maxX; ix++) {
                int xvalue;
                if (relative) {
                    xvalue = relativex;
                } else {
                    xvalue = ix;
                }

                if (ix == 0) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else if (ix == this.borderX) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else if (iy == 0) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else if (iy == this.borderY) {
                    fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.BORDER));
                } else {
                    GameObject field = this.getField(ix, iy);
                    if (field instanceof Snake) {
                        Snake fieldSnake = (Snake) field;
                        if (field == snake) {
                            if (Arrays.equals(fieldSnake.getPos(), new int[]{ix, iy})) {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.getPlayerHeadOwnValue(fieldSnake.getFacing()), fieldSnake.isHasMoved()));
                            } else {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.PLAYER_BODY_OWN));
                            }
                        } else {
                            if (Arrays.equals(fieldSnake.getPos(), new int[]{ix, iy})) {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.getPlayerHeadOtherValue(fieldSnake.getFacing())));
                            } else {
                                fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.PLAYER_BODY_OTHER));
                            }
                        }
                    } else if (field instanceof Item) {
                        if (field instanceof Food) {
                            fields.put(this.createCoordsJSONArray(xvalue, yvalue, FieldState.ITEM_FOOD));
                        } else if (field instanceof SuperFood) {
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

        return fields;
    }

    /**
     * Creates a JSONArray [0,0,0,0]
     * 1. Value: X-Coordinates
     * 2. Value: Y-Coordinates
     * 3. Value: FieldState
     *
     * @param x          X-Coordinates
     * @param y          Y-Coordinates
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

    private JSONArray createCoordsJSONArray(int x, int y, int fieldState, boolean hasMoved) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(x);
        jsonArray.put(y);
        jsonArray.put(fieldState);
        jsonArray.put(hasMoved);
        return jsonArray;
    }

    // FIELD MANAGEMENT

    /**
     * Returns true if the specified field is free.
     * This also includes outside fields and the worldborder.
     *
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
        if (x < 0 || x >= this.borderX) {
            return false;
        }
        if (y < 0 || y >= this.borderY) {
            return false;
        }

        int left = x - Math.round(area / 2);
        int top = y - Math.round(area / 2);

        if (left < 0) {
            left = 0;
        }
        if (top < 0) {
            top = 0;
        }

        int right = x + Math.round(area / 2);
        int bottom = y + Math.round(area / 2);

        if (right >= this.borderX) {
            right = this.borderX - 1;
        }
        if (bottom >= this.borderY) {
            bottom = this.borderY - 1;
        }

        for (int iy = top; iy < bottom; iy++) {
            for (int ix = left; ix < right; ix++) {
                if (!this.isFree(ix, iy)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the specified field is not another player
     *
     * @param posX Position X
     * @param posY Position Y
     * @return boolean
     */
    public boolean isOtherPlayerFree(int posX, int posY, Snake snake) {
        GameObject fieldObject = this.getField(posX, posY);
        return (!(fieldObject instanceof Snake) || fieldObject == snake);
    }

    /**
     * Returns if the specified field is a food item
     *
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
     *
     * @param posX Position X
     * @param posY Position Y
     * @return GameObject
     */
    public GameObject getField(int posX, int posY) {
        for (Snake snake : this.snakeList) {
            if (snake.getPosX() == posX && snake.getPosY() == posY) {
                return snake;
            } else {
                for (int[] pos : snake.getBodyPositions()) {
                    if (pos[0] == posX && pos[1] == posY) {
                        return snake;
                    }
                }
            }
        }
        synchronized (this.itemList) {
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
     *
     * @param posX X Coordinates
     * @param posY Y Coordinates
     * @return true if the field is on the worldborder
     */
    public boolean isBorder(int posX, int posY) {
        if (posX == 0 || posX == this.borderX || posY == 0 || posY == this.borderY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a field is outside (world border included) of the world border
     *
     * @param posX X Coordinates
     * @param posY Y Coordinates
     * @return true (if outside)
     */
    public boolean isOutside(int posX, int posY) {
        if (posX <= 0 || posX >= this.borderX || posY <= 0 || posY >= this.borderY) {
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
            // this.server.getLogger().debug("RANDOM-INT-GENERATOR", "Exception: " + e);
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
     *
     * @param connection Player
     * @return Snake
     */
    public Snake getSnakeOfConnection(ServerConnection connection) {
        for (Snake snake : this.snakeList) {
            if (snake.getConnection() == connection) {
                return snake;
            }
        }
        return null;
    }

    /**
     * Returns the snake with a specific list index
     *
     * @param index List index (snake id)
     * @return Snake (if it is available, else null)
     */
    public Snake getSnake(int index) {
        try {
            return this.snakeList.get(index);
        } catch (IndexOutOfBoundsException ignored) {
        }
        return null;
    }

    public int getSnakeId(Snake snake) {
        if (this.snakeList.contains(snake)) {
            return this.snakeList.indexOf(snake);
        }
        return -1;
    }

    /**
     * Get a copy of the snake list.
     *
     * @return copy of snake list
     */
    public List<Snake> getSnakeList() {
        return List.copyOf(this.snakeList);
    }

    /**
     * Get a copy of the item list.
     *
     * @return copy of item list
     */
    public List<Item> getItemList() {
        return List.copyOf(this.itemList);
    }

    // SPECTATORS

    private String getSendableSpectatorData() {
        JSONObject spectatorData = new JSONObject();
        spectatorData.put("size_x", this.borderX);
        spectatorData.put("size_y", this.borderY);
        spectatorData.put("snake_count", this.snakeList.size());
        spectatorData.put("item_count", this.itemList.size());

        JSONArray fields = new JSONArray();

        int minY = 0;
        int maxY = this.borderY;

        for (int iy = minY; iy <= maxY; iy++) {
            int minX = 0;
            int maxX = this.borderX;

            for (int ix = minX; ix <= maxX; ix++) {
                if (ix == 0) {
                    fields.put(this.createCoordsJSONArray(ix, iy, FieldState.BORDER));
                } else if (ix == this.borderX) {
                    fields.put(this.createCoordsJSONArray(ix, iy, FieldState.BORDER));
                } else if (iy == 0) {
                    fields.put(this.createCoordsJSONArray(ix, iy, FieldState.BORDER));
                } else if (iy == this.borderY) {
                    fields.put(this.createCoordsJSONArray(ix, iy, FieldState.BORDER));
                } else {
                    GameObject field = this.getField(ix, iy);
                    if (field instanceof Snake) {
                        Snake snake = (Snake) field;
                        if (Arrays.equals(field.getPos(), new int[]{ix, iy})) {
                            fields.put(this.createCoordsJSONArray(ix, iy, FieldState.getPlayerHeadOtherValue(snake.getFacing())));
                        } else {
                            fields.put(this.createCoordsJSONArray(ix, iy, FieldState.PLAYER_BODY_OTHER));
                        }
                    } else if (field instanceof Item) {
                        if (field instanceof Food) {
                            fields.put(this.createCoordsJSONArray(ix, iy, FieldState.ITEM_FOOD));
                        } else if (field instanceof SuperFood) {
                            fields.put(this.createCoordsJSONArray(ix, iy, FieldState.ITEM_SUPER_FOOD));
                        } else {
                            // DO NOTHING
                            //fields.put(this.createCoordsJSONArray(ix, iy, FieldState.ITEM_UNKNOWN));
                        }
                    } else {
                        // DO NOTHING
                        //fields.put(this.createCoordsJSONArray(ix, iy, FieldState.EMPTY));
                    }
                }
            }
        }

        spectatorData.put("fields", fields);

        return spectatorData.toString();
    }

    // SAVEGAMES
    /**
     * Return the entire game as a JSONObject with all game data in it
     *
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
        for (Snake snake : this.snakeList) {
            JSONObject snakeData = new JSONObject();
            snakeData.put("id", this.getSnakeId(snake));
            snakeData.put("x", snake.getPosX());
            snakeData.put("y", snake.getPosY());
            snakeData.put("facing", snake.getFacing());
            snakeData.put("movein", snake.getMoveIn());

            JSONArray bodyPositions = new JSONArray();
            for (int[] body : snake.getBodyPositions()) {
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
        for (Item item : this.itemList) {
            JSONObject itemData = new JSONObject();

            itemData.put("x", item.getPosX());
            itemData.put("y", item.getPosY());
            itemData.put("description", item.getDescription());
            itemData.put("despawnTime", item.getDespawnTime());

            if (item instanceof Food) {
                itemData.put("food_value", ((Food) item).getFoodValue());
            } else if (item instanceof SuperFood) {
                itemData.put("superfood_value", ((SuperFood) item).getValue());
            }

            items.put(itemData);
        }
        savegame.put("items", items);

        return savegame;
    }

    public SlakeoverflowServer getServer() {
        return this.server;
    }

    // EXTENDS

    /**
     * This method will be executed if
     */
    public void onTick1() {}

    public void onTick2() {}

    public void onTick3() {}

    public void onTick4() {}
}
