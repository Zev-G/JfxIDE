package sample.ide;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import sample.betterfx.Console;
import sample.ide.codeEditor.IntegratedTextEditor;
import sample.ide.tools.ComponentTabPane;
import sample.test.FXScript;
import sample.test.interpretation.SyntaxManager;
import sample.test.interpretation.run.CodeChunk;

public class Ide extends AnchorPane {

    private final IntegratedTextEditor defaultTextEditor = new IntegratedTextEditor();
    private final ComponentTabPane.ComponentTab defaultTab = new ComponentTabPane.ComponentTab("Untitled", defaultTextEditor);
    private final ComponentTabPane tabPane = new ComponentTabPane(defaultTab);

    private final SVGPath playSvg = new SVGPath();
    private final HBox topBox = new HBox(playSvg);
    private final Button runTabButton = new Button("Run");
    private final HBox bottomBox = new HBox(runTabButton);

    private final AnchorPane bottomTab = new AnchorPane();
    private final AnchorPane topTab = new AnchorPane(tabPane, topBox);

//    private final Console runConsole = Console.generateForJava();
    private final Console runConsole = new Console();
    private final VBox consoleAnchor = new VBox();

    private final SplitPane verticalSplitPane = new SplitPane(topTab, bottomTab);

    public Ide() {
        bottomTab.setMaxHeight(0);
        playSvg.setContent("M 0 0 L 0 18.9 L 13.5 9.45 L 0 0");
        playSvg.setFill(Color.LIGHTGREEN);
        playSvg.setPickOnBounds(true);
        defaultTab.setClosable(false);
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

        this.getChildren().addAll(verticalSplitPane, bottomBox);
        topBox.setFillHeight(true);
        AnchorPane.setTopAnchor(tabPane, 0D); AnchorPane.setBottomAnchor(tabPane, 0D);
        AnchorPane.setRightAnchor(tabPane, 0D); AnchorPane.setLeftAnchor(tabPane, 0D);

        AnchorPane.setTopAnchor(verticalSplitPane, 8D); AnchorPane.setBottomAnchor(verticalSplitPane, 35D);
        AnchorPane.setRightAnchor(verticalSplitPane, 8D); AnchorPane.setLeftAnchor(verticalSplitPane, 8D);

        AnchorPane.setTopAnchor(topBox, 2D); AnchorPane.setRightAnchor(topBox, 2D);
        AnchorPane.setBottomAnchor(bottomBox, 11D); AnchorPane.setLeftAnchor(bottomBox, 8D);

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

}
