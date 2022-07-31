package com.chandrawansha.shavin;

import java.util.Objects;

public class Position {

    private int X;
    private int Y;

    public Position(int x, int y) {
        this.X = x;
        this.Y = y;
    }

    public static Position inverse(Position position){
        return new Position(DamModel.BOARD_SIZE - 1 - position.getX(), DamModel.BOARD_SIZE - 1 - position.getY());
    }

    public int getX(){
        return X;
    }

    public int getY() {
        return Y;
    }


    public void setX(int x) {
        this.X = x;
    }

    public void setY(int y) {
        this.Y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return X == position.X && Y == position.Y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(X, Y);
    }

    public boolean compareWithDisk(Disk disk){
        return disk.getX() == this.X && disk.getY() == this.Y;
    }
}
