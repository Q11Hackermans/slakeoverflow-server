package com.github.q11hackermans.slakeoverflow_server.config;

import com.github.q11hackermans.slakeoverflow_server.constants.DefaultConfigValues;

import java.nio.charset.StandardCharsets;

public class ServerConfig {
    // SERVER OPTIONS
    private int port;
    private boolean autoConnectionAccept;
    private boolean userAuthentication;
    private boolean unauthenticatePlayerOnDeath;
    private boolean printDebugMessages;
    private int maxConnections;
    private boolean allowGuests;
    private boolean allowLogin;
    private boolean alsoDisablePrivilegedLogin;
    private boolean allowRegistration;
    private String serverName;
    private boolean enableChat;
    private boolean allowGuestChat;
    private boolean enableAdminCommand;
    private boolean printChatToConsole;
    private boolean printChatCommandsToConsole;
    private boolean verboseChatLogs;
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
    private int itemSuperFoodDespawnTime;
    private boolean enableSpectator;
    private int spectatorUpdateInterval;
    private boolean enableSnakeSpeedBoost;
    private boolean eatOwnSnake;
    private double snakeDeathSuperfoodMultiplier;
    private int playingTimeCoinsRewardTime;
    private int playingTimeCoinsRewardSnakeLengthIncrement;
    private int playingTimeCoinsRewardAmount;
    private int foodCoinsRewardAmount;
    private int foodCoinsRewardFoodValueIncrement;
    private int superFoodCoinsRewardAmount;
    private int superFoodCoinsRewardFoodValueIncrement;


    // ADVANCED OPTIONS
    private final boolean advancedOptionsEnabled;
    private boolean overrideServerTickrate;
    private int customServerTickrate;
    private int customServerTickrateIdle;

