package com.chandrawansha.shavin;

public enum DiskType {

    NULL(0),
    WHITE(1),
    BLACK(2),
    WHITE_KING(3),
    BLACK_KING(4);

    private final int diskID;
    private static final String[] diskNames = {
            "Empty",
            "White",
            "Black",
            "White King",
            "Black King"
    };

    DiskType(int diskID){
        this.diskID = diskID;
    }

    @Override
    public String toString() {
        return diskNames[this.diskID];
    }
}
