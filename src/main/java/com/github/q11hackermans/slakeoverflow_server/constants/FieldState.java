package com.github.q11hackermans.slakeoverflow_server.constants;

public class FieldState {
    // EMPTY
    public static final int EMPTY = 0;

    // OWN PLAYER
    public static final int PLAYER_HEAD_OWN = 101;
    public static final int PLAYER_BODY_OWN = 102;
    //public static final int PLAYER_TAIL_OWN = 103;

    // OTHER PLAYERS
    public static final int PLAYER_HEAD_OTHER = 201;
    public static final int PLAYER_BODY_OTHER = 202;
    //public static final int PLAYER_TAIL_OTHER = 203;

    // ITEMS
    public static final int ITEM_UNKNOWN = 501; // If it is an unknown item
    public static final int ITEM_APPLE = 502; // The default food item
    public static final int ITEM_DEATH_PLAYER_POINTS = 503; // This item will be dropped when a player dies

    private FieldState() {}
}
