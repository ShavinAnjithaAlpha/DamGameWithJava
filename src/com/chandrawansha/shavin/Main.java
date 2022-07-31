package com.chandrawansha.shavin;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.awt.*;
import java.util.ArrayList;

public class Main extends Application {

    // declare constants
    private static final IntegerProperty BOX_SIZE = new SimpleIntegerProperty(75);
    private static final DoubleProperty DISK_SIZE = new SimpleDoubleProperty(0.35);
    private static final String whiteSymbol = "W";
    private static final String blackSymbol = "B";

    private DamModel damModel = new DamModel();
    private Pane leftPane;
    private Pane rightPane;

    private Ellipse[] leftDisks = new Ellipse[DamModel.DISK_COUNT * 2];
    private Ellipse[] rightDisks = new Ellipse[DamModel.DISK_COUNT * 2];

    private ArrayList<Rectangle> layoutBoxes = new ArrayList<>();
    private Shape currentDisk;

    // other nodes declaretions
    private Label leftPositionLabel;
    private Label rightPositionLabel;
    private final Rectangle leftHoverBox = new Rectangle(BOX_SIZE.getValue(), BOX_SIZE.getValue());
    private final Rectangle rightHoverBox = new Rectangle(BOX_SIZE.getValue(), BOX_SIZE.getValue());

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
        leftPane.prefWidthProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        leftPane.prefHeightProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        // bind the mouse event listeners
        leftPane.setOnMouseMoved(this::leftPaneMouseMoved);

