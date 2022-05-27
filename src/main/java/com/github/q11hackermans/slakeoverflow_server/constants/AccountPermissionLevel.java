package com.github.q11hackermans.slakeoverflow_server.constants;

public final class AccountPermissionLevel {
    public static final int DEFAULT = 0; // NO PERMISSIONS
    public static final int MODERATOR = 1; // PLAYER MANAGEMENT, CHAT MANAGEMENT
    public static final int ADMIN = 2; // ALL PERMISSIONS

    private AccountPermissionLevel() {}

    public static String toString(int permissionLevel) {
        if(permissionLevel == DEFAULT) {
            return "DEFAULT (" + DEFAULT + ")";
        } else if(permissionLevel == MODERATOR) {
            return "MODERATOR (" + MODERATOR + ")";
        } else if(permissionLevel == ADMIN) {
            return "ADMIN (" + ADMIN + ")";
        } else {
            return "UNKNOWN";
        }
    }
}
