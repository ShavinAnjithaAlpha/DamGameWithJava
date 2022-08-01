package com.chandrawansha.shavin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.input.*;
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

public class Main extends Application {

    // declare constants
    private static final IntegerProperty BOX_SIZE = new SimpleIntegerProperty(70);
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

    private final Rectangle leftIndicator = new Rectangle();
    private final Rectangle rightIndicator = new Rectangle();

    private RemovedDiskBar leftDiskBar;
    private RemovedDiskBar rightDiskBar;

    // other game properties
    private final ObservableMap<String, ObjectProperty<Color>> gameColorMap = FXCollections.observableHashMap();


    public static void main(String[] args) {
        // write your code here
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // create border pane
        final BorderPane borderPane = new BorderPane();

        // create left and right
        createCenter(borderPane);
        createTop(borderPane);
        createLeft(borderPane);
        createRight(borderPane);

        // initiate basic game properties
        initiateSettings();
        // initiate other nodes
        initiate();

        // start the new game
        newGame();

        Scene scene = new Scene(borderPane);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        // create new Scene and attach to primary stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dam Game");
        primaryStage.show();
    }

    final private void createCenter(BorderPane borderPane) {
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

        // create hbox for pack theses two panes
        HBox hBox = new HBox(leftPane, rightPane);
        hBox.setSpacing(25);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(Pos.CENTER);

        // current board indicator bars
        leftIndicator.widthProperty().bind(leftPane.widthProperty());
        rightIndicator.widthProperty().bind(rightPane.widthProperty());

        for (Rectangle indicator : new Rectangle[]{leftIndicator, rightIndicator}) {
            indicator.setHeight(25);
            indicator.setArcWidth(20);
            indicator.setArcHeight(20);

            indicator.setEffect(new DropShadow(5, Color.GREY));

            updateIndicators();
        }
        HBox hBox1 = new HBox(leftIndicator, rightIndicator);
        hBox1.setAlignment(Pos.TOP_CENTER);
        hBox1.setSpacing(25);

        // crate disk bar
        leftDiskBar = new RemovedDiskBar(gameColorMap);
        rightDiskBar = new RemovedDiskBar(gameColorMap);

        leftDiskBar.prefWidthProperty().bind(leftPane.widthProperty());
        rightDiskBar.prefWidthProperty().bind(rightPane.widthProperty());

        HBox hBox2 = new HBox(leftDiskBar, rightDiskBar);
        hBox2.setSpacing(25);
        hBox2.setAlignment(Pos.TOP_CENTER);

        VBox vBox = new VBox(hBox, hBox1, hBox2);
        vBox.setSpacing(20);

        borderPane.setCenter(vBox);
    }

    final private void createTop(BorderPane borderPane) {

        // create the menu bar
        final MenuBar menuBar = new MenuBar(createFileMenu(), createEditMenu());
        borderPane.setTop(menuBar);
    }

