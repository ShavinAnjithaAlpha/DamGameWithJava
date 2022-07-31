package com.chandrawansha.shavin;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Arrays;

public class DamModel {

    // declare the dam board constants
    public static final int BOARD_SIZE = 10;
    public static final int DISK_COUNT = 30;

    // create dam model disk structure
    private final Disk[] whiteSideDisks = new Disk[DISK_COUNT];
    private final Disk[] blackSideDisks = new Disk[DISK_COUNT];

    private Side firstMoveSide = Side.WHITE;
    private Side currentSide;
    private Disk lastRemovedDisk;
    private ObjectProperty<Disk> kingProperty = new SimpleObjectProperty<>();

    public void newGame(){
        // initiate the disks positions
        for (int i = 0; i < DISK_COUNT; i++){
            try {
                // fill the board with new disk with new layout
                whiteSideDisks[i] = new Disk(DiskType.WHITE, i % BOARD_SIZE, i / BOARD_SIZE);
                blackSideDisks[i] = new Disk(DiskType.BLACK, i % BOARD_SIZE, i / BOARD_SIZE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currentSide = firstMoveSide;
    }

    public Disk[] getWhiteSideDisks(){
        return whiteSideDisks;
    }

    public Disk[] getBlackSideDisks(){
        return blackSideDisks;
    }

    public Side getCurrentSide(){
        return currentSide;
    }

    // method for extract the appropriate disk object from the disk arrays
    public Disk getDisk(String tag){
        // split the string and extract id of the disk
        String side = tag.split("[.]")[0];
        int id = Integer.parseInt(tag.split("[.]")[1]);

        if (side.equals("W")){
            return whiteSideDisks[id];
        }
        else{
            return blackSideDisks[id];
        }
    }

    public boolean isEmptyPosition(Position position, Side side){
        if (side == Side.WHITE){
            // firs check for white disks
            for (Disk disk : whiteSideDisks){
                if (position.compareWithDisk(disk)){
                    return false;
                }
            }

            // then check for black disks
            Position inversePosition = Position.inverse(position);
            for (Disk disk : blackSideDisks){
                if (inversePosition.compareWithDisk(disk)){
                    return false;
                }
            }
        }
        else{
            // firs check for white disks
            for (Disk disk : blackSideDisks){
                if (position.compareWithDisk(disk)){
                    return false;
                }
            }

            // then check for black disks
            for (Disk disk : whiteSideDisks){
                Position inversePosition = Position.inverse(position);
                if (inversePosition.compareWithDisk(disk)){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkForCutting(Disk disk, Position position, Side side){
        // first construct the intermidiate position object that peforme the cutting
        if (position.getY() == disk.getY() + 2){
            Position newPosition = null;
            if (position.getX() == disk.getX() + 2){
                newPosition = new Position(disk.getX() + 1, disk.getY() + 1);
            }
            else if (position.getX() == disk.getX() - 2){
                newPosition = new Position(disk.getX() - 1, disk.getY() + 1);
            }
            else{
                return false;
            }

            if (side == Side.WHITE){
                // check relative to the white side
                for (Disk whiteDisk : whiteSideDisks){
                    if (newPosition.compareWithDisk(whiteDisk)){
                        return false;
                    }
                }

                Position inversePosition = Position.inverse(newPosition);
                for (Disk blackDisk : blackSideDisks){
                    if (inversePosition.compareWithDisk(blackDisk)){
                        // process the cutting
                        blackDisk.setDiskType(DiskType.NULL); // set as the removed disk from the board
                        lastRemovedDisk = blackDisk; // set the last removed disk
                        return true;
                    }
                }
            }
            else{
                // check relative to the black side
                for (Disk blackDisk : blackSideDisks){
                    if (newPosition.compareWithDisk(blackDisk)){
                        return false;
                    }
                }

                Position inversePosition = Position.inverse(newPosition);
                for (Disk whiteDisk : whiteSideDisks){
                    if (inversePosition.compareWithDisk(whiteDisk)){
                        // process the cutting
                        whiteDisk.setDiskType(DiskType.NULL); // set as the removed disk from the board
                        lastRemovedDisk = whiteDisk; // set last removed disk
                        return true;
                    }
                }
            }
        }
        return false;

    }

    public boolean checkForKingCutting(Disk disk, Position position, Side side){
        return false;
    }

    public boolean makeKing(Disk disk, Side side){
        if (disk.getY() == BOARD_SIZE - 1){
            if (side == Side.WHITE){
                disk.setDiskType(DiskType.WHITE_KING);
            }
            else{
                disk.setDiskType(DiskType.BLACK_KING);
            }
            // change side again
            currentSide = currentSide == Side.WHITE ? Side.BLACK : Side.WHITE;
            return true;
        }
       return false;
    }

    public MoveType move(Disk disk, Position position){
        // first check for emptyness
        if (!isEmptyPosition(position, currentSide)){
            return MoveType.INVALID;
        }
        System.out.println("empty write");
        // now check for normal move
        System.out.println(position);
        System.out.println(disk.getPosition());
        if ((position.getY() == disk.getY() + 1 && position.getX() == disk.getX() + 1)
                || (position.getY() == disk.getY() + 1 && position.getX() == disk.getX() - 1)){
            disk.setPosition(position);
            // change the side
            currentSide = currentSide == Side.WHITE ? Side.BLACK : Side.WHITE;

            // check for making king
            if (makeKing(disk, currentSide)){
                kingProperty.set(disk);
            }
            return MoveType.NORMAL;
        }

        // now check for the cutted move
        if (checkForCutting(disk, position, currentSide)){
            // check for making king
            if (makeKing(disk, currentSide)){
                kingProperty.set(disk);
            }
            return MoveType.CUTTED;
        }
        // in last, check for king cutting
        if (checkForKingCutting(disk, position, currentSide)){
            return MoveType.KING_CUTTED;
        }
        return MoveType.INVALID;
    }

    public int getDiskIndex(Position position){
        if (!position.isValid()){
            return -1;
        }

        if (currentSide == Side.WHITE){
            for (int i = 0; i < whiteSideDisks.length; i++){
                if (position.compareWithDisk(whiteSideDisks[i])){
                    return i;
                }
            }
        }
        else{
            for (int i = 0; i < blackSideDisks.length; i++){
                if (position.compareWithDisk(blackSideDisks[i])){
                    return i;
                }
            }
        }
        return -1;
    }

}
