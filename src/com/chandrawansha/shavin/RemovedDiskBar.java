package com.chandrawansha.shavin;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class RemovedDiskBar extends Pane {

    private double currentPosition = 0;
    private double height = space + diskRadius;
    private final ArrayList<Ellipse> diskArray = new ArrayList<>();
    private final ObservableMap<String, ObjectProperty<Color>> colorMap;

    private final static double diskRadius = 30;
    private final static double space = 10;

    public RemovedDiskBar(ObservableMap<String, ObjectProperty<Color>> colorMap) {
        super();
        this.colorMap = colorMap;
        setPrefHeight(300);

        // create rectangle to fill the pane
        final Rectangle fillRectangle = new Rectangle();
        fillRectangle.setFill(Color.SADDLEBROWN);
        fillRectangle.widthProperty().bind(widthProperty());
        fillRectangle.heightProperty().bind(heightProperty());
        fillRectangle.setX(0);
        fillRectangle.setY(0);

        getChildren().add(fillRectangle);
        fillRectangle.toBack();
    }

    public void addDisk(DiskType disk) {
        // create disk image
        currentPosition += (space + diskRadius);
        Ellipse diskImage = new Ellipse(currentPosition, height, diskRadius, diskRadius);

        if (disk == DiskType.WHITE) {
            diskImage.fillProperty().bind(colorMap.get("WHITE-DISK"));
        } else if (disk == DiskType.BLACK) {
            diskImage.fillProperty().bind(colorMap.get("BLACK-DISK"));
        } else if (disk == DiskType.WHITE_KING) {
            diskImage.fillProperty().bind(colorMap.get("WHITE-KING"));
        } else {
            diskImage.fillProperty().bind(colorMap.get("BLACK-KING"));
        }

        diskImage.setEffect(new DropShadow(15, Color.BLACK));
        getChildren().add(diskImage);
        // add to the list
        diskArray.add(diskImage);

        if (diskArray.size() >= 10)
            height += (2 * diskRadius + space);
        currentPosition += (diskRadius + space);
    }

    public void clear() {
        diskArray.forEach((Ellipse diskImage) -> getChildren().remove(diskImage));
        // clear the disk array
        diskArray.clear();
        currentPosition = 0;
        height = space + diskRadius;
    }
}