    public ServerConfig(boolean advancedOptionsEnabled) {
        // SERVER OPTIONS
        this.port = DefaultConfigValues.port;
        this.autoConnectionAccept = DefaultConfigValues.autoConnectionAccept;
        this.userAuthentication = DefaultConfigValues.userAuthentication;
        this.maxConnections = DefaultConfigValues.maxConnections;
        this.allowGuests = DefaultConfigValues.allowGuests;
        this.allowLogin = DefaultConfigValues.allowLogin;
        this.alsoDisablePrivilegedLogin = DefaultConfigValues.alsoDisablePrivilegedLogin;
        this.allowRegistration = DefaultConfigValues.allowRegistration;
        this.serverName = DefaultConfigValues.serverName;
        this.printDebugMessages = DefaultConfigValues.printDebugMessages;
        this.enableChat = DefaultConfigValues.enableChat;
        this.allowGuestChat = DefaultConfigValues.allowGuestChat;
        this.enableAdminCommand = DefaultConfigValues.enableAdminCommand;
        this.printChatToConsole = DefaultConfigValues.printChatToConsole;
        this.printChatCommandsToConsole = DefaultConfigValues.printChatCommandsToConsole;
        this.verboseChatLogs = DefaultConfigValues.verboseChatLogs;

        // GAME OPTIONS
        this.maxPlayers = DefaultConfigValues.maxPlayers;
        this.maxSpectators = DefaultConfigValues.maxSpectators;
        this.minFoodValue = DefaultConfigValues.minFoodValue;
        this.maxFoodValue = DefaultConfigValues.maxFoodValue;
        this.defaultSnakeLength = DefaultConfigValues.defaultSnakeLength;
        this.snakeSpeedBase = DefaultConfigValues.snakeSpeedBase;
        this.snakeSpeedModifierValue = DefaultConfigValues.snakeSpeedModifierValue;
        this.snakeSpeedModifierBodycount = DefaultConfigValues.snakeSpeedModifierBodycount;
        this.defaultGameFieldSizeX = DefaultConfigValues.defaultGameFieldSizeX;
        this.defaultGameFieldSizeY = DefaultConfigValues.defaultGameFieldSizeY;
        this.unauthenticatePlayerOnDeath = DefaultConfigValues.unauthenticatePlayerOnDeath;
        this.itemDefaultDespawnTime = DefaultConfigValues.itemDefaultDespawnTime;
        this.itemSuperFoodDespawnTime = DefaultConfigValues.itemSuperFoodDespawnTime;
        this.enableSpectator = DefaultConfigValues.enableSpectator;
        this.spectatorUpdateInterval = DefaultConfigValues.spectatorUpdateInterval;
        this.enableSnakeSpeedBoost = DefaultConfigValues.enableSnakeSpeedBoost;
        this.eatOwnSnake = DefaultConfigValues.eatOwnSnake;
        this.snakeDeathSuperfoodMultiplier = DefaultConfigValues.snakeDeathSuperfoodMultiplier;
        this.playingTimeCoinsRewardTime = DefaultConfigValues.playingTimeCoinsRewardTime;
        this.playingTimeCoinsRewardSnakeLengthIncrement = DefaultConfigValues.playingTimeCoinsRewardSnakeLengthIncrement;
        this.playingTimeCoinsRewardAmount = DefaultConfigValues.playingTimeCoinsRewardAmount;
        this.foodCoinsRewardAmount = DefaultConfigValues.foodCoinsRewardAmount;
        this.foodCoinsRewardFoodValueIncrement = DefaultConfigValues.foodCoinsRewardFoodValueIncrement;
        this.superFoodCoinsRewardAmount = DefaultConfigValues.superFoodCoinsRewardAmount;
        this.superFoodCoinsRewardFoodValueIncrement = DefaultConfigValues.superFoodCoinsRewardFoodValueIncrement;

        // ADVANCED OPTIONS
        this.advancedOptionsEnabled = advancedOptionsEnabled;
        this.overrideServerTickrate = DefaultConfigValues.overrideServerTickrate;
        this.customServerTickrate = DefaultConfigValues.customServerTickrate;
        this.customServerTickrateIdle = DefaultConfigValues.customServerTickrateIdle;
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
            return 4;
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
            return 0;
        }
    }

    public void setSnakeSpeedModifierValue(int snakeSpeedModifierValue) {
        if (snakeSpeedModifierValue >= 0) {
            this.snakeSpeedModifierValue = snakeSpeedModifierValue;
        } else {
            throw new IllegalArgumentException("The value must be 0 or higher");
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
        return itemSuperFoodDespawnTime;
    }

    public void setItemSuperFoodDespawnTime(int itemSuperFoodDespawnTime) {
        if (itemSuperFoodDespawnTime > 0) {
            this.itemSuperFoodDespawnTime = itemSuperFoodDespawnTime;
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

    public boolean isAllowGuests() {
        return this.allowGuests;
    }

    public void setAllowGuests(boolean allowGuests) {
        this.allowGuests = allowGuests;
    }

    public boolean isAllowLogin() {
        return this.allowLogin;
    }

    public void setAllowLogin(boolean allowLogin) {
        this.allowLogin = allowLogin;
    }

    public boolean isAlsoDisablePrivilegedLogin() {
        return this.alsoDisablePrivilegedLogin;
    }

    public void setAlsoDisablePrivilegedLogin(boolean alsoDisablePrivilegedLogin) {
        this.alsoDisablePrivilegedLogin = alsoDisablePrivilegedLogin;
    }

    public boolean isAllowRegistration() {
        return allowRegistration;
    }

    public void setAllowRegistration(boolean allowRegistration) {
        this.allowRegistration = allowRegistration;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void setServerName(String serverName) {
        byte[] bytes = serverName.getBytes(StandardCharsets.UTF_8);

        String addString = "";
        for(byte b : bytes) {
            addString = addString + (char) b;
        }

        this.serverName = addString;
    }

    public boolean isEnableSnakeSpeedBoost() {
        return this.enableSnakeSpeedBoost;
    }

    public void setEnableSnakeSpeedBoost(boolean enableSnakeSpeedBoost) {
        this.enableSnakeSpeedBoost = enableSnakeSpeedBoost;
    }

    public boolean isEatOwnSnake() {
        return this.eatOwnSnake;
    }

    public void setEatOwnSnake(boolean eatOwnSnake) {
        this.eatOwnSnake = eatOwnSnake;
    }

    public boolean isEnableChat() {
        return enableChat;
    }

    public void setEnableChat(boolean enableChat) {
        this.enableChat = enableChat;
    }

    public boolean isAllowGuestChat() {
        return allowGuestChat;
    }

    public void setAllowGuestChat(boolean allowGuestChatW) {
        this.allowGuestChat = allowGuestChatW;
    }

    public boolean isEnableAdminCommand() {
        return enableAdminCommand;
    }

    public void setEnableAdminCommand(boolean enableAdminCommand) {
        this.enableAdminCommand = enableAdminCommand;
    }

    public double getSnakeDeathSuperfoodMultiplier() {
        if(this.snakeDeathSuperfoodMultiplier <= 2 && this.snakeDeathSuperfoodMultiplier >= 0.01) {
            return this.snakeDeathSuperfoodMultiplier;
        } else {
            return 0.3;
        }
    }

    public void setSnakeDeathSuperfoodMultiplier(double snakeDeathSuperfoodMultiplier) {
        if(snakeDeathSuperfoodMultiplier <= 2 && snakeDeathSuperfoodMultiplier >= 0.01) {
            this.snakeDeathSuperfoodMultiplier = snakeDeathSuperfoodMultiplier;
        } else {
            throw new IllegalArgumentException("The value must be higher or equal than 0.01 and lower or equal than 2");
        }
    }

    public boolean isPrintChatToConsole() {
        return printChatToConsole;
    }

    public void setPrintChatToConsole(boolean printChatToConsole) {
        this.printChatToConsole = printChatToConsole;
    }

    public boolean isPrintChatCommandsToConsole() {
        return printChatCommandsToConsole;
    }

    public void setPrintChatCommandsToConsole(boolean printChatCommandsToConsole) {
        this.printChatCommandsToConsole = printChatCommandsToConsole;
    }

    public boolean isVerboseChatLogs() {
        return verboseChatLogs;
    }

    public void setVerboseChatLogs(boolean verboseChatLogs) {
        this.verboseChatLogs = verboseChatLogs;
    }

    public int getPlayingTimeCoinsRewardTime() {
        return playingTimeCoinsRewardTime;
    }

    public void setPlayingTimeCoinsRewardTime(int playingTimeCoinsRewardTime) {
        this.playingTimeCoinsRewardTime = playingTimeCoinsRewardTime;
    }

    public int getPlayingTimeCoinsRewardSnakeLengthIncrement() {
        return playingTimeCoinsRewardSnakeLengthIncrement;
    }

    public void setPlayingTimeCoinsRewardSnakeLengthIncrement(int playingTimeCoinsRewardSnakeLengthIncrement) {
        this.playingTimeCoinsRewardSnakeLengthIncrement = playingTimeCoinsRewardSnakeLengthIncrement;
    }

    public int getPlayingTimeCoinsRewardAmount() {
        return playingTimeCoinsRewardAmount;
    }

    public void setPlayingTimeCoinsRewardAmount(int playingTimeCoinsRewardAmount) {
        this.playingTimeCoinsRewardAmount = playingTimeCoinsRewardAmount;
    }

    public int getFoodCoinsRewardAmount() {
        return foodCoinsRewardAmount;
    }

    public void setFoodCoinsRewardAmount(int foodCoinsRewardAmount) {
        this.foodCoinsRewardAmount = foodCoinsRewardAmount;
    }

    public int getSuperFoodCoinsRewardAmount() {
        return superFoodCoinsRewardAmount;
    }

    public void setSuperFoodCoinsRewardAmount(int superFoodCoinsRewardAmount) {
        this.superFoodCoinsRewardAmount = superFoodCoinsRewardAmount;
    }

    public int getFoodCoinsRewardFoodValueIncrement() {
        return foodCoinsRewardFoodValueIncrement;
    }

    public void setFoodCoinsRewardFoodValueIncrement(int foodCoinsRewardFoodValueIncrement) {
        this.foodCoinsRewardFoodValueIncrement = foodCoinsRewardFoodValueIncrement;
    }

    public int getSuperFoodCoinsRewardFoodValueIncrement() {
        return superFoodCoinsRewardFoodValueIncrement;
    }

    public void setSuperFoodCoinsRewardFoodValueIncrement(int superFoodCoinsRewardFoodValueIncrement) {
        this.superFoodCoinsRewardFoodValueIncrement = superFoodCoinsRewardFoodValueIncrement;
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
