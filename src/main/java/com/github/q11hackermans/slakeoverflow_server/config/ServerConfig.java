package com.github.q11hackermans.slakeoverflow_server.config;

public class ServerConfig {
    // SERVER OPTIONS
    private int port;
    private boolean autoConnectionAccept;
    private boolean userAuthentication;
    private boolean unauthenticatePlayerOnDeath;
    private boolean printDebugMessages;
    private int maxConnections;
    // GAME OPTIONS
    private int maxPlayers;
    private int maxSpectators;
    private int minFoodValue;
    private int maxFoodValue;
    private int defaultSnakeLength;
    private int snakeSpeedBase;
    private int snakeSpeedModifierValue;
    private int snakeSpeedModifierBodycount;
    private int defaultGameFieldSizeX;
    private int defaultGameFieldSizeY;
    private int itemDefaultDespawnTime;
    private int ItemSuperFoodDespawnTime;
    private boolean enableSpectator;
    private int spectatorUpdateInterval;

    // ADVANCED OPTIONS
    private final boolean advancedOptionsEnabled;
    private boolean overrideServerTickrate;
    private int customServerTickrate;
    private int customServerTickrateIdle;

    public ServerConfig(boolean advancedOptionsEnabled) {
        // SERVER OPTIONS
        this.port = 26677;
        this.autoConnectionAccept = true;
        this.userAuthentication = false;
        this.maxConnections = 20;

        // GAME OPTIONS
        this.maxPlayers = 10;
        this.maxSpectators = 2;
        this.minFoodValue = 1;
        this.maxFoodValue = 2;
        this.defaultSnakeLength = 3;
        this.snakeSpeedBase = 8;
        this.snakeSpeedModifierValue = 1;
        this.snakeSpeedModifierBodycount = 2;
        this.defaultGameFieldSizeX = 100;
        this.defaultGameFieldSizeY = 100;
        this.unauthenticatePlayerOnDeath = true;
        this.printDebugMessages = false;
        this.itemDefaultDespawnTime = 60;
        this.ItemSuperFoodDespawnTime = 120;
        this.enableSpectator = true;
        this.spectatorUpdateInterval = 200;

        // ADVANCED OPTIONS
        this.advancedOptionsEnabled = advancedOptionsEnabled;
        this.overrideServerTickrate = false;
        this.customServerTickrate = 50;
        this.customServerTickrateIdle = 950;
    }


    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAutoConnectionAccept() {
        return this.autoConnectionAccept;
    }

    public void setAutoConnectionAccept(boolean autoConnectionAccept) {
        this.autoConnectionAccept = autoConnectionAccept;
    }

    public boolean isUserAuthentication() {
        return this.userAuthentication;
    }

    public void setUserAuthentication(boolean userAuthentication) {
        this.userAuthentication = userAuthentication;
    }

    public int getMaxPlayers() {
        if (maxPlayers > 0) {
            return this.maxPlayers;
        } else {
            return 1;
        }
    }

    public void setMaxPlayers(int maxPlayers) {
        if (maxPlayers > 0) {
            this.maxPlayers = maxPlayers;
        }
    }

    public int getMinFoodValue() {
        if (this.minFoodValue >= 1 && this.minFoodValue <= 10) {
            return minFoodValue;
        } else {
            return 1;
        }
    }

    public void setMinFoodValue(int minFoodValue) {
        if (minFoodValue >= 1 && minFoodValue <= 10) {
            this.minFoodValue = minFoodValue;
        } else {
            this.minFoodValue = 1;
        }
    }

    public int getMaxFoodValue() {
        if (this.maxFoodValue >= 1 && this.maxFoodValue <= 10) {
            return maxFoodValue;
        } else {
            return 2;
        }
    }

    public void setMaxFoodValue(int maxFoodValue) {
        if (maxFoodValue >= 1 && maxFoodValue <= 10) {
            this.maxFoodValue = maxFoodValue;
        } else {
            this.maxFoodValue = 2;
        }
    }

    public int getDefaultSnakeLength() {
        if (this.defaultSnakeLength > 0 && this.defaultSnakeLength <= 10) {
            return this.defaultSnakeLength;
        } else {
            return 1;
        }
    }

    public void setDefaultSnakeLength(int defaultSnakeLength) {
        if (defaultSnakeLength > 0 && defaultSnakeLength <= 10) {
            this.defaultSnakeLength = defaultSnakeLength;
        }
    }

    public int getSnakeSpeedBase() {
        if (this.snakeSpeedBase > 0) {
            return snakeSpeedBase;
        } else {
            return 20;
        }
    }

    public void setSnakeSpeedBase(int snakeSpeedBase) {
        if (snakeSpeedBase > 0) {
            this.snakeSpeedBase = snakeSpeedBase;
        }
    }

    public int getSnakeSpeedModifierValue() {
        if (this.snakeSpeedModifierValue >= 0) {
            return snakeSpeedModifierValue;
        } else {
            return 1;
        }
    }

    public void setSnakeSpeedModifierValue(int snakeSpeedModifierValue) {
        if (snakeSpeedModifierValue >= 0) {
            this.snakeSpeedModifierValue = snakeSpeedModifierValue;
        }
    }