    final private Menu createFileMenu() {
        // create new game item
        final MenuItem newGameAction = new MenuItem("New Game");
        newGameAction.setMnemonicParsing(true);
        newGameAction.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHIFT_DOWN));
        newGameAction.setOnAction((event -> newGame()));

        final MenuItem exitAction = new MenuItem("Exit");
        exitAction.setMnemonicParsing(true);
        exitAction.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));
        exitAction.setOnAction((event -> Platform.exit()));

        final Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(newGameAction, exitAction);
        return fileMenu;
    }

    final private Menu createEditMenu() {

        // create board size menu sliders
        final MenuItem boxSizeMenu = new MenuItem("Box Size");
        final Slider boxSizeSlider = new Slider();
        boxSizeSlider.setMin(30);
        boxSizeSlider.setMax(100);
        boxSizeSlider.valueProperty().bindBidirectional(BOX_SIZE);
        boxSizeSlider.setShowTickLabels(true);
        boxSizeMenu.setGraphic(boxSizeSlider);

        final MenuItem diskSizeMenu = new MenuItem("Disk Size");
        final Slider diskSizeSlider = new Slider();
        diskSizeSlider.setMin(0);
        diskSizeSlider.setMax(100);
        diskSizeSlider.setValue(85);
        DISK_SIZE.bind(diskSizeSlider.valueProperty().divide(200));
        diskSizeSlider.setShowTickLabels(true);
        diskSizeMenu.setGraphic(diskSizeSlider);

        final Menu sizeMenu = new Menu("Size");
        sizeMenu.getItems().addAll(boxSizeMenu, diskSizeMenu);

        final Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(sizeMenu);

        return editMenu;
    }

    final private void createLeft(BorderPane borderPane) {
        // create labels for display current position dam board
        leftPositionLabel = new Label();
        leftPositionLabel.setId("left-position-label");

        VBox vBox = new VBox(leftPositionLabel);
        vBox.setPrefWidth(200);
        vBox.setPadding(new Insets(10));
        vBox.setAlignment(Pos.TOP_CENTER);

        // create titled pane
        TitledPane titledPane = new TitledPane();
        titledPane.getStyleClass().add("static-group");
        titledPane.setText("Left Side Statics");
        titledPane.setContent(vBox);

        borderPane.setLeft(titledPane);
    }

    final private void createRight(BorderPane borderPane) {
        // create label for display current position of dam board
        rightPositionLabel = new Label();
        rightPositionLabel.setId("right-position-label");

        VBox vBox = new VBox(rightPositionLabel);
        vBox.setPrefWidth(200);
        vBox.setPadding(new Insets(10));
        vBox.setAlignment(Pos.TOP_CENTER);

        // create titled pane
        TitledPane titledPane = new TitledPane();
        titledPane.getStyleClass().add("static-group");
        titledPane.setText("Right Side Statics");
        titledPane.setContent(vBox);

        borderPane.setRight(titledPane);
    }
    // end of UI of the game


    final private void initiate() {
        // set the colors of hover rectangle
        for (Rectangle rectangle : new Rectangle[]{leftHoverBox, rightHoverBox}) {
            rectangle.setStrokeWidth(5);
            rectangle.setStroke(Color.rgb(0, 200, 250, 0.8));
            rectangle.setFill(null);
            rectangle.widthProperty().bind(BOX_SIZE);
            rectangle.heightProperty().bind(BOX_SIZE);
        }

        for (Rectangle rectangle : new Rectangle[]{leftValidBox, rightValidBox}) {
            rectangle.setFill(Color.rgb(0, 255, 0, 0.1));
            rectangle.setStroke(Color.LAWNGREEN);
            rectangle.setStrokeWidth(5);
            rectangle.setStrokeType(StrokeType.INSIDE);
            // set the size properties
            rectangle.widthProperty().bind(BOX_SIZE);
            rectangle.heightProperty().bind(BOX_SIZE);
        }

        leftPane.getChildren().addAll(leftHoverBox);
        rightPane.getChildren().addAll(rightHoverBox);

        // add listeners to king property changes
        damModel.getKingProperty().addListener(new ChangeListener<Disk>() {
            @Override
            public void changed(ObservableValue<? extends Disk> observable, Disk oldValue, Disk newValue) {
                // set the disk image as king image
                // first get the disk image
                int diskIndex = damModel.getDiskIndex(newValue.getPosition(), damModel.getCurrentSide());
                Ellipse kingDisk = null;
                Ellipse mirrorKingDisk = null;
                if (damModel.getCurrentSide() == Side.WHITE) {
                    kingDisk = getDiskImageByIndex(whiteDisks, diskIndex);
                    mirrorKingDisk = whiteDisks.get(kingDisk);

                    kingDisk.fillProperty().bind(gameColorMap.get("WHITE-KING"));
                    mirrorKingDisk.fillProperty().bind(gameColorMap.get("WHITE-KING"));
                } else {
                    kingDisk = getDiskImageByIndex(blackDisks, diskIndex);
                    mirrorKingDisk = blackDisks.get(kingDisk);

                    kingDisk.fillProperty().bind(gameColorMap.get("BLACK-KING"));
                    mirrorKingDisk.fillProperty().bind(gameColorMap.get("BLACK-KING"));
                }

            }
        });

        // add listeners to side property of the dam model
        damModel.currentSideProperty().addListener(((observable, oldValue, newValue) -> {
            updateIndicators();
        }));
    }

    private final void initiateSettings() {
        gameColorMap.put("WHITE-BOX", new SimpleObjectProperty<>(Color.WHITE));
        gameColorMap.put("BLACK-BOX", new SimpleObjectProperty<>(Color.BLACK));
        gameColorMap.put("WHITE-DISK", new SimpleObjectProperty<>(Color.DARKBLUE));
        gameColorMap.put("BLACK-DISK", new SimpleObjectProperty<>(Color.ORANGERED));
        gameColorMap.put("WHITE-KING", new SimpleObjectProperty<Color>(Color.GOLD));
        gameColorMap.put("BLACK-KING", new SimpleObjectProperty<Color>(Color.MEDIUMPURPLE));

    }
    // end of the settings side of the game


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
                    Rectangle rectangle = new Rectangle();
                    rectangle.widthProperty().bind(BOX_SIZE);
                    rectangle.heightProperty().bind(BOX_SIZE);
                    rectangle.xProperty().bind(BOX_SIZE.multiply(j));
                    rectangle.yProperty().bind(BOX_SIZE.multiply(i));
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
            activeDisk.setUserData(whiteDisk);
            mirrorDisk.setUserData(whiteDisk);

            whiteDisk.setDiskImages(activeDisk, mirrorDisk); // set the disk images to disk object

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
            activeDisk.setUserData(blackDisk);
            mirrorDisk.setUserData(blackDisk);

            blackDisk.setDiskImages(activeDisk, mirrorDisk); // set disk images for disk object

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
        } else if (disk.getDiskType() == DiskType.WHITE_KING) {
            diskImage.fillProperty().bind(gameColorMap.get("WHITE-KING"));
        } else {
            diskImage.fillProperty().bind(gameColorMap.get("BLACK-KING"));
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

    // mouse event listeners for disk
    final private void leftPaneMouseMoved(MouseEvent event) {
        Position currentPosition = getCurrentPosition(event);
        if (!currentPosition.isValid()) {
            return;
        }

        leftPositionLabel.setText(String.format("X : %d%nY : %d", currentPosition.getX(), currentPosition.getY()));

        setBoxPosition(leftHoverBox, currentPosition);
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

        setBoxPosition(rightHoverBox, currentPosition);
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
            if (checkSide(diskImage)) {
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

                setValidBox(diskImage, position); // show the valid places by highlight the suitable places
                // bring the these two disks to front of the boards
                diskImage.toFront();
                mirrorImage.toFront();
            }
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
            if (damModel.isBlankPosition(currentPosition, damModel.getCurrentSide())) {
                // next check if the new position is valid position
                MoveType diskMove = damModel.move((Disk) activeDisk.getUserData(), currentPosition);

//                bindAgain(activeDisk);

                if (diskMove == MoveType.CUTTED) {
//                    // hide the removed disk
//                    int index = damModel.getDiskIndex(damModel.getLastRemovedDisk().getPosition(),
//                            damModel.getCurrentSide() == Side.WHITE ? Side.BLACK : Side.WHITE);
//                    Ellipse removedDisk = null;
//                    Ellipse mirrorRemovedDisk = null;
//                    if (damModel.getCurrentSide() == Side.WHITE) {
//                        removedDisk = getDiskImageByIndex(blackDisks, index);
//                        mirrorRemovedDisk = blackDisks.get(removedDisk);
//
//                        rightDiskBar.addDisk(DiskType.BLACK);
//                    } else {
//                        removedDisk = getDiskImageByIndex(whiteDisks, index);
//                        mirrorRemovedDisk = whiteDisks.get(removedDisk);
//
//                        leftDiskBar.addDisk(DiskType.WHITE);
//                    }
//                    removedDisk.setVisible(false);
//                    mirrorRemovedDisk.setVisible(false);

                    damModel.getLastRemovedDisk().getActiveDisk().setVisible(false);
                    damModel.getLastRemovedDisk().getMirrorDisk().setVisible(false);


                }
                else if (diskMove == MoveType.KING_CUTTED) {
                    // king cutting
                    System.out.println("King Move");
                }
            }
        }
        bindAgain(activeDisk);
    }



    private void bindAgain(Ellipse activeDisk) {

        Ellipse mirrorDisk = ((Disk) activeDisk.getUserData()).getMirrorDisk();
        // get Model Disk object relative to disk image
        Disk disk = (Disk) activeDisk.getUserData();

        // now bind the disk images
        bindPosition(activeDisk, disk.getPosition());
        bindPosition(mirrorDisk, disk.getPosition().inverse());
    }

//    private void bindAgainForMove(Ellipse activeDisk) {
//        Ellipse mirrorDisk = ((Disk) activeDisk.getUserData()).getMirrorDisk();
//        // get Model Disk object relative to disk image
//        Disk disk = damModel.getDisk((String) activeDisk.getUserData());
//
//        // now bind the disk images
//        bindPosition(activeDisk, disk.getPosition());
//        bindPosition(mirrorDisk, disk.getPosition().inverse());
//    }

    private boolean checkSide(Ellipse disk) {
        if (damModel.getCurrentSide() == Side.WHITE && whiteDisks.containsKey(disk)) {
            return true;
        } else if (damModel.getCurrentSide() == Side.BLACK && blackDisks.containsKey(disk)) {
            return true;
        }
        return false;
    }

    // methods for other game features
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

    private void updateIndicators() {
        if (damModel.getCurrentSide() == Side.WHITE) {
            leftIndicator.setFill(Color.GREEN);
            rightIndicator.setFill(Color.RED);
        } else {
            leftIndicator.setFill(Color.RED);
            rightIndicator.setFill(Color.GREEN);
        }
    }
}
