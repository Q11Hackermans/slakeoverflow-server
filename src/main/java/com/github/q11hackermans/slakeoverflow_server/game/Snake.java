package com.github.q11hackermans.slakeoverflow_server.game;

import com.github.q11hackermans.slakeoverflow_server.GameSession;
import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.constants.AuthenticationState;
import com.github.q11hackermans.slakeoverflow_server.constants.Direction;

import java.util.ArrayList;
import java.util.List;

public class Snake implements GameObject {
    // MANAGEMENT
    private final GameSession gameSession;
    private ServerConnection connection;
    // MOVEMENT
    private int speed;
    private int posx;
    private int posy;
    private int growthBalance; // The internal "food balance" the snake can grow
    private int facing;
    private int newFacing;
    private boolean newFacingUpdated;
    private List<int[]> bodyPositions; //[gerade Zahlen] = X - [ungerade Zahlen] = Y
    private boolean alive;
    private boolean freezed;
    private int moveIn;
    private boolean fastMove;
    private int fastMoveMultiplier;
    private boolean hasMoved;
    private boolean fixedFovPlayerdataSystem;
    private int rewardTimer;

    public Snake(GameSession gameSession, ServerConnection connection, int x, int y, int facing, List<int[]> bodyPositions) {
        this.gameSession = gameSession;
        this.connection = connection;
        this.bodyPositions = new ArrayList<>();
        this.posx = x;
        this.posy = y;
        this.facing = facing;
        this.newFacing = facing;
        this.newFacingUpdated = false;
        this.alive = true;
        this.freezed = false;
        this.moveIn = 0;
        this.fastMove = false;
        this.fastMoveMultiplier = 1;
        this.hasMoved = false;
        this.fixedFovPlayerdataSystem = false;
        this.rewardTimer = 0;

        if (bodyPositions != null) {
            this.bodyPositions.addAll(bodyPositions);
        }
    }

    public Snake(GameSession gameSession, ServerConnection connection, int x, int y, int facing, int length) {
        this(gameSession, connection, x, y, facing, null);

        // set start length body positions
        if (this.facing == Direction.NORTH) {
            int bodyY = this.posy + 1;
            for (int i = length - 1; i > 0; i--) {
                this.bodyPositions.add(new int[]{this.posx, bodyY});
                bodyY = bodyY + 1;
            }
        } else if (this.facing == Direction.SOUTH) {
            int bodyY = this.posy - 1;
            for (int i = length - 1; i > 0; i--) {
                this.bodyPositions.add(new int[]{this.posx, bodyY});
                bodyY = bodyY - 1;
            }
        } else if (this.facing == Direction.WEST) {
            int bodyX = this.posx - 1;
            for (int i = length - 1; i > 0; i--) {
                this.bodyPositions.add(new int[]{bodyX, this.posy});
                bodyX = bodyX - 1;
            }
        } else if (this.facing == Direction.EAST) {
            int bodyX = this.posx - 1;
            for (int i = length - 1; i > 0; i--) {
                this.bodyPositions.add(new int[]{bodyX, this.posy});
                bodyX = bodyX - 1;
            }
        } else {
            this.alive = false;
        }
    }

    @Override
    public int[] getPos() {
        if (this.alive) {
            return new int[]{this.posx, this.posy};
        } else {
            return new int[]{-1, -1};
        }
    }

    @Override
    public int getPosX() {
        if (this.alive) {
            return this.posx;
        } else {
            return -1;
        }
    }

    @Override
    public int getPosY() {
        if (this.alive) {
            return this.posy;
        } else {
            return -1;
        }
    }

    /**
     * Set the position manually
     *
     * @param x X Coordinates
     * @param y Y Coordinates
     * @throws IllegalArgumentException If value is lower/higher than the world border
     * @deprecated It takes time until all bodies of the snake are updated to the new position
     */
    @Deprecated
    public void setPosition(int x, int y) {
        if (x >= 0 && x < this.gameSession.getBorder()[0] && y >= 0 && y < this.gameSession.getBorder()[1]) {
            this.posx = x;
            this.posy = y;
        } else {
            throw new IllegalArgumentException("Value out of range");
        }
    }

