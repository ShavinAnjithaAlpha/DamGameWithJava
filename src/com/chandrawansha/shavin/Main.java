package com.chandrawansha.shavin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.HashMap;

public class Main extends Application {

    // declare constants
    private static final IntegerProperty BOX_SIZE = new SimpleIntegerProperty(70);
    private static final DoubleProperty DISK_SIZE = new SimpleDoubleProperty(0.35);
    private static final String whiteSymbol = "W";
    private static final String blackSymbol = "B";

    private final DamModel damModel = new DamModel();
    private Pane leftPane;
    private Pane rightPane;

    private final HashMap<Ellipse, Ellipse> whiteDisks = new HashMap<>();
    private final HashMap<Ellipse, Ellipse> blackDisks = new HashMap<>();

    private final ArrayList<Rectangle> layoutBoxes = new ArrayList<>();

    // other nodes declarations
    private Label leftPositionLabel;
    private Label rightPositionLabel;
    private final HashMap<String, Label> staticLabels = new HashMap<>();

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
    public void start(Stage primaryStage){
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

        Scene scene = new Scene(borderPane, Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
        scene.getStylesheets().add(getClass().getResource("style.css").toString());

        // create new Scene and attach to primary stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dam Game");
        primaryStage.show();
    }

    private void createCenter(BorderPane borderPane) {
        // build the basic dam board in here
        // create two panes using box sizes
        leftPane = new Pane();
        leftPane.minWidthProperty().bind(BOX_SIZE.multiply(DamModel.BOARD_SIZE));
        leftPane.minHeightProperty().bind(BOX_SIZE.multiply(DamModel.BOARD_SIZE));
        leftPane.maxWidthProperty().bind(BOX_SIZE.multiply(DamModel.BOARD_SIZE));
        leftPane.maxHeightProperty().bind(BOX_SIZE.multiply(DamModel.BOARD_SIZE));
        leftPane.setId("left-pane");
        // bind the mouse event listeners
        leftPane.setOnMouseMoved(this::leftPaneMouseMoved);

        rightPane = new Pane();
        rightPane.minWidthProperty().bind(BOX_SIZE.multiply(DamModel.BOARD_SIZE));
        rightPane.minHeightProperty().bind(BOX_SIZE.multiply(DamModel.BOARD_SIZE));
        rightPane.maxWidthProperty().bind(BOX_SIZE.multiply(DamModel.BOARD_SIZE));
        rightPane.maxHeightProperty().bind(BOX_SIZE.multiply(DamModel.BOARD_SIZE));
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
        vBox.setSpacing(0);

        borderPane.setCenter(vBox);
    }

    private void createTop(BorderPane borderPane) {

        // create the menu bar
        final MenuBar menuBar = new MenuBar(createFileMenu(), createEditMenu());
        borderPane.setTop(menuBar);
    }

    private Menu createFileMenu() {
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

    private Menu createEditMenu() {

        final MenuItem colorMenuItem = new MenuItem("Color Settings");
        colorMenuItem.setOnAction((event -> createColorDialog()));

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
        editMenu.getItems().addAll(colorMenuItem, sizeMenu);

        return editMenu;
    }

    void createLeft(BorderPane borderPane) {
        // create labels for display current position dam board
        leftPositionLabel = new Label();
        leftPositionLabel.setId("left-position-label");

        // create other statics labels
        Label diskCountLabel = new Label();
        diskCountLabel.getStyleClass().add("static-label");

        Label kingCountLabel = new Label();
        kingCountLabel.getStyleClass().add("static-label");

        Label removedCountLabel = new Label();
        removedCountLabel.getStyleClass().add("static-label");

        staticLabels.put("WHITE", diskCountLabel);
        staticLabels.put("WHITE-KING", kingCountLabel);
        staticLabels.put("BLACK-REMOVE", removedCountLabel);

        VBox vBox = new VBox(
                new Label("Position"),
                leftPositionLabel,
                new Label("Current Disks"),
                diskCountLabel,
                new Label("Current Kings"),
                kingCountLabel,
                new Label("Removed Disk"),
                removedCountLabel
        );
        vBox.setSpacing(5);
        vBox.setAlignment(Pos.TOP_LEFT);

        // create titled pane
        TitledPane titledPane = new TitledPane();
        titledPane.getStyleClass().add("static-group");
        titledPane.setText("Left Side Statics");
        titledPane.setContent(vBox);

        borderPane.setLeft(titledPane);
    }

    private void createRight(BorderPane borderPane) {
        // create label for display current position of dam board
        rightPositionLabel = new Label();
        rightPositionLabel.setId("right-position-label");

        // create other statics labels
        Label diskCountLabel = new Label();
        diskCountLabel.getStyleClass().add("static-label");

        Label kingCountLabel = new Label();
        kingCountLabel.getStyleClass().add("static-label");

        Label removedCountLabel = new Label();
        removedCountLabel.getStyleClass().add("static-label");

        staticLabels.put("BLACK", diskCountLabel);
        staticLabels.put("BLACK-KING", kingCountLabel);
        staticLabels.put("WHITE-REMOVE", removedCountLabel);


        VBox vBox = new VBox(
                new Label("Position"),
                rightPositionLabel,
                new Label("Current Disks"),
                diskCountLabel,
                new Label("Current Kings"),
                kingCountLabel,
                new Label("Removed Disk"),
                removedCountLabel
        );
        vBox.setSpacing(5);
        vBox.setAlignment(Pos.TOP_LEFT);
        // create titled pane
        TitledPane titledPane = new TitledPane();
        titledPane.getStyleClass().add("static-group");
        titledPane.setText("Right Side Statics");
        titledPane.setContent(vBox);

        borderPane.setRight(titledPane);
    }

    private void createColorDialog(){

        // create dialog for change the dam game color pellate
        Dialog<Boolean> dialogPane = new Dialog<>();
        dialogPane.initModality(Modality.WINDOW_MODAL);
        dialogPane.setTitle("Color Settings");

        // create color pickers for various color settings
        ColorPicker whiteDiskColor = new ColorPicker(gameColorMap.get("WHITE-DISK").getValue());
        whiteDiskColor.valueProperty().addListener(((observable, oldValue, newValue) ->
            gameColorMap.get("WHITE-DISK").set(newValue)
        ));

        ColorPicker blackDiskColor = new ColorPicker(gameColorMap.get("BLACK-DISK").getValue());
        blackDiskColor.valueProperty().addListener(((observable, oldValue, newValue) ->
            gameColorMap.get("BLACK-DISK").set(newValue)
        ));

        ColorPicker whiteBoxColor = new ColorPicker(gameColorMap.get("WHITE-BOX").getValue());
        whiteBoxColor.valueProperty().addListener(((observable, oldValue, newValue) ->
            gameColorMap.get("WHITE-BOX").set(newValue)
        ));

        ColorPicker blackBoxColor = new ColorPicker(gameColorMap.get("BLACK-BOX").getValue());
        blackBoxColor.valueProperty().addListener(((observable, oldValue, newValue) ->
            gameColorMap.get("BLACK-BOX").set(newValue)
        ));

        ColorPicker whiteKingColor = new ColorPicker(gameColorMap.get("WHITE-KING").getValue());
        whiteKingColor.valueProperty().addListener(((observable, oldValue, newValue) ->
            gameColorMap.get("WHITE-KING").set(newValue)
        ));

        ColorPicker blackKingColor = new ColorPicker(gameColorMap.get("BLACK-KING").getValue());
        blackKingColor.valueProperty().addListener(((observable, oldValue, newValue) ->
            gameColorMap.get("BLACK-KING").set(newValue)
        ));


        // create grid pane for store nodes
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(20);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.add(new Label("Left Side Disk Color"), 0, 0);
        gridPane.add(whiteDiskColor, 1, 0);
        gridPane.add(new Label("Right Side Disk Color"), 0, 1);
        gridPane.add(blackDiskColor, 1, 1);
        gridPane.add(new Label("White Box Color"), 0, 2);
        gridPane.add(whiteBoxColor, 1, 2);
        gridPane.add(new Label("Black Box Color"), 0, 3);
        gridPane.add(blackBoxColor, 1, 3);
        gridPane.add(new Label("Left Side King Color"), 0, 4);
        gridPane.add(whiteKingColor, 1, 4);
        gridPane.add(new Label("Left Side King Color"), 0, 5);
        gridPane.add(blackKingColor, 1, 5);

        dialogPane.setGraphic(gridPane);
        dialogPane.getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.APPLY));
        dialogPane.showAndWait();




    }
    // end of UI of the game


