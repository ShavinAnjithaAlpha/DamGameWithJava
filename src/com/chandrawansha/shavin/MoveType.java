package com.chandrawansha.shavin;

public enum MoveType {

    NORMAL(0),
    CUTTED(1),
    KING_CUTTED(2),
    INVALID(-1);

    private int moveID;

    MoveType(int moveID){
        this.moveID = moveID;
    }
}
