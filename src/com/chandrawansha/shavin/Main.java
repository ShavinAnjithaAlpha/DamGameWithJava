package com.chandrawansha.shavin;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

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

    // other nodes declaretions


    // other game properties
    private final ObservableMap<String, ObjectProperty<Color>> gameColorMap = FXCollections.observableHashMap();


    public static void main(String[] args) {
        // write your code here
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // initiate basic game properties
        initiateSettings();
        // build the basic dam board in here
        // create two panes using box sizes
        leftPane = new Pane();
        leftPane.prefWidthProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        leftPane.prefHeightProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));

        rightPane = new Pane();
        rightPane.prefWidthProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));
        rightPane.prefHeightProperty().bind(BOX_SIZE.multiply(damModel.BOARD_SIZE));

        // create hbox for pack theses two panes
        HBox hBox = new HBox(leftPane, rightPane);
        hBox.setSpacing(25);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(Pos.CENTER);

        // create border pane
        final BorderPane borderPane = new BorderPane();
        borderPane.setCenter(hBox);
        newGame();

        // create new Scene and attach to primary stage
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.setTitle("Dam Game");
        primaryStage.show();
    }

    final private void createLeft(BorderPane borderPane){
        // create labels for display current position dam board

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
            marginRect.setStroke(Color.BLACK);

            pane.getChildren().add(marginRect);
        }
    }

    final private void newGame(){
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
        for (Disk disk : damModel.getWhiteSideDisks()){
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
        for (Disk disk : damModel.getBlackSideDisks()){
            // first right pane disk
            Ellipse leftDisk = getDisk(disk, disk.getPosition());
            Ellipse rightDisk = getDisk(disk, Position.inverse(disk.getPosition()));

            leftDisk.setUserData(String.format("%s.%d", blackSymbol, i));
            rightDisk.setUserData(String.format("%s.%d", blackSymbol, i));
            // add to lists
            leftDisks[i] = leftDisk;
            rightDisks[i] = rightDisk;

            leftPane.getChildren().add(leftDisk);
            rightPane.getChildren().add(rightDisk);

            i++;
        }
    }

    final private Ellipse getDisk(Disk disk, Position position){
        // create new ellipse object
        Ellipse ellipse = new Ellipse();
        ellipse.radiusXProperty().bind(DISK_SIZE.multiply(BOX_SIZE));
        ellipse.radiusYProperty().bind(DISK_SIZE.multiply(BOX_SIZE));

        ellipse.centerXProperty().bind(BOX_SIZE.multiply(position.getX() + 0.5));
        ellipse.centerYProperty().bind(BOX_SIZE.multiply(position.getY() + 0.5));

        if (disk.getDiskType() == DiskType.WHITE){
            ellipse.fillProperty().bind(gameColorMap.get("WHITE-DISK"));
        }
        else if (disk.getDiskType() == DiskType.BLACK){
            ellipse.fillProperty().bind(gameColorMap.get("BLACK-DISK"));
        }

        ellipse.setEffect(new DropShadow(10, Color.BLACK));
//        ellipse.setEffect(new Blend(BlendMode.HARD_LIGHT));

        return ellipse;
    }
}