    private void initiate() {
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
                Ellipse kingDisk = newValue.getActiveDisk();
                Ellipse mirrorKingDisk = newValue.getMirrorDisk();

                if (damModel.getCurrentSide() == Side.WHITE) {
                    kingDisk.fillProperty().bind(gameColorMap.get("WHITE-KING"));
                    mirrorKingDisk.fillProperty().bind(gameColorMap.get("WHITE-KING"));
                } else {
                    kingDisk.fillProperty().bind(gameColorMap.get("BLACK-KING"));
                    mirrorKingDisk.fillProperty().bind(gameColorMap.get("BLACK-KING"));
                }

            }
        });

        damModel.lastRemovedDiskProperty().addListener(new ListChangeListener<Disk>() {
            @Override
            public void onChanged(Change<? extends Disk> c) {
                while (c.next()) {
                    for (Disk disk : c.getAddedSubList()) {
                        disk.getActiveDisk().setVisible(false);
                        disk.getMirrorDisk().setVisible(false);

                        // update the disk bars
                        if (whiteDisks.containsKey(disk.getActiveDisk())) {
                            leftDiskBar.addDisk(DiskType.WHITE);
                        } else {
                            rightDiskBar.addDisk(DiskType.BLACK);
                        }
                    }
                }
                damModel.lastRemovedDiskProperty().clear(); // clear the last removed disks array list
            }
        });

        // add listeners to side property of the dam model
        damModel.currentSideProperty().addListener(((observable, oldValue, newValue) ->
            updateIndicators()
        ));
    }

    private void initiateSettings() {
        gameColorMap.put("WHITE-BOX", new SimpleObjectProperty<>(Color.WHITE));
        gameColorMap.put("BLACK-BOX", new SimpleObjectProperty<>(Color.BLACK));
        gameColorMap.put("WHITE-DISK", new SimpleObjectProperty<>(Color.DARKBLUE));
        gameColorMap.put("BLACK-DISK", new SimpleObjectProperty<>(Color.ORANGERED));
        gameColorMap.put("WHITE-KING", new SimpleObjectProperty<>(Color.GOLDENROD));
        gameColorMap.put("BLACK-KING", new SimpleObjectProperty<>(Color.MEDIUMPURPLE));

    }
    // end of the settings side of the game


    private void drawLayout() {
        // first clear the layout
        for (Rectangle rectangle : layoutBoxes) {
            leftPane.getChildren().remove(rectangle);
        }
        layoutBoxes.clear();

        for (Pane pane : new Pane[]{leftPane, rightPane}) {
            // draw board layout with standard colors
            ObjectProperty<Color> color = gameColorMap.get("WHITE-BOX");
            for (int i = 0; i < DamModel.BOARD_SIZE; i++) {
                for (int j = 0; j < DamModel.BOARD_SIZE; j++) {
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

    private void newGame() {
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

        // update statics labels
        updateStaticticsLabels();
        leftPositionLabel.setText("None");
        rightPositionLabel.setText("None");

    }

    private Ellipse createDisk(Disk disk, Side side, boolean active) {
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

    private void bindPosition(Ellipse disk, Position position) {
        // bind the center X and Y coordinates to ellipse object
        // calculate pane relative coordinates of disk
        int X = position.getX();
        int Y = DamModel.BOARD_SIZE - 1 - position.getY();

        // now bind the property
        disk.centerXProperty().bind(BOX_SIZE.multiply(X + 0.5));
        disk.centerYProperty().bind(BOX_SIZE.multiply(Y + 0.5));
    }

    private Position getCurrentPosition(MouseEvent event) {
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

    private Position getCurrentPosition(DragEvent event) {
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
    private void leftPaneMouseMoved(MouseEvent event) {
        Position currentPosition = getCurrentPosition(event);
        if (!currentPosition.isValid()) {
            return;
        }

        leftPositionLabel.setText(String.format("(%d, %d)", currentPosition.getX(), currentPosition.getY()));

        setBoxPosition(leftHoverBox, currentPosition);
        if (!leftPane.getChildren().contains(leftHoverBox)) {
            leftPane.getChildren().add(leftHoverBox);
        }
    }

    private void rightPaneMouseMoved(MouseEvent event) {
        // calculate the position using the coordinates
        Position currentPosition = getCurrentPosition(event);
        if (!currentPosition.isValid()) {
            return;
        }

        rightPositionLabel.setText(String.format("(%d, %d)", currentPosition.getX(), currentPosition.getY()));

        setBoxPosition(rightHoverBox, currentPosition);
        if (!rightPane.getChildren().contains(rightHoverBox)) {
            rightPane.getChildren().add(rightHoverBox);
        }
    }

    private void diskHover(MouseEvent event) {
        Ellipse diskImage = (Ellipse) event.getSource();
        if (diskImage != null && checkSide(diskImage)) {
            diskImage.setStrokeWidth(4);
            diskImage.setStroke(Color.DARKSALMON);
        }
    }

    private void diskExit(MouseEvent event) {
        Ellipse ellipse = (Ellipse) event.getSource();
        if (ellipse != null) {
            ellipse.setStroke(null);
            ellipse.setStrokeWidth(0);
        }
    }

    private void diskDragged(MouseEvent event) {
        // get position
        Position position = getCurrentPosition(event);
        if (position.isValid()) {
            // get disk image
            Ellipse diskImage = (Ellipse) event.getSource();
            if (checkSide(diskImage)) {
                Ellipse mirrorImage = ((Disk) diskImage.getUserData()).getMirrorDisk();

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

    private void mouseReleased(MouseEvent event) {
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

    private void dragReleased(DragEvent event) {
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

    private void setDiskBehavior(Position currentPosition, Ellipse activeDisk) {

        if (currentPosition.isValid()) {
            // validate position
            if (damModel.isBlankPosition(currentPosition, damModel.getCurrentSide())) {
                // next check if the new position is valid position
                MoveType diskMove = damModel.move((Disk) activeDisk.getUserData(), currentPosition);
                // update the satictics labels
                updateStaticticsLabels();
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
        if (damModel.determineValid((Disk) diskImage.getUserData(), position)) {
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
            leftIndicator.setFill(Color.LAWNGREEN);
            rightIndicator.setFill(Color.RED);
        } else {
            leftIndicator.setFill(Color.RED);
            rightIndicator.setFill(Color.LAWNGREEN);
        }
    }

    private void updateStaticticsLabels() {

        staticLabels.get("WHITE").setText("" + damModel.getRemainWhiteDiskCount());
        staticLabels.get("BLACK").setText("" + damModel.getRemainBlackDiskCount());
        staticLabels.get("WHITE-KING").setText("" + damModel.getWhiteKingCount());
        staticLabels.get("BLACK-KING").setText("" + damModel.getBlackKingCount());
        staticLabels.get("BLACK-REMOVE").setText("" + damModel.getRemovedBlackDiskCount());
        staticLabels.get("WHITE-REMOVE").setText("" + damModel.getRemovedWhiteDiskCount());

    }
}
