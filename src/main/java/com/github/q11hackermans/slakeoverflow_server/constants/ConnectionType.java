package com.github.q11hackermans.slakeoverflow_server.constants;

import net.jandie1505.connectionmanager.enums.ConnectionBehavior;

public enum ConnectionType {
    UNAUTHORIZED(0),
    PLAYER(1),
    SPECTATOR(2);

    private final int id;

    private ConnectionType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
