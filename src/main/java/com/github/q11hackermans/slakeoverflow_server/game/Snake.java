package com.github.q11hackermans.slakeoverflow_server.game;

import com.github.q11hackermans.slakeoverflow_server.GameSession;
import com.github.q11hackermans.slakeoverflow_server.Player;
import com.github.q11hackermans.slakeoverflow_server.constants.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class Snake implements GameObject {
    // MANAGEMENT
    private final Player player;
    private GameSession gameSession;
    // MOVEMENT
    private int speed;
    private int posx;
    private int posy;
    private int length;
    private int growthBalance; // The internal "apple balance" the snake can grow
    private int facing;
    private int newFacing;
    private List<int[]> bodyPositions; //[gerade Zahlen] = X - [ungerade Zahlen] = Y

    public Snake(Player player, int x, int y, int facing, GameSession session) {
        this.player = player;
        this.bodyPositions = new ArrayList<>();
        this.posx = x;
        this.posy = y;
        this.length = 0;
        this.facing = facing;
        this.newFacing = facing;
        this.gameSession = session;
    }

    @Override
    public int[] getPos() {
        return new int[]{this.posx, this.posy};
    }

    @Override
    public int getPosX() {
        return 0;
    }

    @Override
    public int getPosY() {
        return 0;
    }

    public List<int[]> getBodyPositions() {
        return List.copyOf(bodyPositions);
    }

    /**
     * Get the player related to this snake
     *
     * @return Player related to this snake
     */
    public Player getPlayer() {
        return this.player;
    }

    // TICK

    /**
     * TICK
     */
    public void tick() {
        if (this.facing != this.newFacing) {
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
        if (dir == Direction.NORTH && this.facing != Direction.SOUTH && gameSession.isPlayerFree(this.posx, (this.posy - 1)) && this.posy > 1) {

            int appleValue = gameSession.getAppleValue(this.posx, (this.posy - 1));
            growSnake(appleValue);

            this.moveBodies();
            this.posy--;

            this.facing = Direction.NORTH;

        } else if(dir == Direction.EAST && this.facing != Direction.WEST && gameSession.isPlayerFree((this.posx + 1), this.posy) && this.posx < (this.gameSession.getBorder()[0] - 1)) {

            int appleValue = gameSession.getAppleValue((this.posx + 1), this.posy);
            growSnake(appleValue);

            this.moveBodies();
            this.posx ++;

            this.facing = Direction.EAST;

        } else if(dir == Direction.SOUTH && this.facing != Direction.NORTH && gameSession.isPlayerFree(this.posx, (this.posy + 1)) && this.posy < (this.gameSession.getBorder()[1] - 1)) {

            int appleValue = gameSession.getAppleValue(this.posx, (this.posy + 1));
            growSnake(appleValue);

            this.moveBodies();
            this.posy ++;

            this.facing = Direction.SOUTH;

        } else if(dir == Direction.WEST && this.facing != Direction.EAST && gameSession.isPlayerFree((this.posx - 1), this.posy) && this.posx > 1) {

            int appleValue = gameSession.getAppleValue((this.posx - 1), this.posy);
            growSnake(appleValue);

            this.moveBodies();
            this.posx --;

            this.facing = Direction.WEST;

        } else {
            this.gameSession.killSnake(this);
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
}
