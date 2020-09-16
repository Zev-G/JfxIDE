package sample.ide;

import javafx.animation.FadeTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import sample.betterfx.Console;
import sample.ide.codeEditor.IntegratedTextEditor;
import sample.ide.fileTreeView.FileTreeView;
import sample.ide.tools.ComponentTabPane;
import sample.ide.tools.Gotten;
import sample.test.FXScript;
import sample.test.interpretation.SyntaxManager;
import sample.test.interpretation.run.CodeChunk;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Ide extends AnchorPane {

    public static final String STYLE_SHEET = Ide.class.getResource("main.css").toExternalForm();

    private final ComponentTabPane.ComponentTab<IntegratedTextEditor> defaultTab = getNewEditorTab(null);
    private final ComponentTabPane tabPane = new ComponentTabPane();

    private final SVGPath playSvg = new SVGPath();
    private final Button playButton = new Button("", playSvg);
    private final HBox topBox = new HBox(playButton);
    private final Button runTabButton = new Button("_Run");
    private final Button projectTabButton = new Button("P\nr\no\nj\ne\nc\nt");
    private final HBox bottomBox = new HBox(runTabButton);
    private final VBox sideBox = new VBox(projectTabButton);

    private final AnchorPane leftTab = new AnchorPane();
    private final AnchorPane rightTab = new AnchorPane(tabPane, topBox);



//    private final Console runConsole = Console.generateForJava();
    private final Console runConsole = new Console();
    private final VBox consoleAnchor = new VBox();
    private final SplitPane horizontalSplitPane = new SplitPane(leftTab, rightTab);

    private final AnchorPane bottomTab = new AnchorPane();
    private final AnchorPane topTab = new AnchorPane(horizontalSplitPane);

    private final SplitPane verticalSplitPane = new SplitPane(topTab, bottomTab);

    private final MenuBar menuBar = new MenuBar();
    private final BorderPane popupPane = new BorderPane();

    private final Label prompt = new Label();
    private final TextField inputBox = new TextField();
    private final HBox textInputBox = new HBox(prompt, inputBox);

    private final Label confirmText = new Label();
    private final Button confirm = new Button("Confirm");
    private final HBox confirmBox = new HBox(confirmText, confirm);

    private final AnchorPane projectViewAnchorPane = new AnchorPane();
    private FileTreeView projectView;




    public Ide() {
        projectTabButton.setVisible(false);
        tabPane.getTabs().add(defaultTab);
        popupPane.getStyleClass().add("popup-shower");
        popupPane.setVisible(false);
        bottomTab.setMaxHeight(0);
        leftTab.setMaxWidth(0);
        playSvg.setContent("M 0 0 L 0 18.9 L 13.5 9.45 L 0 0");
        playSvg.setFill(Color.LIGHTGREEN);
        playSvg.setPickOnBounds(true);
        playSvg.getStyleClass().add("circle-highlight-background");
        playButton.getStyleClass().add("transparent-background");
        verticalSplitPane.setOrientation(Orientation.VERTICAL);
        runConsole.disableInput();
        runConsole.setMainBg(Color.valueOf("#1c2532"));
        runConsole.setFont(new Font("Terminal", 16));

        consoleAnchor.getChildren().add(runConsole);
        consoleAnchor.setFillWidth(true);

        projectTabButton.setLineSpacing(-5);
        projectTabButton.setTextAlignment(TextAlignment.CENTER);

        textInputBox.getStyleClass().add("popup-item");
        confirmBox.getStyleClass().add("popup-item");


        bottomTab.getChildren().addListener((ListChangeListener<Node>) change -> {
            if (bottomTab.getChildren().isEmpty()) {
                bottomTab.setMaxHeight(0);
            } else {
                bottomTab.setMaxHeight(Integer.MAX_VALUE);
            }
        });
        leftTab.getChildren().addListener((ListChangeListener<Node>) change -> {
            if (leftTab.getChildren().isEmpty()) {
                leftTab.setMaxWidth(0);
            } else {
                leftTab.setMaxWidth(Integer.MAX_VALUE);
            }
        });

        makeTabButton(runTabButton, consoleAnchor, bottomTab, verticalSplitPane, 0.8);
        makeTabButton(projectTabButton, projectViewAnchorPane, leftTab, horizontalSplitPane, 0.2);

        this.getChildren().addAll(verticalSplitPane, bottomBox, sideBox, menuBar, popupPane);
        topBox.setFillHeight(true);
        AnchorPane.setTopAnchor(tabPane, 0D); AnchorPane.setBottomAnchor(tabPane, 0D);
        AnchorPane.setRightAnchor(tabPane, 0D); AnchorPane.setLeftAnchor(tabPane, 0D);

        AnchorPane.setTopAnchor(verticalSplitPane, 26D); AnchorPane.setBottomAnchor(verticalSplitPane, 35D);
        AnchorPane.setRightAnchor(verticalSplitPane, 8D); AnchorPane.setLeftAnchor(verticalSplitPane, 14D);
        AnchorPane.setTopAnchor(horizontalSplitPane, 0D); AnchorPane.setBottomAnchor(horizontalSplitPane, 0D);
        AnchorPane.setRightAnchor(horizontalSplitPane, 0D); AnchorPane.setLeftAnchor(horizontalSplitPane, 0D);

        AnchorPane.setTopAnchor(topBox, 2D); AnchorPane.setRightAnchor(topBox, 2D);
        AnchorPane.setBottomAnchor(bottomBox, 11D); AnchorPane.setLeftAnchor(bottomBox, 14D);

        AnchorPane.setTopAnchor(sideBox, 26D); AnchorPane.setLeftAnchor(sideBox, 2D);

        AnchorPane.setTopAnchor(popupPane, 0D); AnchorPane.setRightAnchor(popupPane, 0D);
        AnchorPane.setBottomAnchor(popupPane, 0D); AnchorPane.setLeftAnchor(popupPane, 0D);

        AnchorPane.setTopAnchor(menuBar, 0D); AnchorPane.setLeftAnchor(menuBar, 0D); AnchorPane.setRightAnchor(menuBar, 0D);

        playSvg.setOnMousePressed(mouseEvent -> {
            runConsole.getConsoleText().getChildren().clear();
            FXScript.restart();
            System.out.println("Parsing code...");
            CodeChunk chunk = SyntaxManager.getCodeChunkFromCode(((ComponentTabPane.ComponentTab<IntegratedTextEditor>) tabPane.getSelectedTab()).getValue().getText(), null);
            System.out.println("Finished Parsing. Running");
            chunk.run();
            if (runTabButton.getAccessibleText() == null || !runTabButton.getAccessibleText().equals("ACTIVATED")) {
                runTabButton.fire();
            }
        });

        this.setBackground(new Background(new BackgroundFill(Color.valueOf("#202937"), CornerRadii.EMPTY, Insets.EMPTY)));
        this.getStylesheets().add(STYLE_SHEET);

        Menu fileMenu = new Menu("File");
        Menu newMenu = new Menu("New");
        MenuItem newProject = new MenuItem("New Project");
        MenuItem newFile = new MenuItem("New File");
        newFile.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
        newProject.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        Menu openMenu = new Menu("Open");
        MenuItem openFile = new MenuItem("Open File");
        MenuItem openProject = new MenuItem("Open Project");
        openMenu.getItems().addAll(openProject, openFile);
        newMenu.getItems().addAll(newProject, newFile);
        fileMenu.getItems().addAll(newMenu, openMenu);
        menuBar.getMenus().addAll(fileMenu);
        // Menu event handling
        newProject.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(new Ide()));
            stage.setTitle("Untitled Project");
            stage.show();
            Window thisWindow = this.getScene().getWindow();
            stage.setWidth(thisWindow.getWidth());
            stage.setHeight(thisWindow.getHeight());
            if (thisWindow instanceof Stage) {
                stage.setMaximized(((Stage) thisWindow).isMaximized());
            }
        });
        newFile.setOnAction(actionEvent -> {
            ComponentTabPane.ComponentTab<IntegratedTextEditor> newTab = getNewEditorTab(null);
            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
        });
        openFile.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (file != null) {
                loadFile(file);
            }
        });
        openProject.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (file != null) {
                Stage stage = new Stage();
                Ide newIde = new Ide();
                stage.setScene(new Scene(newIde));
                stage.setTitle(file.isDirectory() ? file.getName() : "Untitled Project");
                stage.show();
                Window thisWindow = this.getScene().getWindow();
                stage.setWidth(thisWindow.getWidth());
                stage.setHeight(thisWindow.getHeight());
                if (thisWindow instanceof Stage) {
                    stage.setMaximized(((Stage) thisWindow).isMaximized());
                }
                newIde.loadFile(file);
            }
        });


