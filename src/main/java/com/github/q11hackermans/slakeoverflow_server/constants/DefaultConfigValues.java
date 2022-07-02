package com.github.q11hackermans.slakeoverflow_server.constants;

public final class DefaultConfigValues {
    // SERVER OPTIONS
    public static final int port = 26677;
    public static final boolean autoConnectionAccept = true;
    public static final boolean userAuthentication = true;
    public static final boolean unauthenticatePlayerOnDeath = true;
    public static final boolean printDebugMessages = false;
    public static final int maxConnections = 20;
    public static final boolean allowGuests = true;
    public static final boolean allowLogin = true;
    public static final boolean alsoDisablePrivilegedLogin = false;
    public static final boolean allowRegistration = true;
    public static final String serverName = "Slakeoverflow-Server";
    public static final boolean enableChat = true;
    public static final boolean allowGuestChat = false;
    public static final boolean enableAdminCommand = false;
    public static final boolean printChatToConsole = true;
    public static final boolean printChatCommandsToConsole = true;
    public static final boolean verboseChatLogs = false;
    // GAME OPTIONS
    public static final int maxPlayers = 20;
    public static final int maxSpectators = 2;
    public static final int minFoodValue = 1;
    public static final int maxFoodValue = 2;
    public static final int defaultSnakeLength = 3;
    public static final int snakeSpeedBase = 2;
    public static final int snakeSpeedModifierValue = 1;
    public static final int snakeSpeedModifierBodycount = 30;
    public static final int defaultGameFieldSizeX = 100;
    public static final int defaultGameFieldSizeY = 100;
    public static final int itemDefaultDespawnTime = 60;
    public static final int itemSuperFoodDespawnTime = 120;
    public static final boolean enableSpectator = true;
    public static final int spectatorUpdateInterval = 200;
    public static final boolean enableSnakeSpeedBoost = true;
    public static final boolean eatOwnSnake = true;
    public static final double snakeDeathSuperfoodMultiplier = 0.3;
    public static final int playingTimeCoinsRewardTime = 36000;
    public static final int playingTimeCoinsRewardSnakeLengthIncrement = 1;
    public static final int playingTimeCoinsRewardAmount = 10;
    public static final int foodCoinsRewardAmount = 10;
    public static final int foodCoinsRewardFoodValueIncrement = 1;
    public static final int superFoodCoinsRewardAmount = 50;
    public static final int superFoodCoinsRewardFoodValueIncrement = 1;
    // ADVANCED OPTIONS
    public static final boolean overrideServerTickrate = false;
    public static final int customServerTickrate = 50;
    public static final int customServerTickrateIdle = 950;

    private DefaultConfigValues() {}
}
