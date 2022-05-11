package com.github.q11hackermans.slakeoverflow_server.constants;

import net.jandie1505.connectionmanager.enums.ConnectionBehavior;

public class ConnectionType {
    public static final int UNAUTHORIZED = 0;
    public static final int PLAYER = 1;
    public static final int SPECTATOR = 2;

    private ConnectionType() {}

    public static String toString(int connectionType) {
        if(connectionType == ConnectionType.UNAUTHORIZED) {
            return "UNAUTHORIZED";
        } else if(connectionType == ConnectionType.PLAYER) {
            return "PLAYER";
        } else if(connectionType == ConnectionType.SPECTATOR) {
            return "SPECTATOR";
        } else {
            return "";
        }
    }
}