    public List<int[]> getBodyPositions() {
        if (this.alive) {
            return List.copyOf(bodyPositions);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Returns the facing
     *
     * @return facing
     */
    public int getFacing() {
        return this.facing;
    }

    /**
     * Returns the length
     *
     * @return length
     */
    public int getLength() {
        return this.bodyPositions.size() + 1;
    }

    /**
     * Add bodies to the snake
     *
     * @param length the length to add
     */
    public void addBody(int length) {
        for (int i = 0; i < length; i++) {
            this.addTale();
        }
    }

    /**
     * Remove bodies from the snake
     *
     * @param length the length to remove
     */
    public void removeBody(int length) {
        for (int i = 0; i < length; i++) {
            if (!this.bodyPositions.isEmpty()) {
                this.bodyPositions.remove(this.bodyPositions.size() - 1);
            }
        }
    }

    /**
     * Clears the snake bodies
     */
    public void clearBodies() {
        this.bodyPositions.clear();
    }

    /**
     * Get the body-id of this snake at this position
     *
     * @param posX Position X
     * @param posY Position Y
     * @return int - [-1 not this snake, 0 <= bodypositions]
     */
    private int getPosBodyID(int posX, int posY) {

        for(int i = 0; i < this.bodyPositions.size(); i++) {
            int[] body = this.bodyPositions.get(i);

            if(body[0] == posX && body[1] == posY) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the player related to this snake
     *
     * @return Player related to this snake
     */
    public ServerConnection getConnection() {
        return this.connection;
    }

    /**
     * With this method, the ServerConnection of the snake can be updated.
     * If you set the ServerConnection to null or to a disconnected connection, the snake gets killed automatically and will be removed from the game session.
     *
     * @param connection new connection
     *                   a@deprecated ONLY USE IF THE GAME IS PAUSED!
     */
    public void setNewServerConnection(ServerConnection connection) {
        this.connection = connection;
    }

    public int getMoveIn() {
        return this.moveIn;
    }

    // TICK

    /**
     * TICK
     */
    public void tick() {
        if (this.connection == null || !this.connection.isConnected() || this.connection.getAuthenticationState() != AuthenticationState.PLAYER) {
            this.killSnake();
            return;
        }

        if(!this.freezed) {
            if (this.moveIn <= 0) {

                if (this.facing != this.newFacing) {
                    this.move(this.newFacing);
                } else {
                    this.move();
                }

                this.moveIn = this.calcMoveIn();

                if(this.getGameSession().getServer().getConfigManager().getConfig().isEnableSnakeSpeedBoost() && this.fastMove) {
                    this.removeBody(1);
                }

                this.fastMove = false;

                this.hasMoved = true;
            } else {

                if(this.getGameSession().getServer().getConfigManager().getConfig().isEnableSnakeSpeedBoost() && this.fastMove) {
                    if((this.moveIn -= this.fastMoveMultiplier) >= 0) {
                        this.moveIn -= this.fastMoveMultiplier;
                    } else {
                        this.moveIn = 0;
                    }
                    this.fastMoveMultiplier += 1;
                } else {
                    this.moveIn--;
                    this.fastMoveMultiplier = 2;
                }

                this.hasMoved = false;
            }
        } else {
            this.hasMoved = false;
        }

        if(this.rewardTimer > this.gameSession.getServer().getConfigManager().getConfig().getPlayingTimeCoinsRewardTime()) {
            this.rewardTimer = 0;

            if(this.connection != null) {
                AccountData account = this.connection.getAccount();

                if(account != null) {
                    int addValue = this.gameSession.getServer().getConfigManager().getConfig().getPlayingTimeCoinsRewardAmount() + (this.gameSession.getServer().getConfigManager().getConfig().getPlayingTimeCoinsRewardSnakeLengthIncrement() * this.getLength());

                    this.gameSession.getServer().getAccountSystem().changeBalance(account.getId(), addValue);
                }
            }
        } else {
            this.rewardTimer += 1;
        }

    }

    /**
     * Set the direction the snake will move during the next tick
     *
     * @param newFacing The direction the snake will move during the next tick
     */
    public void setNewFacing(int newFacing, boolean forced) {
        if (forced || !this.newFacingUpdated) {
            this.newFacing = newFacing;
            this.newFacingUpdated = true;
        }
    }

    /**
     * Set the direction the snake is facing now
     *
     * @param facing The direction the snake is facing now
     */
    public void setFacing(int facing) {
        if (Direction.isValid(facing)) {
            this.facing = facing;
        }
    }

    // MOVEMENT

    /**
     * Add one element to the tale of the snake
     */
    private void addTale() {
        this.bodyPositions.add(this.bodyPositions.size(), new int[]{0, 0});
    }

    private void move() {
        this.move(this.facing);
    }

    /**
     * Moves the snake one field into the given direction
     *
     * @param dir Direction the snake should move
     */
    private void move(int dir) {
        if (dir == Direction.NORTH && gameSession.isOtherPlayerFree(this.posx, (this.posy - 1), this) && this.posy > 1) {
            if (this.facing != Direction.SOUTH) {
                GameObject newHeadField = this.gameSession.getField(this.posx, this.posy - 1);
                if (newHeadField == this) {
                    if(this.getGameSession().getServer().getConfigManager().getConfig().isEatOwnSnake()) {
                        int bodyId = this.getPosBodyID(this.posx, this.posy - 1);
                        while (bodyId < (bodyPositions.size() - 1)) {
                            bodyPositions.remove(bodyId);
                        }
                    } else {
                        this.killSnake();
                    }
                } else if(newHeadField instanceof Item) {
                    int foodValue = gameSession.getFoodValue(this.posx, (this.posy - 1));
                    if (foodValue > 0) {
                        this.growSnake(foodValue);

                        if(this.connection != null) {
                            AccountData account = this.connection.getAccount();
                            if(account != null) {
                                if(newHeadField instanceof Food) {
                                    this.gameSession.getServer().getAccountSystem().changeBalance(account.getId(), this.gameSession.getServer().getConfigManager().getConfig().getFoodCoinsRewardAmount() + (this.gameSession.getServer().getConfigManager().getConfig().getFoodCoinsRewardAmount() * foodValue));
                                } else if(newHeadField instanceof SuperFood) {
                                    this.gameSession.getServer().getAccountSystem().changeBalance(account.getId(), this.gameSession.getServer().getConfigManager().getConfig().getSuperFoodCoinsRewardAmount() + (this.gameSession.getServer().getConfigManager().getConfig().getSuperFoodCoinsRewardFoodValueIncrement() * foodValue));
                                }
                            }
                        }
                    }

                    gameSession.killItem((Item) newHeadField);
                }

                this.moveBodies();
                this.posy--;

                this.setFacing(Direction.NORTH);
            } else {
                this.newFacing = this.facing;
                this.move();
            }

        } else if (dir == Direction.EAST && gameSession.isOtherPlayerFree((this.posx + 1), this.posy, this) && this.posx < (this.gameSession.getBorder()[0] - 1)) {
            if (this.facing != Direction.WEST) {
                GameObject newHeadField = this.gameSession.getField(this.posx + 1, this.posy);
                if (newHeadField == this) {
                    if(this.getGameSession().getServer().getConfigManager().getConfig().isEatOwnSnake()) {
                        int bodyId = this.getPosBodyID(this.posx + 1, this.posy);
                        while (bodyId < (bodyPositions.size() - 1)) {
                            bodyPositions.remove(bodyId);
                        }
                    } else {
                        this.killSnake();
                    }
                } else if(newHeadField instanceof Item) {
                    int foodValue = gameSession.getFoodValue((this.posx + 1), this.posy);
                    if (foodValue > 0) {
                        this.growSnake(foodValue);

                        if(this.connection != null) {
                            AccountData account = this.connection.getAccount();
                            if(account != null) {
                                if(newHeadField instanceof Food) {
                                    this.gameSession.getServer().getAccountSystem().changeBalance(account.getId(), this.gameSession.getServer().getConfigManager().getConfig().getFoodCoinsRewardAmount() + (this.gameSession.getServer().getConfigManager().getConfig().getFoodCoinsRewardAmount() * foodValue));
                                } else if(newHeadField instanceof SuperFood) {
                                    this.gameSession.getServer().getAccountSystem().changeBalance(account.getId(), this.gameSession.getServer().getConfigManager().getConfig().getSuperFoodCoinsRewardAmount() + (this.gameSession.getServer().getConfigManager().getConfig().getSuperFoodCoinsRewardFoodValueIncrement() * foodValue));
                                }
                            }
                        }
                    }

                    gameSession.killItem((Item) newHeadField);
                }

                this.moveBodies();
                this.posx++;

                this.setFacing(Direction.EAST);
            } else {
                this.newFacing = this.facing;
                this.move();
            }

        } else if (dir == Direction.SOUTH && gameSession.isOtherPlayerFree(this.posx, (this.posy + 1), this) && this.posy < (this.gameSession.getBorder()[1] - 1)) {
            if (this.facing != Direction.NORTH) {
                GameObject newHeadField = this.gameSession.getField(this.posx, this.posy + 1);
                if (newHeadField == this) {
                    if(this.getGameSession().getServer().getConfigManager().getConfig().isEatOwnSnake()) {
                        int bodyId = this.getPosBodyID(this.posx, this.posy + 1);
                        while (bodyId < (bodyPositions.size() - 1)) {
                            bodyPositions.remove(bodyId);
                        }
                    } else {
                        this.killSnake();
                    }
                } else if(newHeadField instanceof Item) {
                    int foodValue = gameSession.getFoodValue(this.posx, (this.posy + 1));
                    if (foodValue > 0) {
                        this.growSnake(foodValue);

                        if(this.connection != null) {
                            AccountData account = this.connection.getAccount();
                            if(account != null) {
                                if(newHeadField instanceof Food) {
                                    this.gameSession.getServer().getAccountSystem().changeBalance(account.getId(), this.gameSession.getServer().getConfigManager().getConfig().getFoodCoinsRewardAmount() + (this.gameSession.getServer().getConfigManager().getConfig().getFoodCoinsRewardAmount() * foodValue));
                                } else if(newHeadField instanceof SuperFood) {
                                    this.gameSession.getServer().getAccountSystem().changeBalance(account.getId(), this.gameSession.getServer().getConfigManager().getConfig().getSuperFoodCoinsRewardAmount() + (this.gameSession.getServer().getConfigManager().getConfig().getSuperFoodCoinsRewardFoodValueIncrement() * foodValue));
                                }
                            }
                        }
                    }

                    gameSession.killItem((Item) newHeadField);
                }

                this.moveBodies();
                this.posy++;

                this.setFacing(Direction.SOUTH);
            } else {
                this.newFacing = this.facing;
                this.move();
            }

        } else if (dir == Direction.WEST && gameSession.isOtherPlayerFree((this.posx - 1), this.posy, this) && this.posx > 1) {
            if (this.facing != Direction.EAST) {
                GameObject newHeadField = this.gameSession.getField(this.posx - 1, this.posy);
                if (newHeadField == this) {
                    if(this.getGameSession().getServer().getConfigManager().getConfig().isEatOwnSnake()) {
                        int bodyId = this.getPosBodyID(this.posx - 1, this.posy);
                        while (bodyId < (bodyPositions.size() - 1)) {
                            bodyPositions.remove(bodyId);
                        }
                    } else {
                        this.killSnake();
                    }
                } else if(newHeadField instanceof Item) {
                    int foodValue = gameSession.getFoodValue((this.posx - 1), this.posy);
                    if (foodValue > 0) {
                        this.growSnake(foodValue);

                        if(this.connection != null) {
                            AccountData account = this.connection.getAccount();
                            if(account != null) {
                                if(newHeadField instanceof Food) {
                                    this.gameSession.getServer().getAccountSystem().changeBalance(account.getId(), this.gameSession.getServer().getConfigManager().getConfig().getFoodCoinsRewardAmount() + (this.gameSession.getServer().getConfigManager().getConfig().getFoodCoinsRewardAmount() * foodValue));
                                } else if(newHeadField instanceof SuperFood) {
                                    this.gameSession.getServer().getAccountSystem().changeBalance(account.getId(), this.gameSession.getServer().getConfigManager().getConfig().getSuperFoodCoinsRewardAmount() + (this.gameSession.getServer().getConfigManager().getConfig().getSuperFoodCoinsRewardFoodValueIncrement() * foodValue));
                                }
                            }
                        }
                    }

                    gameSession.killItem((Item) newHeadField);
                }

                this.moveBodies();
                this.posx--;

                this.setFacing(Direction.WEST);
            } else {
                this.newFacing = this.facing;
                this.move();
            }

        } else {
            this.killSnake();
        }
    }

    /**
     * Adds one element to the tale if it has foods on its balance or expands the snake by the consumed amount of foods
     *
     * @param foodValue The value of foods the snake has consumed
     */
    private void growSnake(int foodValue) {
        if (this.growthBalance > 0) {
            this.growthBalance--;
            this.addTale();
            this.growthBalance += foodValue;
        } else if (foodValue > 0) {
            this.addTale();
            this.growthBalance += (foodValue - 1);
        }
    }

    private void moveBodies() {
        for (int i = this.bodyPositions.size() - 1; i >= 0; i--) {
            if (i == 0) {
                this.bodyPositions.set(i, new int[]{this.posx, this.posy});
            } else {
                int[] prev = this.bodyPositions.get(i - 1);
                this.bodyPositions.set(i, new int[]{prev[0], prev[1]});
            }
        }
    }

    public void setFreezed(boolean freezed) {
        this.freezed = freezed;
    }

    public boolean isFreezed() {
        return this.freezed;
    }

    public void fastMove() {
        if(!this.getBodyPositions().isEmpty()) {
            this.fastMove = true;
        }
    }

    public boolean isFastMove() {
        return this.fastMove;
    }

    public boolean isHasMoved() {
        return this.hasMoved;
    }

    public int calcMoveIn() {
        int speedModifierValue = this.getGameSession().getServer().getConfigManager().getConfig().getSnakeSpeedModifierValue();
        int speedModifierBodycount = this.getGameSession().getServer().getConfigManager().getConfig().getSnakeSpeedModifierBodycount();

        if (speedModifierBodycount <= 0) {
            speedModifierBodycount = 1;
        }

        int value = this.getGameSession().getServer().getConfigManager().getConfig().getSnakeSpeedBase();
        //for(int i = 0; i < (this.bodyPositions.size() / speedModifierBodycount); i++) {
        //    value = value + speedModifierValue;
        //}
        return value + (this.bodyPositions.size() / speedModifierBodycount) * speedModifierValue;
    }

    public boolean isFixedFovPlayerdataSystem() {
        return this.fixedFovPlayerdataSystem;
    }

    public void setFixedFovPlayerdataSystem(boolean fixedFovPlayerdataSystem) {
        this.fixedFovPlayerdataSystem = fixedFovPlayerdataSystem;
    }

    /**
     * This method will kill the snake.
     * After killing the snake, it can't be used anymore.
     */
    public void killSnake() {
        this.getGameSession().getServer().getLogger().debug("GAME", "Killed snake " + gameSession.getSnakeId(this));

        this.alive = false;
        this.gameSession.spawnSuperFoodAt((int) Math.round((this.bodyPositions.size() * this.getGameSession().getServer().getConfigManager().getConfig().getSnakeDeathSuperfoodMultiplier())), this.posx, this.posy);

        if (this.connection != null && this.connection.getAuthenticationState() == AuthenticationState.PLAYER && this.getGameSession().getServer().getConfigManager().getConfig().isUnauthenticatePlayerOnDeath()) {
            this.connection.unauthenticate();
        }
    }

    public boolean isAlive() {
        return this.alive;
    }

    @Override
    public GameSession getGameSession() {
        return this.gameSession;
    }
}
