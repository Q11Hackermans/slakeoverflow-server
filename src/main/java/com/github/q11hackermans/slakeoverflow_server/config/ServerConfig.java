package com.github.q11hackermans.slakeoverflow_server.config;

public class ServerConfig {
    private int port;
    private boolean whitelist;

    public ServerConfig() {
        this.port = 26677;
        this.whitelist = true;
    }


    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isWhitelist() {
        return this.whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }
}
