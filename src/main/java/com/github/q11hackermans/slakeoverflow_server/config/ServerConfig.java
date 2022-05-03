package com.github.q11hackermans.slakeoverflow_server.config;

public class ServerConfig {
    private int port;
    private boolean whitelist;
    private int slots;

    public ServerConfig() {
        this.port = 26677;
        this.whitelist = true;
        this.slots = 10;
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

    public int getSlots() {
        return this.slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }
}
