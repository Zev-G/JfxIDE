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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import sample.betterfx.Console;
import sample.ide.codeEditor.IntegratedTextEditor;
import sample.ide.fileTreeView.FileTreeView;
import sample.ide.tools.ComponentTabPane;
import sample.ide.tools.Gotten;
import sample.language.FXScript;
import sample.language.syntax.SyntaxManager;
import sample.language.interpretation.run.CodeChunk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Ide extends AnchorPane {

    public static final String STYLE_SHEET = Ide.class.getResource("styles/main.css").toExternalForm();

    private final ComponentTabPane tabPane = new ComponentTabPane();

    private final SVGPath playSvg = new SVGPath();
    private final Button playButton = new Button("", playSvg);
    private final HBox topBox = new HBox(playButton);
    private final Button runTabButton = new Button("Run");
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

    private final Pane emptyPopupPane = new Pane();
    private final VBox insidePopup = new VBox();
    private final BorderPane popupPane = new BorderPane();

    private final Label prompt = new Label();
    private final TextField inputBox = new TextField();
    private final HBox textInputBox = new HBox(prompt, inputBox);

    private final Label confirmText = new Label();
    private final Button confirm = new Button("Confirm");
    private final HBox confirmBox = new HBox(confirmText, confirm);

    private final AnchorPane projectViewAnchorPane = new AnchorPane();
    private FileTreeView projectView;

    private final VBox notificationPane = new VBox();




    public Ide() {
        projectTabButton.setVisible(false);
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

        emptyPopupPane.setMinHeight(250);
        popupPane.setCenter(insidePopup);
        insidePopup.setAlignment(Pos.CENTER);

        tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, tab, t1) -> {
            if (t1 instanceof ComponentTabPane.ComponentTab && getScene().getWindow() instanceof Stage) {
                ComponentTabPane.ComponentTab<?> componentTab = (ComponentTabPane.ComponentTab<?>) t1;
                Stage stage = (Stage) getScene().getWindow();
                if (stage.getTitle() != null) {
                    if (!stage.getTitle().contains("-")) {
                        stage.setTitle("Untitled - ");
                    }
                    stage.setTitle(stage.getTitle().split("-")[0] + "- " + componentTab.getLabel().getText());
                }
            }
        });

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

        this.getChildren().addAll(verticalSplitPane, bottomBox, sideBox, menuBar, notificationPane, popupPane);
        topBox.setFillHeight(true);
        AnchorPane.setTopAnchor(notificationPane, 13D); AnchorPane.setRightAnchor(notificationPane, 13D);

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
        MenuItem openFolder = new MenuItem("Open Folder");
        MenuItem openProject = new MenuItem("Open Project");
        openMenu.getItems().addAll(openProject, openFile, openFolder);
        newMenu.getItems().addAll(newProject, newFile);
        fileMenu.getItems().addAll(newMenu, openMenu);

        Menu tabMenu = new Menu("Tab");
        MenuItem save = new MenuItem("Save");
        save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        MenuItem close = new MenuItem("Close");
        close.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
        MenuItem openInNewWindow = new MenuItem("Open in New Window");
        openInNewWindow.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        tabMenu.getItems().addAll(save, close, openInNewWindow);

        menuBar.getMenus().addAll(fileMenu, tabMenu);
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> tabMenu.setDisable(change.getList().isEmpty()));
        // Menu event handling
        newProject.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(new Ide()));
            stage.setTitle("Untitled Project - " + (getTabPane().getSelectedTab() != null ? getTabPane().getSelectedTab().getLabel().getText() : ""));
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
        openFolder.setOnAction(actionEvent -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            File file = fileChooser.showDialog(this.getScene().getWindow());
            if (file != null) {
                loadFile(file);
            }
        });
        openProject.setOnAction(actionEvent -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            File file = fileChooser.showDialog(this.getScene().getWindow());
            if (file != null) {
                Stage stage = new Stage();
                Ide newIde = new Ide();
                stage.setScene(new Scene(newIde));
                stage.setTitle(file.isDirectory() ? file.getName() : "Untitled Project - " + (getTabPane().getSelectedTab() != null ? getTabPane().getSelectedTab().getLabel().getText() : ""));
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

        save.setOnAction(actionEvent -> {
            ComponentTabPane.ComponentTab<?> selectedTab = tabPane.getSelectedTab();
            if (selectedTab != null && selectedTab.getValue() instanceof IntegratedTextEditor && selectedTab.getFile() != null && selectedTab.getFile().exists()) {
                IntegratedTextEditor selectedTextEditor = (IntegratedTextEditor) selectedTab.getValue();
                try {
                    FileWriter fileWriter = new FileWriter(selectedTab.getFile());
                    fileWriter.write(selectedTextEditor.getText());
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        close.setOnAction(actionEvent -> {
            ComponentTabPane.ComponentTab<?> componentTab = tabPane.getSelectedTab();
            if (componentTab != null && componentTab.getFile() != null) {
                tabPane.getTabs().remove(componentTab);
            }
        });
        openInNewWindow.setOnAction(actionEvent -> {
            ComponentTabPane.ComponentTab<?> componentTab = tabPane.getSelectedTab();
            if (componentTab != null) {
                tabPane.getTabs().remove(componentTab);
                Ide ide = new Ide();
                Stage stage = new Stage();
                stage.setScene(new Scene(ide));
                stage.show();
                stage.setHeight(500);
                stage.setWidth(800);
                if (componentTab.getFile() != null) {
                    ide.loadFile(componentTab.getFile());
                } else {
                    String text = ((IntegratedTextEditor) componentTab.getValue()).getText();
                    IntegratedTextEditor newIntegratedTextEditor = new IntegratedTextEditor();
                    newIntegratedTextEditor.replaceText(text);
                    ComponentTabPane.ComponentTab<IntegratedTextEditor> dupedTab = new ComponentTabPane.ComponentTab<>(componentTab.getLabel().getText(), newIntegratedTextEditor);
                    ide.getTabPane().getTabs().add(dupedTab);
                }
            }
        });
    }
    public Ide(File file) {
        this();
        sceneProperty().addListener((observableValue, scene, t1) -> {
            if (t1 != null && scene == null) {
                t1.windowProperty().addListener((observableValue1, window, t11) -> {
                    if (t11 != null && window == null) {
                        loadFile(file);
                    }
                });
            }
        });
    }

    public void loadFile(File file) {
        if (file != null) {
            if (projectView == null) {
                if (file.isDirectory()) {
                    projectView = new FileTreeView(file, this);
                } else {
                    projectView = new FileTreeView(file.getParentFile(), this);
                }
                if (this.getScene().getWindow() instanceof Stage) {
                    ((Stage) getScene().getWindow()).setTitle(projectView.getFileRoot().getName() + " - " + (getTabPane().getSelectedTab() != null ? getTabPane().getSelectedTab().getLabel().getText() : ""));
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
        insidePopup.getChildren().clear();
        insidePopup.getChildren().add(textInputBox);
        textInputBox.setAlignment(Pos.CENTER);
        showPopup();
        this.inputBox.requestFocus();
    }
    public void showConfirmation(String confirmText, Gotten<Boolean> gotten) {
        this.confirmText.setText(confirmText);
        confirm.setOnAction(actionEvent -> {
            gotten.gotten(true);
            hidePopup();
        });
        insidePopup.getChildren().clear();
        insidePopup.getChildren().add(confirmBox);
        confirmBox.setAlignment(Pos.CENTER);
        showPopup();
        this.confirm.requestFocus();
    }

    public void showPopup() {
        popupPane.setOpacity(0);
        popupPane.setVisible(true);
        insidePopup.getChildren().add(emptyPopupPane);
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
