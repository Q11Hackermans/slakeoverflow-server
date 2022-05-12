package com.github.q11hackermans.slakeoverflow_server.game;

import com.github.q11hackermans.slakeoverflow_server.GameSession;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.constants.ConnectionType;
import com.github.q11hackermans.slakeoverflow_server.constants.Direction;

import java.util.ArrayList;
import java.util.List;

public class Snake implements GameObject {
    // MANAGEMENT
    private final ServerConnection connection;
    private GameSession gameSession;
    // MOVEMENT
    private int speed;
    private int posx;
    private int posy;
    @Deprecated
    private int length;
    private int growthBalance; // The internal "apple balance" the snake can grow
    private int facing;
    private int newFacing;
    private List<int[]> bodyPositions; //[gerade Zahlen] = X - [ungerade Zahlen] = Y
    private boolean alive;

    public Snake(ServerConnection connection, int x, int y, int facing, int length, GameSession session) {
        this.connection = connection;
        this.bodyPositions = new ArrayList<>();
        this.posx = x;
        this.posy = y;
        this.length = 0;
        this.facing = facing;
        this.newFacing = facing;
        this.gameSession = session;
        this.alive = true;

        // set start length body positions
        if(this.facing == Direction.NORTH) {
            int bodyY = this.posy + 1;
            for(int i = length - 1; i > 0; i--) {
                this.bodyPositions.add(new int[]{this.posx, bodyY});
                bodyY = bodyY + 1;
            }
        } else if(this.facing == Direction.SOUTH) {
            int bodyY = this.posy - 1;
            for(int i = length - 1; i > 0; i--) {
                this.bodyPositions.add(new int[]{this.posx, bodyY});
                bodyY = bodyY - 1;
            }
        } else if(this.facing == Direction.WEST) {
            int bodyX = this.posx - 1;
            for(int i = length - 1; i > 0; i--) {
                this.bodyPositions.add(new int[]{bodyX, this.posy});
                bodyX = bodyX - 1;
            }
        } else if(this.facing == Direction.EAST) {
            int bodyX = this.posx - 1;
            for(int i = length - 1; i > 0; i--) {
                this.bodyPositions.add(new int[]{bodyX, this.posy});
                bodyX = bodyX - 1;
            }
        } else {
            this.alive = false;
        }
    }

    @Override
    public int[] getPos() {
        if(this.alive) {
            return new int[]{this.posx, this.posy};
        } else {
            return new int[]{-1, -1};
        }
    }

    @Override
    public int getPosX() {
        if(this.alive) {
            return this.posx;
        } else {
            return -1;
        }
    }

    @Override
    public int getPosY() {
        if(this.alive) {
            return this.posy;
        } else {
            return -1;
        }
    }

