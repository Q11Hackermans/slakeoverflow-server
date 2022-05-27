package com.github.q11hackermans.slakeoverflow_server.constants;

public final class AccountPermissionLevel {
    public static final int DEFAULT = 0; // NO PERMISSIONS
    public static final int MODERATOR = 1; // PLAYER MANAGEMENT, CHAT MANAGEMENT
    public static final int ADMIN = 2; // ALL PERMISSIONS

    private AccountPermissionLevel() {}
}