//        tabPane.setOnTabCloseRequested(event -> {
//            if (event.getSource() instanceof ComponentTabPane.ComponentTab && !popupPane.isVisible()) {
//                ComponentTabPane.ComponentTab<IntegratedTextEditor> componentTab = (ComponentTabPane.ComponentTab<IntegratedTextEditor>) event.getSource();
////                if (componentTab.getFile() != null && componentTab.getMainNode() instanceof IntegratedTextEditor) {
////                    showConfirmation("This file ");
////                }
//            }
//        });
    }

    public void loadFile(File file) {
        if (file != null) {
            if (projectView == null) {
                if (file.isDirectory()) {
                    projectView = new FileTreeView(file, this);
                } else {
                    projectView = new FileTreeView(file.getParentFile(), this);
                }
                projectTabButton.setVisible(true);
                projectViewAnchorPane.getChildren().add(projectView);
                AnchorPane.setTopAnchor(projectView, 0D);
                AnchorPane.setBottomAnchor(projectView, 0D);
                AnchorPane.setRightAnchor(projectView, 0D);
                AnchorPane.setLeftAnchor(projectView, 0D);
            }
            if (!file.isDirectory()) {
                Tab newTab = getNewEditorTab(file);
                tabPane.getTabs().add(newTab);
                tabPane.getSelectionModel().select(newTab);
            }
        }
    }

    public HBox getTopBox() {
        return topBox;
    }

    public ComponentTabPane getTabPane() {
        return tabPane;
    }

    public SplitPane getVerticalSplitPane() {
        return verticalSplitPane;
    }

    public HBox getBottomBox() {
        return bottomBox;
    }



    private void makeTabButton(Button button, Region putInTab, AnchorPane tab, SplitPane divider, double newDividerSpot) {
        tab.getStyleClass().add("darker-background");
        tab.setMinHeight(0);
        button.setMnemonicParsing(true);
        button.setOnAction(actionEvent -> {
            if (button.getAccessibleText() != null && button.getAccessibleText().equals("ACTIVATED")) {
                button.setAccessibleText("");
                tab.getChildren().remove(putInTab);
                button.getStyleClass().remove("active-button");
            } else {
                button.setAccessibleText("ACTIVATED");
                tab.getChildren().clear();
                tab.getChildren().add(putInTab);
                AnchorPane.setTopAnchor(putInTab, 0D); AnchorPane.setBottomAnchor(putInTab, 0D);
                AnchorPane.setRightAnchor(putInTab, 0D); AnchorPane.setLeftAnchor(putInTab, 0D);
                divider.setDividerPositions(newDividerSpot);
                button.getStyleClass().add("active-button");
            }
        });
    }

    public void showPopupForText(String prompt, String defaultText, Gotten<String> gotten) {
        this.prompt.setText(prompt);
        this.inputBox.setText(defaultText);
        this.inputBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                gotten.gotten(this.inputBox.getText());
                hidePopup();
            }
        });
        this.popupPane.setCenter(textInputBox);
        textInputBox.setAlignment(Pos.CENTER);
        showPopup();
    }
    public void showConfirmation(String confirmText, Gotten<Boolean> gotten) {
        this.confirmText.setText(confirmText);
        confirm.setOnAction(actionEvent -> {
            gotten.gotten(true);
            hidePopup();
        });
        this.popupPane.setCenter(confirmBox);
        confirmBox.setAlignment(Pos.CENTER);
        showPopup();
    }

    public void showPopup() {
        popupPane.setOpacity(0);
        popupPane.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(new Duration(200), popupPane);
        fadeIn.setToValue(1);
        fadeIn.play();
        popupPane.setOnMousePressed(mouseEvent -> hidePopup());
    }
    public void hidePopup() {
        FadeTransition fadeOut = new FadeTransition(new Duration(100), popupPane);
        fadeOut.setToValue(0);
        fadeOut.play();
        fadeOut.setOnFinished(actionEvent -> popupPane.setVisible(false));
    }

    public static ComponentTabPane.ComponentTab<IntegratedTextEditor> getNewEditorTab(File file) {
        String fileName = file != null ? file.getName() : "Untitled";
        IntegratedTextEditor integratedTextEditor = new IntegratedTextEditor();
        if (file != null) {
            StringBuilder builder = new StringBuilder();
            try {
                Scanner scanner = new Scanner(file);
                boolean first = true;
                while (scanner.hasNextLine()) {
                    if (!first) {
                        builder.append("\n");
                    }
                    first = false;
                    builder.append(scanner.nextLine());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            integratedTextEditor.replaceText(builder.toString());
        }
        ComponentTabPane.ComponentTab<IntegratedTextEditor> componentTab = new ComponentTabPane.ComponentTab<>(fileName, integratedTextEditor);
        componentTab.setFile(file);
        componentTab.setMainNode(integratedTextEditor);
        return componentTab;
    }

}
