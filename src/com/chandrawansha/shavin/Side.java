package com.chandrawansha.shavin;

public enum Side {

    WHITE(1),
    BLACK(2);

    private int sideIndex;

    Side(int index){
        this.sideIndex = index;
    }

    @Override
    public String toString() {
        if (sideIndex == 1){
            return "WHITE SIDE";
        }
        else{
            return "BLACK SIDE";
        }
    }
}