    public List<int[]> getBodyPositions() {
        if(this.alive) {
            return List.copyOf(bodyPositions);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get the body-id of this snake at this position
     * @param posX Position X
     * @param posY Position Y
     * @return int - [-1 not this snake, 0 <= bodypositions]
     */
    private int getPosBodyID(int posX, int posY){
        return bodyPositions.indexOf(new int[]{posX, posY});
    }

    /**
     * Get the player related to this snake
     *
     * @return Player related to this snake
     */
    public ServerConnection getConnection() {
        return this.connection;
    }

    // TICK

    /**
     * TICK
     */
    public void tick() {
        if(!this.connection.isConnected() && this.connection.getConnectionType() == ConnectionType.PLAYER) {
            this.killSnake();
            return;
        }

        if(this.facing != this.newFacing) {
            this.move(this.newFacing);
            return;
        }
        this.move();
    }

    /**
     * Set the direction the snake will move during the next tick
     *
     * @param newFacing The direction the snake will move during the next tick
     */
    public void setNewFacing(int newFacing) {
        this.newFacing = newFacing;
    }

    // MOVEMENT

    /**
     * Add one element to the tale of the snake
     */
    private void addTale() {
        this.length++;
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
        if (dir == Direction.NORTH && this.facing != Direction.SOUTH && gameSession.isOtherPlayerFree(this.posx, (this.posy - 1), this) && this.posy > 1) {

            GameObject newHeadField = this.gameSession.getField(this.posx, this.posy - 1);
            if(newHeadField == this){
                int bodyId = this.getPosBodyID(this.posx, this.posy - 1);
                while (bodyId < bodyPositions.size()) {
                    bodyPositions.remove(bodyId);
                }
            }
            int appleValue = gameSession.getFoodValue(this.posx, (this.posy - 1));
            growSnake(appleValue);

            this.moveBodies();
            this.posy--;

            this.setNewFacing(Direction.NORTH);

        } else if(dir == Direction.EAST && this.facing != Direction.WEST && gameSession.isOtherPlayerFree((this.posx + 1), this.posy, this) && this.posx < (this.gameSession.getBorder()[0] - 1)) {

            GameObject newHeadField = this.gameSession.getField(this.posx + 1, this.posy);
            if(newHeadField == this){
                int bodyId = this.getPosBodyID(this.posx + 1, this.posy);
                while (bodyId < bodyPositions.size()) {
                    bodyPositions.remove(bodyId);
                }
            }
            int appleValue = gameSession.getFoodValue((this.posx + 1), this.posy);
            growSnake(appleValue);

            this.moveBodies();
            this.posx ++;

            this.setNewFacing(Direction.EAST);

        } else if(dir == Direction.SOUTH && this.facing != Direction.NORTH && gameSession.isOtherPlayerFree(this.posx, (this.posy + 1), this) && this.posy < (this.gameSession.getBorder()[1] - 1)) {

            GameObject newHeadField = this.gameSession.getField(this.posx, this.posy + 1);
            if(newHeadField == this){
                int bodyId = this.getPosBodyID(this.posx, this.posy + 1);
                while (bodyId < bodyPositions.size()) {
                    bodyPositions.remove(bodyId);
                }
            }
            int appleValue = gameSession.getFoodValue(this.posx, (this.posy + 1));
            growSnake(appleValue);

            this.moveBodies();
            this.posy ++;

            this.setNewFacing(Direction.SOUTH);

        } else if(dir == Direction.WEST && this.facing != Direction.EAST && gameSession.isOtherPlayerFree((this.posx - 1), this.posy, this) && this.posx > 1) {

            GameObject newHeadField = this.gameSession.getField(this.posx - 1, this.posy);
            if(newHeadField == this){
                int bodyId = this.getPosBodyID(this.posx - 1, this.posy);
                while (bodyId < bodyPositions.size()) {
                    bodyPositions.remove(bodyId);
                }
            }
            int appleValue = gameSession.getFoodValue((this.posx - 1), this.posy);
            growSnake(appleValue);

            this.moveBodies();
            this.posx --;

            this.setNewFacing(Direction.WEST);

        } else {
            this.killSnake();
        }
    }

    /**
     * Adds one element to the tale if it has apples on its balance or expands the snake by the consumed amount of apples
     * @param appleValue The value of apples the snake has consumed
     */
    private void growSnake(int appleValue) {
        if(this.growthBalance > 0 ) {
            this.growthBalance--;
            this.addTale();
            this.growthBalance += appleValue;
        } else if(appleValue > 0){
            this.addTale();
            this.growthBalance += (appleValue - 1);
        }
    }

    private void moveBodies() {
        for(int i = this.bodyPositions.size() - 1; i >= 0; i--) {
            if(i == 0) {
                this.bodyPositions.set(i, new int[]{this.posx, this.posy});
            } else {
                int[] prev = this.bodyPositions.get(i-1);
                this.bodyPositions.set(i, new int[]{prev[0], prev[1]});
            }
        }
    }

    /**
     * This method will kill the snake.
     * After killing the snake, it cant be used anymore.
     */
    public void killSnake() {
        this.alive = false;
        this.gameSession.spawnSuperFoodAt((int) Math.round((this.bodyPositions.size() * 0.3)), this.posx, this.posy);
    }

    public boolean isAlive() {
        return this.alive;
    }
}
