package com.github.q11hackermans.slakeoverflow_server.game;

import com.github.q11hackermans.slakeoverflow_server.GameSession;
import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;

public abstract class Item implements GameObject {
    private int posx;
    private int posy;

    public Item(int posx, int posy) {
        this.posx = posx;
        this.posy = posy;
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

    public abstract String getDescription();
}
