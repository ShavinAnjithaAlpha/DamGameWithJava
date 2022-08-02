package com.chandrawansha.shavin;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

import java.util.Arrays;

public class DamModel {

    // declare the dam board constants
    public static final int BOARD_SIZE = 10;
    public static final int DISK_COUNT = 30;

    // create dam model disk structure
    private final Disk[] whiteSideDisks = new Disk[DISK_COUNT];
    private final Disk[] blackSideDisks = new Disk[DISK_COUNT];

    private Side firstMoveSide = Side.WHITE;
    private SimpleObjectProperty<Side> currentSide = new SimpleObjectProperty<>(firstMoveSide);
    private ListProperty<Disk> lastRemovedDisk = new SimpleListProperty<>(FXCollections.observableArrayList());
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

        currentSide.set(firstMoveSide);
    }

    public Disk[] getWhiteSideDisks(){
        return whiteSideDisks;
    }

    public Disk[] getBlackSideDisks(){ return blackSideDisks; }

    public Side getCurrentSide(){
        return currentSideProperty().getValue();
    }

    public ObjectProperty<Side> currentSideProperty(){
        return currentSide;
    }


    public boolean determineValid(Disk disk, Position position){
        if (position.isValid()){
            if (isBlankPosition(position, getCurrentSide())){

                // now check move types
                if (checkForNormalMove(disk, position)){
                    return true;
                }

                else if (disk.getDiskType() == DiskType.WHITE_KING || disk.getDiskType() == DiskType.BLACK_KING){
                    // check for king move
                    return checkForKingCut(disk, position, getCurrentSide());
                }

                else if (position.getY() == disk.getY() + 2){

                    // check for cut move
                    Position newPosition;
                    if (position.getX() == disk.getX() + 2)
                        newPosition = new Position(disk.getX() + 1, disk.getY() + 1);
                    else if (position.getX() == disk.getX() - 2)
                        newPosition = new Position(disk.getX() - 1, disk.getY() + 1);
                    else
                        return false;

                    if (getCurrentSide() == Side.WHITE){
                        if (getDisk(newPosition, Side.WHITE) != null)
                            return false;
                        if (getDisk(newPosition.inverse(), Side.BLACK) == null)
                            return false;
                        else
                            return true;
                    }
                    else{
                        if (getDisk(newPosition, Side.BLACK) != null)
                            return false;
                        if (getDisk(newPosition.inverse(), Side.WHITE) == null)
                            return false;
                        else
                            return true;
                    }
                }

            }
        }
        return false;
    }

    public boolean checkForKingCut(Disk disk, Position position, Side side){
        if (Math.abs(disk.getX() - position.getX()) == Math.abs(disk.getY() - position.getY())) {
            // now check if same side disk in there
            // now performe checking using iterating
            int c = 1;
            int x = disk.getX(), y = disk.getY();
            while (c < Math.abs(position.getX() - disk.getX())) {
                if (position.getX() > disk.getX())
                    x++;
                else {
                    x--;
                }

                if (position.getY() > disk.getY())
                    y++;
                else
                    y--;

                // get the disk
                Disk checkedDisk = getDisk(new Position(x, y), side);
                if (checkedDisk != null && checkedDisk.getDiskType() != DiskType.NULL) {
                    return false;
                }
                c++;
            }
            return true;
        }
        return false;
    }

    public boolean checkForNormalMove(Disk disk, Position position){

        return (position.getY() == disk.getY() + 1 && (position.getX() == disk.getX() + 1 || position.getX()  == disk.getX() - 1));
    }

    public boolean isBlankPosition(Position position, Side side){
        if (side == Side.WHITE){
            // first check for white disks
            for (Disk disk : whiteSideDisks){
                if (position.compareWithDisk(disk) && disk.getDiskType() != DiskType.NULL){
                    return false;
                }
            }

            // then check for black disks
            Position inversePosition = Position.inverse(position);
            for (Disk disk : blackSideDisks){
                if (inversePosition.compareWithDisk(disk)  && disk.getDiskType() != DiskType.NULL){
                    return false;
                }
            }
        }
        else{
            // first check for white disks
            for (Disk disk : blackSideDisks){
                if (position.compareWithDisk(disk)  && disk.getDiskType() != DiskType.NULL){
                    return false;
                }
            }

            // then check for black disks
            for (Disk disk : whiteSideDisks){
                Position inversePosition = Position.inverse(position);
                if (inversePosition.compareWithDisk(disk)  && disk.getDiskType() != DiskType.NULL){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean processNormalCut(Disk disk, Position position, Side side){
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
                    if (newPosition.compareWithDisk(whiteDisk) && whiteDisk.getDiskType() != DiskType.NULL){
                        return false;
                    }
                }

                Position inversePosition = Position.inverse(newPosition);
                for (Disk blackDisk : blackSideDisks){
                    if (inversePosition.compareWithDisk(blackDisk) && blackDisk.getDiskType() != DiskType.NULL){
                        // process the cutting
                        blackDisk.setDiskType(DiskType.NULL); // set as the removed disk from the board
                        lastRemovedDisk.add(blackDisk); // set the last removed disk
                        disk.setPosition(position); // set new position
                        return true;
                    }
                }
            }
            else{
                // check relative to the black side
                for (Disk blackDisk : blackSideDisks){
                    if (newPosition.compareWithDisk(blackDisk) && blackDisk.getDiskType() != DiskType.NULL){
                        return false;
                    }
                }

                Position inversePosition = Position.inverse(newPosition);
                for (Disk whiteDisk : whiteSideDisks){
                    if (inversePosition.compareWithDisk(whiteDisk) && disk.getDiskType() != DiskType.NULL){
                        // process the cutting
                        whiteDisk.setDiskType(DiskType.NULL); // set as the removed disk from the board
                        lastRemovedDisk.add(whiteDisk); // set last removed disk
                        disk.setPosition(position); // set new position for disk
                        return true;
                    }
                }
            }
        }
        return false;

    }

    public void processKingCutting(Disk disk, Position position, Side side){
        int diff = Math.abs(position.getX() - disk.getX());
        int c = 1;
        int x = disk.getX();
        int y = disk.getY();
        boolean removed = false;
        while (c < diff){
            if (position.getX() > disk.getX())
                x++;
            else
                x--;

            if (position.getY() > disk.getY())
                y++;
            else
                y--;

            // get disk from position
            Disk removedDisk = getDisk(new Position(x, y).inverse(), side == Side.WHITE ? Side.BLACK : Side.BLACK);
            if (removedDisk != null){
                removed = true;
                removedDisk.setDiskType(DiskType.NULL);
                lastRemovedDisk.add(removedDisk);
            }
            c++;

        }
        if (!removed){
            currentSide.set(getCurrentSide() == Side.WHITE ? Side.BLACK : Side.WHITE);
        }
        disk.setPosition(position);

    }

    public boolean makeKing(Disk disk, Side side){
        if (disk.getY() == BOARD_SIZE - 1){
            if (side == Side.WHITE){
                disk.setDiskType(DiskType.WHITE_KING);
            }
            else{
                disk.setDiskType(DiskType.BLACK_KING);
            }
            return true;
        }
       return false;
    }

    public MoveType move(Disk disk, Position position){
        // first check for emptiness
        if (!isBlankPosition(position, getCurrentSide())){
            return MoveType.INVALID;
        }

        // now check for normal move
        if (checkForNormalMove(disk, position)){
            disk.setPosition(position);
            // check for making king
            if (makeKing(disk, getCurrentSide())){
                kingProperty.set(disk);
            }
            // change the side
            currentSide.set(getCurrentSide() == Side.WHITE ? Side.BLACK : Side.WHITE);
            return MoveType.NORMAL;
        }

        // in last, check for king cutting
        if (checkForKingCut(disk, position, getCurrentSide())  && (disk.getDiskType() == DiskType.WHITE_KING || disk.getDiskType() == DiskType.BLACK_KING)){
            // performe the king cut operation
            processKingCutting(disk, position, getCurrentSide());
            return MoveType.KING_CUTTED;
        }

        // now check for the cut move
        else if (processNormalCut(disk, position, getCurrentSide())){
            // check for making king
            if (makeKing(disk, getCurrentSide())){
                kingProperty.set(disk);
            }
            return MoveType.CUTTED;
        }
        return MoveType.INVALID;
    }


    public ListProperty<Disk> lastRemovedDiskProperty(){
        return lastRemovedDisk;
    }

    public ObjectProperty<Disk> getKingProperty(){
        return kingProperty;
    }

    public Disk getDisk(Position position, Side side){
        Disk[] array;
        if (side == Side.WHITE)
            array = whiteSideDisks;
        else
            array = blackSideDisks;

        for (Disk disk : array){
            if (position.compareWithDisk(disk) && disk.getDiskType() != DiskType.NULL)
                return disk;
        }

        return null;
    }


    // statics data methods
    public int getDiskCount(DiskType diskType){
        return (int) Arrays.stream(whiteSideDisks).filter((Disk disk) -> {
           return disk.getDiskType() == diskType;
        }).count() +
                (int) Arrays.stream(blackSideDisks).filter((Disk disk) -> {
                    return disk.getDiskType() == diskType;
                }).count();
    }

    public int getRemainWhiteDiskCount(){
        return getDiskCount(DiskType.WHITE);
    }

    public int getRemainBlackDiskCount(){
        return getDiskCount(DiskType.BLACK);
    }

    public int getWhiteKingCount(){
        return getDiskCount(DiskType.WHITE_KING);
    }

    public int getBlackKingCount(){
        return getDiskCount(DiskType.BLACK_KING);
    }

    public int getRemovedWhiteDiskCount(){
        return BOARD_SIZE - getWhiteKingCount() - getRemainWhiteDiskCount();
    }

    public int getRemovedBlackDiskCount(){
        return BOARD_SIZE - getBlackKingCount() - getRemainBlackDiskCount();
    }
}
