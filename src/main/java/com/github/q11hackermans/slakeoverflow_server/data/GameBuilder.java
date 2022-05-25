package com.github.q11hackermans.slakeoverflow_server.data;

import com.github.q11hackermans.slakeoverflow_server.GameSession;
import com.github.q11hackermans.slakeoverflow_server.game.Item;

import java.util.ArrayList;
import java.util.List;

public class GameBuilder {
    private int borderX;
    private int borderY;
    private int fovsizeX;
    private int fovsizeY;
    private int nextItemDespawn;
    private List<SnakeData> snakes;
    private List<Item> items;

    public GameBuilder() {
        this.borderX = 10;
        this.borderY = 10;
        this.fovsizeX = 30;
        this.fovsizeY = 20;
        this.nextItemDespawn = 20;
        this.snakes = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    // CREATE
    public GameBuilder setBorderX(int borderX) {
        if(borderX > 10) {
            this.borderX = borderX;
        }
        return this;
    }

    public GameBuilder setBorderY(int borderY) {
        if(borderY > 10) {
            this.borderY = borderY;
        }
        return this;
    }

    public GameBuilder setFovX(int fovsizeX) {
        if(fovsizeX > 10) {
            this.fovsizeX = fovsizeX;
        }
        return this;
    }

    public GameBuilder setFovY(int fovsizeY) {
        if(fovsizeY > 10) {
            this.fovsizeY = fovsizeY;
        }
        return this;
    }

    public GameBuilder setNextItemDespawn(int nextItemDespawn) {
        this.nextItemDespawn = nextItemDespawn;
        return this;
    }

    public GameBuilder addSnakeData(SnakeData snakeData) {
        this.snakes.add(snakeData);
        return this;
    }

    public GameBuilder removeSnakeData(int index) {
        this.snakes.remove(index);
        return this;
    }

    public GameBuilder removeSnakeData(SnakeData snakeData) {
        this.snakes.remove(snakeData);
        return this;
    }

    public GameBuilder addItem(Item item) {
        this.items.add(item);
        return this;
    }

    public GameBuilder removeItem(int index) {
        this.items.remove(index);
        return this;
    }

    public GameBuilder removeItem(Item item) {
        this.items.remove(item);
        return this;
    }

    // BUILD

    public GameSession build() {
        return new GameSession(this.borderX, this.borderY, this.fovsizeX, this.fovsizeY, this.nextItemDespawn, this.snakes, this.items);
    }

    // GET LISTS
    public List<SnakeData> getSnakes() {
        return this.snakes;
    }

    public List<Item> getItems() {
        return this.items;
    }
}
