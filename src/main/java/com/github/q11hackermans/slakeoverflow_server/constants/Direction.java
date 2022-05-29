package com.github.q11hackermans.slakeoverflow_server.constants;

public final class Direction {
    public static final int NORTH = 0;
    public static final int SOUTH = 2;
    public static final int EAST = 1;
    public static final int WEST = 3;
    // public static final int SPEED = 4; DO NOT USE, IS USED IN CLIENT FOR SENDING SPEED BOOST

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
            //case SPEED:
            //    return "SPEED";
            default:
                return "";
        }
    }

    public static int getOpposite(int direction) {
        switch (direction) {
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case WEST:
                return EAST;
            case EAST:
                return WEST;
            default:
                return -1;
        }
    }

    public static boolean isValid(int direction) {
        return direction >= 0 && direction < 4;
    }
}
