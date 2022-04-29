package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.field_components.FieldObject;

public class GameSession {

    private FieldObject[][] gameField;

    public GameSession(int x, int y){

        gameField = new FieldObject[x][y];

    }
}
