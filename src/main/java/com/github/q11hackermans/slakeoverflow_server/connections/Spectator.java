package com.github.q11hackermans.slakeoverflow_server.connections;

import java.util.UUID;

public class Spectator extends ServerConnection {
    public Spectator(UUID clientId) {
        super(clientId);
    }
}
