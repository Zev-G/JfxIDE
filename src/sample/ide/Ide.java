package sample.ide;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
import javafx.stage.Stage;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;
import sample.betterfx.Console;
import sample.ide.codeEditor.IntegratedTextEditor;
import sample.ide.tools.ComponentTabPane;
import sample.test.FXScript;
import sample.test.interpretation.SyntaxManager;
import sample.test.interpretation.run.CodeChunk;

import java.io.File;

public class Ide extends AnchorPane {

    private final ComponentTabPane.ComponentTab defaultTab = getNewEditorTab(null);
    private final ComponentTabPane tabPane = new ComponentTabPane(defaultTab);

    private final SVGPath playSvg = new SVGPath();
    private final Button playButton = new Button("", playSvg);
    private final HBox topBox = new HBox(playButton);
    private final Button runTabButton = new Button("_Run");
    private final HBox bottomBox = new HBox(runTabButton);

    private final AnchorPane bottomTab = new AnchorPane();
    private final AnchorPane topTab = new AnchorPane(tabPane, topBox);

//    private final Console runConsole = Console.generateForJava();
    private final Console runConsole = new Console();
    private final VBox consoleAnchor = new VBox();
    private final SplitPane verticalSplitPane = new SplitPane(topTab, bottomTab);
    private final MenuBar menuBar = new MenuBar();
    private final AnchorPane popupAnchorPane = new AnchorPane();


    public Ide() {

        popupAnchorPane.getStyleClass().add(".popup-shower");
        popupAnchorPane.setVisible(false);
        bottomTab.setMaxHeight(0);
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


        bottomTab.getChildren().addListener((ListChangeListener<Node>) change -> {
            if (bottomTab.getChildren().isEmpty()) {
                bottomTab.setMaxHeight(0);
            } else {
                bottomTab.setMaxHeight(Integer.MAX_VALUE);
            }
        });

        makeTabButton(runTabButton, consoleAnchor, bottomTab, verticalSplitPane);

        this.getChildren().addAll(verticalSplitPane, bottomBox, popupAnchorPane, menuBar);
        topBox.setFillHeight(true);
        AnchorPane.setTopAnchor(tabPane, 0D); AnchorPane.setBottomAnchor(tabPane, 0D);
        AnchorPane.setRightAnchor(tabPane, 0D); AnchorPane.setLeftAnchor(tabPane, 0D);

        AnchorPane.setTopAnchor(verticalSplitPane, 26D); AnchorPane.setBottomAnchor(verticalSplitPane, 35D);
        AnchorPane.setRightAnchor(verticalSplitPane, 8D); AnchorPane.setLeftAnchor(verticalSplitPane, 8D);

        AnchorPane.setTopAnchor(topBox, 2D); AnchorPane.setRightAnchor(topBox, 2D);
        AnchorPane.setBottomAnchor(bottomBox, 11D); AnchorPane.setLeftAnchor(bottomBox, 8D);

        AnchorPane.setTopAnchor(popupAnchorPane, 0D); AnchorPane.setRightAnchor(popupAnchorPane, 0D);
        AnchorPane.setBottomAnchor(popupAnchorPane, 0D); AnchorPane.setLeftAnchor(popupAnchorPane, 0D);

        AnchorPane.setTopAnchor(menuBar, 0D); AnchorPane.setLeftAnchor(menuBar, 0D); AnchorPane.setRightAnchor(menuBar, 0D);

        playSvg.setOnMousePressed(mouseEvent -> {
            runConsole.getConsoleText().getChildren().clear();
            FXScript.restart();
            System.out.println("Parsing code...");
            CodeChunk chunk = SyntaxManager.getCodeChunkFromCode(tabPane.getSelectedTab().getIntegratedTextEditor().getText(), null);
            System.out.println("Finished Parsing. Running");
            chunk.run();
            if (runTabButton.getAccessibleText() == null || !runTabButton.getAccessibleText().equals("ACTIVATED")) {
                runTabButton.fire();
            }
        });

        this.setBackground(new Background(new BackgroundFill(Color.valueOf("#202937"), CornerRadii.EMPTY, Insets.EMPTY)));
        this.getStylesheets().add(Ide.class.getResource("main.css").toExternalForm());

        Menu fileMenu = new Menu("File");
        Menu newMenu = new Menu("New");
        MenuItem newProject = new MenuItem("New Project");
        MenuItem newFile = new MenuItem("New File");
        newFile.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
        newProject.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newMenu.getItems().addAll(newProject, newFile);
        fileMenu.getItems().addAll(newMenu);
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
            ComponentTabPane.ComponentTab newTab = getNewEditorTab(null);
            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
        });


        tabPane.setOnTabCloseRequested(event -> {
            if (event.getSource() instanceof ComponentTabPane.ComponentTab) {
                ComponentTabPane.ComponentTab componentTab = (ComponentTabPane.ComponentTab) event.getSource();
                if (componentTab.getFile() != null && componentTab.getMainNode() instanceof IntegratedTextEditor) {
                    // Show close confirmation
                }
            }
        });
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

    private void makeTabButton(Button button, Region putInTab, AnchorPane tab, SplitPane divider) {
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
                divider.setDividerPositions(0.8);
                button.getStyleClass().add("active-button");
            }
        });
    }

    private ComponentTabPane.ComponentTab getNewEditorTab(File file) {
        String fileName = file != null ? file.getName() : "Untitled";
        IntegratedTextEditor integratedTextEditor = new IntegratedTextEditor();
        ComponentTabPane.ComponentTab componentTab = new ComponentTabPane.ComponentTab(fileName, integratedTextEditor);
        componentTab.setFile(file);
        componentTab.setMainNode(integratedTextEditor);
        return componentTab;
    }

}
