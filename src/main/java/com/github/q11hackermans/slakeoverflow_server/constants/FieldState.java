package com.github.q11hackermans.slakeoverflow_server.constants;

public final class FieldState {
    // EMPTY
    public static final int EMPTY = 0;

    // BORDER
    public static final int BORDER = 1;

    // OWN PLAYER
    public static final int PLAYER_HEAD_OWN_NORTH = 101;
    public static final int PLAYER_HEAD_OWN_SOUTH = 103;
    public static final int PLAYER_HEAD_OWN_EAST = 102;
    public static final int PLAYER_HEAD_OWN_WEST = 104;
    public static final int PLAYER_BODY_OWN = 105;
    //public static final int PLAYER_TAIL_OWN = 103;

    // OTHER PLAYERS
    public static final int PLAYER_HEAD_OTHER_NORTH = 201;
    public static final int PLAYER_HEAD_OTHER_SOUTH = 203;
    public static final int PLAYER_HEAD_OTHER_EAST = 202;
    public static final int PLAYER_HEAD_OTHER_WEST = 204;
    public static final int PLAYER_BODY_OTHER = 205;
    //public static final int PLAYER_TAIL_OTHER = 203;

    // ITEMS
    public static final int ITEM_UNKNOWN = 501; // If it is an unknown item
    public static final int ITEM_FOOD = 502; // The default food item
    public static final int ITEM_SUPER_FOOD = 503; // This item will be dropped when a player dies

    private FieldState() {
    }

    public static int getPlayerHeadOwnValue(int facing) {
        if (facing == Direction.NORTH) {
            return PLAYER_HEAD_OWN_NORTH;
        } else if (facing == Direction.SOUTH) {
            return PLAYER_HEAD_OWN_SOUTH;
        } else if (facing == Direction.EAST) {
            return PLAYER_HEAD_OWN_EAST;
        } else if (facing == Direction.WEST) {
            return PLAYER_HEAD_OWN_WEST;
        } else {
            return 0;
        }
    }

    public static int getPlayerHeadOtherValue(int facing) {
        if (facing == Direction.NORTH) {
            return PLAYER_HEAD_OTHER_NORTH;
        } else if (facing == Direction.SOUTH) {
            return PLAYER_HEAD_OTHER_SOUTH;
        } else if (facing == Direction.EAST) {
            return PLAYER_HEAD_OTHER_EAST;
        } else if (facing == Direction.WEST) {
            return PLAYER_HEAD_OTHER_WEST;
        } else {
            return 0;
        }
    }
}
