package com.github.q11hackermans.slakeoverflow_server.connections;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOStreamHandler;

import java.util.UUID;

public class Player extends ServerConnection {
    private String username;

    public Player(UUID clientId) {
        super(clientId);
        this.username = "";
    }
    public Player(UUID clientId, String username) {
        super(clientId);
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
