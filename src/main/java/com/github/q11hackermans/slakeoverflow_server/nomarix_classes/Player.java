package com.github.q11hackermans.slakeoverflow_server.nomarix_classes;

import java.util.List;
import java.util.UUID;

public class Player implements GameObject {
    private UUID connectionId;
    private int[] position;
    private List<int[]> bodyPositions;
}
