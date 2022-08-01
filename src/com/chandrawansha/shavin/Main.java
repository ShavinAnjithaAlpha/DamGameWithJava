package com.chandrawansha.shavin;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class Main extends Application {

    // declare constants
    private static final IntegerProperty BOX_SIZE = new SimpleIntegerProperty(75);
    private static final DoubleProperty DISK_SIZE = new SimpleDoubleProperty(0.35);
    private static final String whiteSymbol = "W";
    private static final String blackSymbol = "B";

    private DamModel damModel = new DamModel();
    private Pane leftPane;
    private Pane rightPane;

    private final HashMap<Ellipse, Ellipse> whiteDisks = new HashMap<>();
    private final HashMap<Ellipse, Ellipse> blackDisks = new HashMap<>();

    private ArrayList<Rectangle> layoutBoxes = new ArrayList<>();

    // other nodes declarations
    private Label leftPositionLabel;
    private Label rightPositionLabel;
    private final Rectangle leftHoverBox = new Rectangle(BOX_SIZE.getValue(), BOX_SIZE.getValue());
    private final Rectangle rightHoverBox = new Rectangle(BOX_SIZE.getValue(), BOX_SIZE.getValue());

    private final Rectangle leftValidBox = new Rectangle(BOX_SIZE.getValue(), BOX_SIZE.getValue());
    private final Rectangle rightValidBox = new Rectangle(BOX_SIZE.getValue(), BOX_SIZE.getValue());

    // other game properties
    private final ObservableMap<String, ObjectProperty<Color>> gameColorMap = FXCollections.observableHashMap();


    public static void main(String[] args) {
        // write your code here
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // build the basic dam board in here
        // create two panes using box sizes
        leftPane = new Pane();
        leftPane.minWidthProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        leftPane.minHeightProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        leftPane.maxWidthProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        leftPane.maxHeightProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        leftPane.setId("left-pane");
        // bind the mouse event listeners
        leftPane.setOnMouseMoved(this::leftPaneMouseMoved);

        rightPane = new Pane();
        rightPane.minWidthProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        rightPane.minHeightProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        rightPane.maxWidthProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        rightPane.maxHeightProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        rightPane.setId("right-pane");
        // bind the mouse move event listeners
        rightPane.setOnMouseMoved(this::rightPaneMouseMoved);

        // initiate basic game properties
        initiateSettings();
        // initiate other nodes
        initiate();

        // create hbox for pack theses two panes
        HBox hBox = new HBox(leftPane, rightPane);
        hBox.setSpacing(25);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(Pos.CENTER);

        // create border pane
        final BorderPane borderPane = new BorderPane();
        borderPane.setCenter(hBox);

        // create left and right
        createLeft(borderPane);
        createRight(borderPane);
        // start the new game
        newGame();

        Scene scene = new Scene(borderPane);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        // create new Scene and attach to primary stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dam Game");
        primaryStage.show();
    }

    final private void createLeft(BorderPane borderPane) {
        // create labels for display current position dam board
        leftPositionLabel = new Label();
        leftPositionLabel.setId("left-position-label");

        VBox vBox = new VBox(leftPositionLabel);
        vBox.setPrefWidth(200);
        vBox.setPadding(new Insets(10));
        vBox.setAlignment(Pos.TOP_CENTER);

        borderPane.setLeft(vBox);
    }

    final private void createRight(BorderPane borderPane) {
        // create label for display current position of dam board
        rightPositionLabel = new Label();
        rightPositionLabel.setId("right-position-label");

        VBox vBox = new VBox(rightPositionLabel);
        vBox.setPrefWidth(200);
        vBox.setPadding(new Insets(10));
        vBox.setAlignment(Pos.TOP_CENTER);

        borderPane.setRight(vBox);
    }

    final private void initiate() {
        // set the colors of hover rectangle
        for (Rectangle rectangle : new Rectangle[]{leftHoverBox, rightHoverBox}) {
            rectangle.setStrokeWidth(5);
            rectangle.setStroke(Color.rgb(0, 200, 250, 0.8));
            rectangle.setFill(null);
        }

        for (Rectangle rectangle : new Rectangle[]{leftValidBox, rightValidBox}) {
            rectangle.setFill(Color.rgb(0, 255, 0, 0.1));
            rectangle.setStroke(Color.LAWNGREEN);
            rectangle.setStrokeWidth(5);
            rectangle.setStrokeType(StrokeType.INSIDE);
        }

        leftPane.getChildren().addAll(leftHoverBox);
        rightPane.getChildren().addAll(rightHoverBox);
    }

    private final void initiateSettings() {
        gameColorMap.put("WHITE-BOX", new SimpleObjectProperty<>(Color.WHITE));
        gameColorMap.put("BLACK-BOX", new SimpleObjectProperty<>(Color.BLACK));
        gameColorMap.put("WHITE-DISK", new SimpleObjectProperty<>(Color.DARKBLUE));
        gameColorMap.put("BLACK-DISK", new SimpleObjectProperty<>(Color.ORANGERED));

    }

    private final void drawLayout() {
        // first clear the layout
        for (Rectangle rectangle : layoutBoxes) {
            leftPane.getChildren().remove(rectangle);
        }
        layoutBoxes.clear();

        for (Pane pane : new Pane[]{leftPane, rightPane}) {
            // draw board layout with standard colors
            ObjectProperty<Color> color = gameColorMap.get("WHITE-BOX");
            for (int i = 0; i < damModel.BOARD_SIZE; i++) {
                for (int j = 0; j < damModel.BOARD_SIZE; j++) {
                    // draw the rectangles
                    Rectangle rectangle = new Rectangle(j * BOX_SIZE.getValue(), i * BOX_SIZE.getValue(),
                            BOX_SIZE.getValue(), BOX_SIZE.getValue());
                    rectangle.fillProperty().bind(color);
                    rectangle.setStrokeWidth(0);
                    // append to box list
                    layoutBoxes.add(rectangle);
                    // add to pane
                    pane.getChildren().add(rectangle);
                    // flip the color to other color
                    color = color == gameColorMap.get("WHITE-BOX") ? gameColorMap.get("BLACK-BOX") : gameColorMap.get("WHITE-BOX");

                }
                color = color == gameColorMap.get("WHITE-BOX") ? gameColorMap.get("BLACK-BOX") : gameColorMap.get("WHITE-BOX");
            }
            // draw margin of board
            Rectangle marginRect = new Rectangle(0, 0, pane.getPrefWidth(), pane.getPrefHeight());
            marginRect.setFill(null);
            marginRect.setStrokeWidth(15);
            marginRect.setStroke(Color.RED);

//            pane.getChildren().add(marginRect);
        }
    }

    final private void newGame() {
        // first clear the two panes
        leftPane.getChildren().clear();
        rightPane.getChildren().clear();

        // draw the layout
        drawLayout();

        // initiate dam model new game
        damModel.newGame();
        // then create disk rendering objects using dam model
        // first draw white disks
        int i = 0;
        for (Disk whiteDisk : damModel.getWhiteSideDisks()) {
            // create two disk for board and mirror board
            Ellipse activeDisk = createDisk(whiteDisk, Side.WHITE, true);
            Ellipse mirrorDisk = createDisk(whiteDisk, Side.BLACK, false);
            // set user data
            activeDisk.setUserData(String.format("%s.%d", whiteSymbol, i));
            mirrorDisk.setUserData(String.format("%s.%d", whiteSymbol, i));

            // add to disk map
            whiteDisks.put(activeDisk, mirrorDisk);
            // add to left and right board panes
            leftPane.getChildren().add(activeDisk);
            rightPane.getChildren().add(mirrorDisk);

            i++;
        }

        i = 0;
        for (Disk blackDisk : damModel.getBlackSideDisks()) {
            // create two ellipse for active board and mirror board
            Ellipse activeDisk = createDisk(blackDisk, Side.BLACK, true);
            Ellipse mirrorDisk = createDisk(blackDisk, Side.WHITE, false);
            // set user data
            activeDisk.setUserData(String.format("%s.%d", blackSymbol, i));
            mirrorDisk.setUserData(String.format("%s.%d", blackSymbol, i));

            // add to the disk map
            blackDisks.put(activeDisk, mirrorDisk);
            // add to left and right board panes
            rightPane.getChildren().add(activeDisk);
            leftPane.getChildren().add(mirrorDisk);

            i++;
        }

    }

    final private Ellipse createDisk(Disk disk, Side side, boolean active) {
        // create new Ellipse
        Ellipse diskImage = new Ellipse();
        diskImage.radiusXProperty().bind(BOX_SIZE.multiply(DISK_SIZE));
        diskImage.radiusYProperty().bind(BOX_SIZE.multiply(DISK_SIZE));

        // apply the position to disk Image
        if (disk.getDiskType() == DiskType.WHITE && side == Side.WHITE || (disk.getDiskType() == DiskType.BLACK && side == Side.BLACK)) {
            bindPosition(diskImage, disk.getPosition());
        } else {
            bindPosition(diskImage, Position.inverse(disk.getPosition()));
        }

        // set colors and strokes
        if (disk.getDiskType() == DiskType.WHITE) {
            diskImage.fillProperty().bind(gameColorMap.get("WHITE-DISK"));
        } else if (disk.getDiskType() == DiskType.BLACK) {
            diskImage.fillProperty().bind(gameColorMap.get("BLACK-DISK"));
        }
        diskImage.setStroke(null);

        // bind the mouse handlers
        if (active) {
            diskImage.setOnMouseDragged(this::diskDragged);
            diskImage.setOnMouseReleased(this::mouseReleased);
            diskImage.setOnDragExited(this::dragReleased);
            diskImage.setOnMouseMoved(this::diskHover);
            diskImage.setOnMouseExited(this::diskExit);
        }
        // apply effect
        DropShadow shadow = new DropShadow(20, Color.rgb(0, 0, 0, 0.8));
        diskImage.setEffect(shadow);

        return diskImage;

    }

    final private void bindPosition(Ellipse disk, Position position) {
        // bind the center X and Y coordinates to ellipse object
        // calculate pane relative coordinates of disk
        int X = position.getX();
        int Y = DamModel.BOARD_SIZE - 1 - position.getY();

        // now bind the property
        disk.centerXProperty().bind(BOX_SIZE.multiply(X + 0.5));
        disk.centerYProperty().bind(BOX_SIZE.multiply(Y + 0.5));
    }


    final private Position getCurrentPosition(MouseEvent event) {
        // calculate the position using the coordinates
        double x = (event.getX() / BOX_SIZE.intValue());
        int X;
        if (x < 0) {
            X = -1;
        } else {
            X = (int) x;
        }
        int Y = (int) (leftPane.getHeight() - event.getY()) / BOX_SIZE.intValue();

        return new Position(X, Y);
    }

    final private Position getCurrentPosition(DragEvent event) {
        // calculate the position using the coordinates
        double x = (event.getX() / BOX_SIZE.intValue());
        int X;
        if (x < 0) {
            X = -1;
        } else {
            X = (int) x;
        }
        int Y = (int) (leftPane.getHeight() - event.getY()) / BOX_SIZE.intValue();

        return new Position(X, Y);
    }

    final private void leftPaneMouseMoved(MouseEvent event) {
        Position currentPosition = getCurrentPosition(event);
        if (!currentPosition.isValid()) {
            return;
        }

        leftPositionLabel.setText(String.format("X : %d%nY : %d", currentPosition.getX(), currentPosition.getY()));

        leftHoverBox.setX(currentPosition.getX() * BOX_SIZE.getValue());
        leftHoverBox.setY((DamModel.BOARD_SIZE - 1 - currentPosition.getY()) * BOX_SIZE.getValue());
        if (!leftPane.getChildren().contains(leftHoverBox)) {
            leftPane.getChildren().add(leftHoverBox);
        }
    }

    final private void rightPaneMouseMoved(MouseEvent event) {
        // calculate the position using the coordinates
        Position currentPosition = getCurrentPosition(event);
        if (!currentPosition.isValid()) {
            return;
        }

        rightPositionLabel.setText(String.format("X : %d%nY : %d", currentPosition.getX(), currentPosition.getY()));

        rightHoverBox.setX(currentPosition.getX() * BOX_SIZE.getValue());
        rightHoverBox.setY((DamModel.BOARD_SIZE - 1 - currentPosition.getY()) * BOX_SIZE.getValue());

        if (!rightPane.getChildren().contains(rightHoverBox)) {
            rightPane.getChildren().add(rightHoverBox);
        }
    }

    final private void diskHover(MouseEvent event) {
        Ellipse diskImage = (Ellipse) event.getSource();
        if (diskImage != null && checkSide(diskImage)) {
            diskImage.setStrokeWidth(4);
            diskImage.setStroke(Color.DARKSALMON);
        }
    }

    final private void diskExit(MouseEvent event) {
        Ellipse ellipse = (Ellipse) event.getSource();
        if (ellipse != null) {
            ellipse.setStroke(null);
            ellipse.setStrokeWidth(0);
        }
    }

    final private void diskDragged(MouseEvent event) {
        // get position
        Position position = getCurrentPosition(event);
        if (position.isValid()) {
            // get disk image
            Ellipse diskImage = (Ellipse) event.getSource();
            Ellipse mirrorImage = null;
            // check if in the right board
            if (damModel.getCurrentSide() == Side.WHITE && whiteDisks.containsKey(diskImage)) {
                mirrorImage = whiteDisks.get(diskImage);
            } else if (damModel.getCurrentSide() == Side.BLACK && blackDisks.containsKey(diskImage)) {
                mirrorImage = blackDisks.get(diskImage);
            } else {
                return;
            }

            // set new position
            diskImage.centerXProperty().unbind();
            diskImage.centerYProperty().unbind();
            mirrorImage.centerXProperty().unbind();
            mirrorImage.centerYProperty().unbind();

            diskImage.setCenterX(event.getX());
            diskImage.setCenterY(event.getY());
            mirrorImage.setCenterX(BOX_SIZE.getValue() * DamModel.BOARD_SIZE - event.getX());
            mirrorImage.setCenterY(BOX_SIZE.get() * DamModel.BOARD_SIZE - event.getY());

            setValidBox(diskImage, position);

            diskImage.toFront();
            mirrorImage.toFront();
        }
    }

    final private void mouseReleased(MouseEvent event) {
        // first get position
        Position currentPosition = getCurrentPosition(event);
        Ellipse activeDisk = (Ellipse) event.getSource();

        if (activeDisk != null) {
            if (checkSide(activeDisk)) {
                setDiskBehavior(currentPosition, activeDisk);
            }
        }
        // hide valid box from board
        leftValidBox.setVisible(false);
        rightValidBox.setVisible(false);
    }

    final private void dragReleased(DragEvent event) {
        // first get position
        Position currentPosition = getCurrentPosition(event);
        Ellipse activeDisk = (Ellipse) event.getSource();

        if (activeDisk != null) {
            if (checkSide(activeDisk)) {
                setDiskBehavior(currentPosition, activeDisk);
            }
        }
        // hide valid box from board
        leftValidBox.setVisible(false);
        rightValidBox.setVisible(false);
    }

    final private void setDiskBehavior(Position currentPosition, Ellipse activeDisk) {

        if (currentPosition.isValid()) {
            // validate position
            if (damModel.isEmptyPosition(currentPosition, damModel.getCurrentSide())) {
                // next check if the new position is valid position
                MoveType diskMove = damModel.move(damModel.getDisk((String) activeDisk.getUserData()), currentPosition);
                if (diskMove == MoveType.INVALID) {
                    bindAgain(activeDisk);
                } else if (diskMove == MoveType.NORMAL) {
                    // set new position
                    bindAgainForMove(activeDisk);
                } else if (diskMove == MoveType.CUTTED) {
                    // set new position and remove unwanted disk
                    bindAgain(activeDisk);
                    // hide the removed disk
                    int index = damModel.getDiskIndex(damModel.getLastRemovedDisk().getPosition(),
                            damModel.getCurrentSide() == Side.WHITE ? Side.BLACK : Side.WHITE);
                    Ellipse removedDisk = null;
                    Ellipse mirrorRemovedDisk = null;
                    if (damModel.getCurrentSide() == Side.WHITE) {
                        removedDisk = getDiskImageByIndex(blackDisks, index);
                        mirrorRemovedDisk = blackDisks.get(removedDisk);
                    } else {
                        removedDisk = getDiskImageByIndex(whiteDisks, index);
                        mirrorRemovedDisk = whiteDisks.get(removedDisk);
                    }
                    removedDisk.setVisible(false);
                    mirrorRemovedDisk.setVisible(false);

                } else {
                    bindAgain(activeDisk);
                    // king cutting
                    System.out.println("King Move");
                }
            } else {
                bindAgain(activeDisk);
            }
        } else {
            bindAgain(activeDisk);
        }
    }

    private void bindAgain(Ellipse activeDisk) {

        Ellipse mirrorDisk = null;
        // disk bind again to the previous data
        if (damModel.getCurrentSide() == Side.WHITE) {
            mirrorDisk = whiteDisks.get(activeDisk);
        } else {
            mirrorDisk = blackDisks.get(activeDisk);
        }

        // get Model Disk object relative to disk image
        Disk disk = damModel.getDisk((String) activeDisk.getUserData());

        // now bind the disk images
        bindPosition(activeDisk, disk.getPosition());
        bindPosition(mirrorDisk, Position.inverse(disk.getPosition()));
    }

    private void bindAgainForMove(Ellipse activeDisk) {
        Ellipse mirrorDisk = null;
        // disk bind again to the previous data
        if (damModel.getCurrentSide() == Side.WHITE) {
            mirrorDisk = blackDisks.get(activeDisk);
        } else {
            mirrorDisk = whiteDisks.get(activeDisk);
        }

        // get Model Disk object relative to disk image
        Disk disk = damModel.getDisk((String) activeDisk.getUserData());
        System.out.println("disk prev position : " + disk.getPosition());

        // now bind the disk images
        bindPosition(activeDisk, disk.getPosition());
        bindPosition(mirrorDisk, Position.inverse(disk.getPosition()));
    }

    private boolean checkSide(Ellipse disk) {
        if (damModel.getCurrentSide() == Side.WHITE && whiteDisks.containsKey(disk)) {
            return true;
        } else if (damModel.getCurrentSide() == Side.BLACK && blackDisks.containsKey(disk)) {
            return true;
        }
        return false;
    }

    private Ellipse getDiskImageByIndex(HashMap<Ellipse, Ellipse> diskMap, int index) {
        int i = 0;
        for (Ellipse diskImage : diskMap.keySet()) {
            if (index == getIndex(diskImage)) {
                return diskImage;
            }
        }
        return null;
    }

    private int getIndex(Ellipse diskImage) {
        return Integer.parseInt((((String) diskImage.getUserData()).split("[.]"))[1]);
    }

    private void setValidBox(Ellipse diskImage, Position position) {
        //
        if (damModel.determineValid(damModel.getDisk((String) diskImage.getUserData()), position)) {
            if (damModel.getCurrentSide() == Side.WHITE) {
                setBoxPosition(leftValidBox, position);
                setBoxPosition(rightValidBox, Position.inverse(position));
            } else {
                setBoxPosition(rightValidBox, position);
                setBoxPosition(leftValidBox, Position.inverse(position));
            }
            // set as visible
            leftValidBox.setVisible(true);
            rightValidBox.setVisible(true);

            // add to the panes
            if (!leftPane.getChildren().contains(leftValidBox)) {
                leftPane.getChildren().add(leftValidBox);
            }
            if (!rightPane.getChildren().contains(rightValidBox)) {
                rightPane.getChildren().add(rightValidBox);
                ;
            }
        } else {
            // hide the rectangles
            leftValidBox.setVisible(false);
            rightValidBox.setVisible(false);
        }
    }

    private void setBoxPosition(Rectangle box, Position position) {
        // bind the center X and Y coordinates to ellipse object
        // calculate pane relative coordinates of disk
        int X = position.getX();
        int Y = DamModel.BOARD_SIZE - 1 - position.getY();

        // now bind the property
        box.setX(BOX_SIZE.getValue() * X);
        box.setY(BOX_SIZE.getValue() * Y);
    }
}