        rightPane = new Pane();
        rightPane.prefWidthProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        rightPane.prefHeightProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
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
            rectangle.setStroke(Color.rgb(0, 150, 250, 0.7));
            rectangle.setFill(null);
        }

        leftPane.getChildren().add(leftHoverBox);
        rightPane.getChildren().add(rightHoverBox);
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

            pane.getChildren().add(marginRect);
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
        for (Disk disk : damModel.getWhiteSideDisks()) {
            // first right pane disk
            Ellipse rightDisk = getDisk(disk, disk.getPosition());
            Ellipse leftDisk = getDisk(disk, Position.inverse(disk.getPosition()));

            leftDisk.setUserData(String.format("%s.%d", whiteSymbol, i));
            rightDisk.setUserData(String.format("%s.%d", whiteSymbol, i));
            // add to lists
            leftDisks[i] = leftDisk;
            rightDisks[i] = rightDisk;

            leftPane.getChildren().add(leftDisk);
            rightPane.getChildren().add(rightDisk);

            i++;
        }

        i = 0;
        for (Disk disk : damModel.getBlackSideDisks()) {
            // first right pane disk
            Ellipse leftDisk = getDisk(disk, disk.getPosition());
            Ellipse rightDisk = getDisk(disk, Position.inverse(disk.getPosition()));

            leftDisk.setUserData(String.format("%s.%d", blackSymbol, i));
            rightDisk.setUserData(String.format("%s.%d", blackSymbol, i));
            // add to lists
            leftDisks[i + DamModel.DISK_COUNT] = leftDisk;
            rightDisks[i + DamModel.DISK_COUNT] = rightDisk;

            leftPane.getChildren().add(leftDisk);
            rightPane.getChildren().add(rightDisk);

            i++;
        }
    }

    final private Ellipse getDisk(Disk disk, Position position) {
        // create new ellipse object
        Ellipse ellipse = new Ellipse();
        ellipse.radiusXProperty().bind(DISK_SIZE.multiply(BOX_SIZE));
        ellipse.radiusYProperty().bind(DISK_SIZE.multiply(BOX_SIZE));

        ellipse.centerXProperty().bind(BOX_SIZE.multiply(position.getX() + 0.5));
        ellipse.centerYProperty().bind(BOX_SIZE.multiply(position.getY() + 0.5));

        if (disk.getDiskType() == DiskType.WHITE) {
            ellipse.fillProperty().bind(gameColorMap.get("WHITE-DISK"));
        } else if (disk.getDiskType() == DiskType.BLACK) {
            ellipse.fillProperty().bind(gameColorMap.get("BLACK-DISK"));
        }

        ellipse.setEffect(new DropShadow(10, Color.BLACK));
//        ellipse.setEffect(new Blend(BlendMode.HARD_LIGHT));

        // set the mouse event listeners
        ellipse.setOnMouseDragged(this::diskDragged);
        ellipse.setOnMouseReleased(this::diskReleased);
        ellipse.setOnMouseDragReleased(this::diskReleased);
        return ellipse;
    }

    final private Position getCurrentPosition(MouseEvent event) {
        // calculate the position using the coordinates
        int X = (int) event.getX() / BOX_SIZE.intValue();
        int Y = (int) (leftPane.getHeight() - event.getY()) / BOX_SIZE.intValue();

        return new Position(X, Y);
    }

    final private Position getCurrentPosition(DragEvent event){
        // calculate the position using the coordinates
        int X = (int) event.getX() / BOX_SIZE.intValue();
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

    final private Ellipse getMirrorDisk(Ellipse disk){

        String diskData = (String) disk.getUserData();
        if (damModel.getCurrentSide() == Side.WHITE){
           for (Ellipse other : rightDisks){
               if (other != null) {
                   String otherData = (String) other.getUserData();
                   if (otherData.equals(diskData)){
                       return other;
                   }
               }
           }
        }
        else{
           for (Ellipse other : leftDisks){
               if (other != null) {
                   String otherData = (String) other.getUserData();
                   if (otherData.equals(diskData)){
                       return other;
                   }
               }
           }
        }
        return null;
    }

    final private void diskDragged(MouseEvent event) {

        if (event.getSource() != null) {
            Ellipse disk = (Ellipse) event.getSource();
            // check if valid action
            if ((damModel.getCurrentSide() == Side.WHITE && leftPane.getChildren().contains(disk))
                || (damModel.getCurrentSide() == Side.BLACK && rightPane.getChildren().contains(disk))){
                    disk.centerXProperty().unbind();
                    disk.centerYProperty().unbind();
                    disk.setCenterX(event.getX());
                    disk.setCenterY(event.getY());

                    // move the other side disk
                    Ellipse otherDisk = getMirrorDisk(disk);
                    if (otherDisk != null){
                        otherDisk.centerXProperty().unbind();
                        otherDisk.centerYProperty().unbind();
                        otherDisk.setCenterX(BOX_SIZE.intValue() * DamModel.BOARD_SIZE - event.getX());
                        otherDisk.setCenterY(BOX_SIZE.getValue() * DamModel.BOARD_SIZE - event.getY());
                    }
            }
        }
    }

    final private void diskReleased(MouseEvent event){
        if (event.getSource() != null) {
            // get current position
            Position position = getCurrentPosition(event);
            // get event source ellipse
            Ellipse diskImage = (Ellipse) event.getSource();

            if (position.isValid()){
                // check if the valid positions
                MoveType move = damModel.move(damModel.getDisk((String) diskImage.getUserData()), position);
                if (move == MoveType.INVALID){
                    // first get mapped disk object relate to Ellipse
                    bindAgain(diskImage);
                    // get mirror disk and bind again
                    bindMirrorAgain(getMirrorDisk(diskImage));
                }
                else{
                    //
                }
            }
            else{
                // bind disk center x and y property again to previous states
                // first get mapped disk object relate to Ellipse
                bindAgain(diskImage);
                // get mirror disk and bind again
                bindMirrorAgain(getMirrorDisk(diskImage));
            }
        }
    }

    final private void bindAgain(Ellipse diskImage){
        Disk disk = damModel.getDisk((String) diskImage.getUserData());

        if (damModel.getCurrentSide() == Side.WHITE){
            // bind black disk
            if (disk.getDiskType() == DiskType.BLACK || disk.getDiskType() == DiskType.BLACK_KING){
                diskImage.centerXProperty().bind(BOX_SIZE.multiply(disk.getX() + 0.5));
                diskImage.centerYProperty().bind(BOX_SIZE.multiply(disk.getY() + 0.5));
            }
            // bind white disk
            else{
                Position newPosition = Position.inverse(disk.getPosition());
                diskImage.centerXProperty().bind(BOX_SIZE.multiply(newPosition.getX() + 0.5));
                diskImage.centerYProperty().bind(BOX_SIZE.multiply(newPosition.getY() + 0.5));
            }
        }
        else{
            if (disk.getDiskType() == DiskType.WHITE || disk.getDiskType() == DiskType.WHITE_KING){
                diskImage.centerXProperty().bind(BOX_SIZE.multiply(disk.getX() + 0.5));
                diskImage.centerYProperty().bind(BOX_SIZE.multiply(disk.getY() + 0.5));
            }
            else{
                Position newPosition = Position.inverse(disk.getPosition());
                diskImage.centerXProperty().bind(BOX_SIZE.multiply(newPosition.getX() + 0.5));
                diskImage.centerYProperty().bind(BOX_SIZE.multiply(newPosition.getY() + 0.5));
            }
        }
    }

    final private void bindMirrorAgain(Ellipse diskImage){
        Disk disk = damModel.getDisk((String) diskImage.getUserData());

        if (damModel.getCurrentSide() == Side.BLACK){
            // bind black disk
            if (disk.getDiskType() == DiskType.BLACK || disk.getDiskType() == DiskType.BLACK_KING){
                diskImage.centerXProperty().bind(BOX_SIZE.multiply(disk.getX() + 0.5));
                diskImage.centerYProperty().bind(BOX_SIZE.multiply(disk.getY() + 0.5));
            }

        }
        else{
            if (disk.getDiskType() == DiskType.WHITE || disk.getDiskType() == DiskType.WHITE_KING){
                diskImage.centerXProperty().bind(BOX_SIZE.multiply(disk.getX() + 0.5));
                diskImage.centerYProperty().bind(BOX_SIZE.multiply(disk.getY() + 0.5));
            }

        }
    }
}
