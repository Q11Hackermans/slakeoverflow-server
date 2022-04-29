package com.github.q11hackermans.slakeoverflow_server.game;

public abstract class Item implements GameObject {
    private final int posx;
    private final int posy;

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
}
