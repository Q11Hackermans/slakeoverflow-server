package com.github.q11hackermans.slakeoverflow_server.config;

public class ServerConfig {
    private int port;
    private boolean whitelist;
    private int slots;
    private int minFoodValue;
    private int maxFoodValue;

    public ServerConfig() {
        this.port = 26677;
        this.whitelist = true;
        this.slots = 10;
        this.minFoodValue = 1;
        this.maxFoodValue = 2;
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

    public int getMinFoodValue() {
        if(this.minFoodValue >= 1 && this.minFoodValue <= 10) {
            return minFoodValue;
        } else {
            return 1;
        }
    }

    public void setMinFoodValue(int minFoodValue) {
        if(minFoodValue >= 1 && minFoodValue <= 10) {
            this.minFoodValue = minFoodValue;
        } else {
            this.minFoodValue = 1;
        }
    }

    public int getMaxFoodValue() {
        if(this.maxFoodValue >= 1 && this.maxFoodValue <= 10) {
            return maxFoodValue;
        } else {
            return 2;
        }
    }

    public void setMaxFoodValue(int maxFoodValue) {
        if(maxFoodValue >= 1 && maxFoodValue <= 10) {
            this.maxFoodValue = maxFoodValue;
        } else {
            this.maxFoodValue = 2;
        }
    }
}
