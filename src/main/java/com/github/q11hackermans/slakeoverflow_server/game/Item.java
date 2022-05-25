package com.github.q11hackermans.slakeoverflow_server.game;

import com.github.q11hackermans.slakeoverflow_server.GameSession;
import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;

public abstract class Item implements GameObject {
    private int posx;
    private int posy;
    private int despawnTime; // IN SECONDS

    public Item(int posx, int posy) {
        this(posx, posy, SlakeoverflowServer.getServer().getConfigManager().getConfig().getDefaultItemDespawnTime());
    }

    public Item(int posx, int posy, int despawnTime) {
        this.posx = posx;
        this.posy = posy;
        this.despawnTime = despawnTime;
    }

    @Override
    public int[] getPos() {
        return new int[]{this.posx, this.posy};
    }

    @Override
    public int getPosX() {
        return this.posx;
    }

    @Override
    public int getPosY() {
        return this.posy;
    }

    /**
     * Update item position
     * @param x X Coordinates
     * @param y Y Coordinates
     * @throws IllegalArgumentException if the position is outside of the worldborder
     */
    public void setPosition(int x, int y) {
        GameSession gameSession = SlakeoverflowServer.getServer().getGameSession();
        if(gameSession != null) {
            if(x >= 0 && x < gameSession.getBorder()[0] && y >= 0 && y < gameSession.getBorder()[1]) {
                this.posx = x;
                this.posy = y;
            } else {
                throw new IllegalArgumentException("Value out of range");
            }
        }
    }

    public int getDespawnTime() {
        return this.despawnTime;
    }

    public void despawnCount() {
        if(this.despawnTime > 0) {
            this.despawnTime--;
        }
    }

    public void setDespawnTime(int despawnTime) {
        if(despawnTime >= 0) {
            this.despawnTime = despawnTime;
        } else {
            throw new IllegalArgumentException("The value must be higher or equal than 0");
        }
    }

    public abstract String getDescription();
}
