package com.github.q11hackermans.slakeoverflow_server.game;

import com.github.q11hackermans.slakeoverflow_server.GameSession;
import com.github.q11hackermans.slakeoverflow_server.constants.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Snake implements GameObject {
    private UUID connectionId;
    private int speed;
    private int posx;
    private int posy;
    private GameSession gameSession;
    private int length;
    private int growthStock; // The internal "apple balance" the snake can grow
    private int facing;
    private List<int[]> bodyPositions; //[gerade Zahlen] = X - [ungerade Zahlen] = Y

    public Snake(int x, int y, UUID connectionID, int facing, GameSession session) {
        this.bodyPositions = new ArrayList<>();
        this.posx = x;
        this.posy = y;
        this.connectionId = connectionID;
        this.length = 0;
        this.facing = facing;
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

    private void addTale(){
        this.length ++;
        this.bodyPositions.add(this.bodyPositions.size(), new int[]{0, 0});
    }

    public void move() {
        this.move(this.facing);
    }

    public void move(int dir) {
        if(dir == Direction.NORTH && this.facing != Direction.SOUTH && gameSession.isFree(this.posx, (this.posy - 1)) && this.posy > 1) {

            int appleValue = gameSession.getAppleValue(this.posx, (this.posy - 1));
            if(this.growthStock > 0 ) {
                this.growthStock --;
                this.addTale();
                this.growthStock += appleValue;
            } else{
                this.addTale();
                this.growthStock += (appleValue - 1);
            }
                this.moveBodies();
                this.posy --;

            this.facing = Direction.NORTH;

        } else if(dir == Direction.EAST && this.facing != Direction.WEST && gameSession.isFree((this.posx + 1), this.posy) && this.posx < (this.gameSession.getBorder()[0] - 1)) {

            int appleValue = gameSession.getAppleValue(this.posx, (this.posy - 1));
            if(this.growthStock > 0 ) {
                this.growthStock --;
                this.addTale();
                this.growthStock += appleValue;
            } else{
                this.addTale();
                this.growthStock += (appleValue - 1);
            }
            this.moveBodies();
            this.posy --;

            this.facing = Direction.EAST;

        } else if(dir == Direction.SOUTH && this.facing != Direction.NORTH) {


            this.facing = Direction.SOUTH;

        } else if(dir == Direction.WEST && this.facing != Direction.EAST) {


            this.facing = Direction.WEST;

        } else {
            this.gameSession.kill(this);
        }
    }

    private void moveBodies() {
        for(int i = this.bodyPositions.size() - 1; i > 0; i--) {
            if(i == 0) {
                this.bodyPositions.set(i, new int[]{this.posx, this.posy});
            } else {
                int[] prev = this.bodyPositions.get(i-1);
                this.bodyPositions.set(i, new int[]{prev[0], prev[1]});
            }
        }
    }
}
