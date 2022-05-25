package com.github.q11hackermans.slakeoverflow_server.data;

import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;

import java.util.List;

public class SnakeData {
    private final ServerConnection connection;
    private final int posx;
    private final int posy;
    private final int facing;
    private final List<int[]> bodyPositions;
    private final int moveIn;

    public SnakeData(ServerConnection connection, int posx, int posy, int facing, List<int[]> bodyPositions, int moveIn) {
        this.connection = connection;
        this.posx = posx;
        this.posy = posy;
        this.facing = facing;
        this.bodyPositions = bodyPositions;
        this.moveIn = moveIn;
    }

    public ServerConnection getConnection() {
        return connection;
    }

    public int getPosx() {
        return posx;
    }

    public int getPosy() {
        return posy;
    }

    public int getFacing() {
        return facing;
    }

    public List<int[]> getBodyPositions() {
        return bodyPositions;
    }

    public int getMoveIn() {
        return moveIn;
    }
}
