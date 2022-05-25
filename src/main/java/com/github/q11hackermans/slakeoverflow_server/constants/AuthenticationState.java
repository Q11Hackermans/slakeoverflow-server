package com.github.q11hackermans.slakeoverflow_server.constants;

public final class AuthenticationState {
    public static final int UNAUTHENTICATED = 0;
    public static final int PLAYER = 1;
    public static final int SPECTATOR = 2;

    private AuthenticationState() {
    }

    public static String toString(int connectionType) {
        if (connectionType == AuthenticationState.UNAUTHENTICATED) {
            return "UNAUTHENTICATED";
        } else if (connectionType == AuthenticationState.PLAYER) {
            return "PLAYER";
        } else if (connectionType == AuthenticationState.SPECTATOR) {
            return "SPECTATOR";
        } else {
            return "";
        }
    }
}