    public int getSnakeSpeedModifierBodycount() {
        if (this.snakeSpeedModifierBodycount > 0) {
            return snakeSpeedModifierBodycount;
        } else {
            return 2;
        }
    }

    public void setSnakeSpeedModifierBodycount(int snakeSpeedModifierBodycount) {
        if (snakeSpeedModifierBodycount >= 0) {
            this.snakeSpeedModifierBodycount = snakeSpeedModifierBodycount;
        }
    }

    public int getDefaultGameFieldSizeX() {
        if (this.defaultGameFieldSizeX >= 50) {
            return this.defaultGameFieldSizeX;
        } else {
            return 50;
        }
    }

    public void setDefaultGameFieldSizeX(int defaultGameFieldSizeX) {
        if (defaultGameFieldSizeX >= 50) {
            this.defaultGameFieldSizeX = defaultGameFieldSizeX;
        }
    }

    public int getDefaultGameFieldSizeY() {
        if (this.defaultGameFieldSizeY >= 50) {
            return defaultGameFieldSizeY;
        } else {
            return 50;
        }
    }

    public void setDefaultGameFieldSizeY(int defaultGameFieldSizeY) {
        this.defaultGameFieldSizeY = defaultGameFieldSizeY;
    }

    public boolean isUnauthenticatePlayerOnDeath() {
        return this.unauthenticatePlayerOnDeath;
    }

    public void setUnauthenticatePlayerOnDeath(boolean unauthenticatePlayerOnDeath) {
        this.unauthenticatePlayerOnDeath = unauthenticatePlayerOnDeath;
    }

    public boolean isPrintDebugMessages() {
        return this.printDebugMessages;
    }

    public void setPrintDebugMessages(boolean printDebugMessages) {
        this.printDebugMessages = printDebugMessages;
    }

    public int getItemDefaultDespawnTime() {
        return itemDefaultDespawnTime;
    }

    public void setItemDefaultDespawnTime(int itemDefaultDespawnTime) {
        if (itemDefaultDespawnTime > 0) {
            this.itemDefaultDespawnTime = itemDefaultDespawnTime;
        } else {
            throw new IllegalArgumentException("The time must be higher than 0");
        }
    }

    public int getItemSuperFoodDespawnTime() {
        return ItemSuperFoodDespawnTime;
    }

    public void setItemSuperFoodDespawnTime(int itemSuperFoodDespawnTime) {
        if (itemSuperFoodDespawnTime > 0) {
            this.ItemSuperFoodDespawnTime = itemSuperFoodDespawnTime;
        } else {
            throw new IllegalArgumentException("The time must be higher than 0");
        }
    }

    public boolean isEnableSpectator() {
        return this.enableSpectator;
    }

    public void setEnableSpectator(boolean enableSpectator) {
        this.enableSpectator = enableSpectator;
    }

    public int getSpectatorUpdateInterval() {
        return spectatorUpdateInterval;
    }

    public void setSpectatorUpdateInterval(int spectatorUpdateInterval) {
        if(spectatorUpdateInterval > 0) {
            this.spectatorUpdateInterval = spectatorUpdateInterval;
        } else {
            throw new IllegalArgumentException("The value must be higher than 0");
        }
    }

    public int getMaxConnections() {
        return this.maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        if (maxConnections > 0) {
            this.maxConnections = maxConnections;
        } else {
            throw new IllegalArgumentException("The value must be higher than 0");
        }
    }

    public int getMaxSpectators() {
        return this.maxSpectators;
    }

    public void setMaxSpectators(int maxSpectators) {
        if(maxSpectators > 0) {
            this.maxSpectators = maxSpectators;
        } else {
            throw new IllegalArgumentException("The value must be higher than 0");
        }
    }

    // ADVANCED
    public boolean isAdvancedOptionsEnabled() {
        return this.advancedOptionsEnabled;
    }

    public boolean isOverrideServerTickrate() {
        return (this.advancedOptionsEnabled && this.overrideServerTickrate);
    }

    public void setOverrideServerTickrate(boolean overrideServerTickrate) {
        this.overrideServerTickrate = overrideServerTickrate;
    }

    public int getCustomServerTickrate() {
        if (this.advancedOptionsEnabled && this.customServerTickrate > 0 && this.customServerTickrate < 120000) {
            return this.customServerTickrate;
        } else {
            return 50;
        }
    }

    public void setCustomServerTickrate(int customServerTickrate) {
        if (customServerTickrate > 0 && customServerTickrate < 60000) {
            this.customServerTickrate = customServerTickrate;
        }
    }

    public int getCustomServerTickrateIdle() {
        if (this.advancedOptionsEnabled && this.customServerTickrateIdle >= 0 && this.customServerTickrateIdle < 120000) {
            return this.customServerTickrateIdle;
        } else {
            return 950;
        }
    }

    public void setCustomServerTickrateIdle(int customServerTickrateIdle) {
        if (customServerTickrateIdle >= 0 && customServerTickrateIdle < 120000) {
            this.customServerTickrateIdle = customServerTickrateIdle;
        }
    }
}
