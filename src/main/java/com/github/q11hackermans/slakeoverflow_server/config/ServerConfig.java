package com.github.q11hackermans.slakeoverflow_server.config;

public class ServerConfig {
    private int port;
    private boolean autoConnectionAccept;
    private boolean userAuthentication;
    private int slots;
    private int minFoodValue;
    private int maxFoodValue;
    private int defaultSnakeLength;
    private int snakeSpeedBase;
    private int snakeSpeedModifierValue;
    private int snakeSpeedModifierBodycount;
    private int defaultGameFieldSizeX;
    private int defaultGameFieldSizeY;
    private boolean unauthenticatePlayerOnDeath;
    private boolean printDebugMessages;
    private int defaultItemDespawnTime;
    private int ItemSuperFoodDespawnTime;

    // ADVANCED OPTIONS
    private final boolean advancedOptionsEnabled;
    private boolean overrideServerTickrate;
    private int customServerTickrate;
    private int customServerTickrateIdle;

    public ServerConfig(boolean advancedOptionsEnabled) {
        this.port = 26677;
        this.autoConnectionAccept = true;
        this.userAuthentication = false;
        this.slots = 10;
        this.minFoodValue = 1;
        this.maxFoodValue = 2;
        this.defaultSnakeLength = 3;
        this.snakeSpeedBase = 20;
        this.snakeSpeedModifierValue = 1;
        this.snakeSpeedModifierBodycount = 2;
        this.defaultGameFieldSizeX = 100;
        this.defaultGameFieldSizeY = 100;
        this.unauthenticatePlayerOnDeath = true;
        this.printDebugMessages = false;
        this.defaultItemDespawnTime = 60;
        this.ItemSuperFoodDespawnTime = 120;

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

    public int getSlots() {
        if(slots > 0) {
            return this.slots;
        } else {
            return 1;
        }
    }

    public void setSlots(int slots) {
        if(slots > 0) {
            this.slots = slots;
        }
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

    public int getDefaultSnakeLength() {
        if(this.defaultSnakeLength > 0 && this.defaultSnakeLength <= 10) {
            return this.defaultSnakeLength;
        } else {
            return 1;
        }
    }

    public void setDefaultSnakeLength(int defaultSnakeLength) {
        if(defaultSnakeLength > 0 && defaultSnakeLength <= 10) {
            this.defaultSnakeLength = defaultSnakeLength;
        }
    }

    public int getSnakeSpeedBase() {
        if(this.snakeSpeedBase > 0) {
            return snakeSpeedBase;
        } else {
            return 20;
        }
    }

    public void setSnakeSpeedBase(int snakeSpeedBase) {
        if(snakeSpeedBase > 0) {
            this.snakeSpeedBase = snakeSpeedBase;
        }
    }

    public int getSnakeSpeedModifierValue() {
        if(this.snakeSpeedModifierValue >= 0) {
            return snakeSpeedModifierValue;
        } else {
            return 1;
        }
    }

    public void setSnakeSpeedModifierValue(int snakeSpeedModifierValue) {
        if(snakeSpeedModifierValue >= 0) {
            this.snakeSpeedModifierValue = snakeSpeedModifierValue;
        }
    }

    public int getSnakeSpeedModifierBodycount() {
        if(this.snakeSpeedModifierBodycount > 0) {
            return snakeSpeedModifierBodycount;
        } else {
            return 2;
        }
    }

    public void setSnakeSpeedModifierBodycount(int snakeSpeedModifierBodycount) {
        if(snakeSpeedModifierBodycount >= 0) {
            this.snakeSpeedModifierBodycount = snakeSpeedModifierBodycount;
        }
    }

    public int getDefaultGameFieldSizeX() {
        if(this.defaultGameFieldSizeX >= 50) {
            return this.defaultGameFieldSizeX;
        } else {
            return 50;
        }
    }

    public void setDefaultGameFieldSizeX(int defaultGameFieldSizeX) {
        if(defaultGameFieldSizeX >= 50) {
            this.defaultGameFieldSizeX = defaultGameFieldSizeX;
        }
    }

    public int getDefaultGameFieldSizeY() {
        if(this.defaultGameFieldSizeY >= 50) {
            return defaultGameFieldSizeY;
        } else {
            return 50;
        }
    }

    public void setDefaultGameFieldSizeY(int defaultGameFieldSizeY) {
        this.defaultGameFieldSizeY = defaultGameFieldSizeY;
    }

    public boolean isUnauthenticatePlayerOnDeath() {
        return unauthenticatePlayerOnDeath;
    }

    public void setUnauthenticatePlayerOnDeath(boolean unauthenticatePlayerOnDeath) {
        this.unauthenticatePlayerOnDeath = unauthenticatePlayerOnDeath;
    }

    public boolean isPrintDebugMessages() {
        return printDebugMessages;
    }

    public void setPrintDebugMessages(boolean printDebugMessages) {
        this.printDebugMessages = printDebugMessages;
    }

    public int getDefaultItemDespawnTime() {
        return defaultItemDespawnTime;
    }

    public void setDefaultItemDespawnTime(int defaultItemDespawnTime) {
        if(defaultItemDespawnTime > 0) {
            this.defaultItemDespawnTime = defaultItemDespawnTime;
        } else {
            throw new IllegalArgumentException("The time must be higher than 0");
        }
    }

    public int getItemSuperFoodDespawnTime() {
        return ItemSuperFoodDespawnTime;
    }

    public void setItemSuperFoodDespawnTime(int itemSuperFoodDespawnTime) {
        if(itemSuperFoodDespawnTime > 0) {
            this.ItemSuperFoodDespawnTime = itemSuperFoodDespawnTime;
        } else {
            throw new IllegalArgumentException("The time must be higher than 0");
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
        if(this.advancedOptionsEnabled && this.customServerTickrate > 0 && this.customServerTickrate < 120000) {
            return this.customServerTickrate;
        } else {
            return 50;
        }
    }

    public void setCustomServerTickrate(int customServerTickrate) {
        if(customServerTickrate > 0 && customServerTickrate < 60000) {
            this.customServerTickrate = customServerTickrate;
        }
    }

    public int getCustomServerTickrateIdle() {
        if(this.advancedOptionsEnabled && this.customServerTickrateIdle >= 0 && this.customServerTickrateIdle < 120000) {
            return this.customServerTickrateIdle;
        } else {
            return 950;
        }
    }

    public void setCustomServerTickrateIdle(int customServerTickrateIdle) {
        if(customServerTickrateIdle >= 0 && customServerTickrateIdle < 120000) {
            this.customServerTickrateIdle = customServerTickrateIdle;
        }
    }
}
