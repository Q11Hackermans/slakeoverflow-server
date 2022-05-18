package com.github.q11hackermans.slakeoverflow_server.constants;

public final class GameState {
    public static final int STOPPED = 0;
    public static final int PREPARING = 1;
    public static final int RUNNING = 2;
    public static final int PAUSED = 3;

    private GameState() {}

    public static String getString(int gameState) {
        switch(gameState) {
            case GameState.STOPPED:
                return "STOPPED";
            case GameState.PREPARING:
                return "PREPARING";
            case GameState.RUNNING:
                return "RUNNING";
            case GameState.PAUSED:
                return "PAUSED";
            default:
                return "UNKNOWN";
        }
    }
}
