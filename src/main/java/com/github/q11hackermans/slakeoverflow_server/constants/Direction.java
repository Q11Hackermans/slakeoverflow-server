package com.github.q11hackermans.slakeoverflow_server.constants;

public final class Direction {
    public static final int NORTH = 0;
    public static final int SOUTH = 2;
    public static final int EAST = 1;
    public static final int WEST = 3;

    private Direction() {
    }

    public static String toString(int direction) {
        switch (direction) {
            case NORTH:
                return "NORTH";
            case SOUTH:
                return "SOUTH";
            case EAST:
                return "EAST";
            case WEST:
                return "WEST";
            default:
                return "";
        }
    }

    public static boolean isValid(int direction) {
        return direction >= 0 && direction < 4;
    }
}
