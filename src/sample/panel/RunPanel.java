package sample.panel;

import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import sample.test.interpretation.SyntaxManager;
import sample.test.interpretation.parse.Parser;
import sample.test.interpretation.run.CodeChunk;
import sample.test.interpretation.run.CodePiece;
import sample.test.interpretation.run.CodeState;
import sample.test.syntaxPiece.effects.EffectFactory;
import sample.test.syntaxPiece.events.WhenEventFactory;
import sample.test.syntaxPiece.expressions.ExpressionFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class RunPanel extends AnchorPane {

    private final TreeView<CodeState> treeView = new TreeView<>();
    private final HashMap<CodeState, Tab> tabFromState = new HashMap<>();

    private final CodeChunk topChunk;
    private final TabPane tabPane = new TabPane();
    private final SplitPane splitPane = new SplitPane(treeView, tabPane);

    public RunPanel(CodeChunk topChunk) {
        this.getChildren().add(splitPane);
        AnchorPane.setTopAnchor(splitPane, 0D); AnchorPane.setBottomAnchor(splitPane, 0D);
        AnchorPane.setRightAnchor(splitPane, 0D); AnchorPane.setLeftAnchor(splitPane, 0D);
        this.topChunk = topChunk;
        treeView.setEditable(true);
        TreeItem<CodeState> topCodeChunk = new TreeItem<>(topChunk);
        treeView.setRoot(topCodeChunk);
        treeView.setOnMousePressed(mouseEvent -> {
            if (treeView.getSelectionModel().getSelectedItem() != null) {
                Tab tab = getTabForCodeState((treeView.getSelectionModel().getSelectedItem().getValue()));
                if (!tabPane.getTabs().contains(tab)) {
                    tabPane.getTabs().add(tab);
                }
                tabPane.getSelectionModel().select(tab);
            }
        });
        populateTreeView(topCodeChunk, topChunk);
    }

    public TreeView<CodeState> getTreeView() {
        return treeView;
    }
    public CodeChunk getTopChunk() {
        return topChunk;
    }

    public SplitPane getSplitPane() {
        return splitPane;
    }

    private void populateTreeView(TreeItem<CodeState> abovePiece, CodeState codeChunk) {
        ArrayList<CodeState> children = codeChunk.getChildren();
        for (CodeState child : children) {
            TreeItem<CodeState> newTreeItem = new TreeItem<>(child);
            abovePiece.getChildren().add(newTreeItem);
            populateTreeView(newTreeItem, child);
        }
    }

    private Tab getTabForCodeState(CodeState state) {
        if (tabFromState.get(state) != null) { return tabFromState.get(state); }
        VBox topBox = new VBox();
        topBox.setSpacing(15);
        VBox variablesBox = new VBox();
        variablesBox.setSpacing(5);
        TitledPane variables = new TitledPane("Variables", variablesBox);
        state.setNewVariable(newValue -> {
            VBox variableBox = new VBox();
            Label title = new Label("{" + newValue.getName() + "}");
            title.setFont(new Font(22));
            Label valueLabel = new Label("Value: " + newValue.getValue());
            valueLabel.setFont(new Font(14));
            Label typeLabel = new Label();
            if (newValue.getValue() != null) {
                typeLabel.setText("Type: " + newValue.getValue().getClass().getSimpleName());
            } else {
                typeLabel.setText("Type: Not set");
            }
            typeLabel.setFont(new Font(14));
            Button setValue = new Button("Set value from code");
            TextField code = new TextField();
            HBox hBox = new HBox(code, setValue);
            hBox.setSpacing(8);
            setValue.setOnAction(actionEvent -> {
                ExpressionFactory<?> expressionFactory = Parser.parseExpression(code.getText());
                expressionFactory.setState((CodeChunk) state);
                newValue.setValue(expressionFactory.activate());
            });
            variableBox.getChildren().addAll(title, valueLabel, typeLabel, hBox);
            variablesBox.getChildren().add(variableBox);
            newValue.setChangeListener((observableValue, o, t1) -> {
                if (t1 != null) {
                    valueLabel.setText("Value: " + t1.toString());
                    typeLabel.setText("Type: " + t1.getClass().getSimpleName());
                }
            });
        });
        VBox expressionBox = new VBox();
        TitledPane expressions = new TitledPane("Local Expressions", expressionBox);
        for (ExpressionFactory<?> expressionFactory : state.getLocalExpressions()) {
            VBox variableBox = new VBox();
            Label title = new Label(expressionFactory.getRegex());
            Label value = new Label("Value: unchecked");
            value.setFont(new Font(15));
            Button checkValue = new Button("Check value");
            HBox valueBox = new HBox(value, checkValue);
            valueBox.setSpacing(5);
            checkValue.setOnAction(actionEvent -> value.setText("Value: " + expressionFactory.activate()));
            title.setFont(new Font(22));
            variableBox.getChildren().addAll(title, valueBox);
            expressionBox.getChildren().add(variableBox);
        }
        VBox codePiecesBox = new VBox();
        TitledPane codePieces = new TitledPane("Lines", codePiecesBox);
        for (CodePiece piece : ((CodeChunk) state).getPieces()) {
            VBox pieceExpressionsBox = new VBox();
            TitledPane pieceExpressions = new TitledPane(piece.getCode(), pieceExpressionsBox);
            pieceExpressions.setAnimated(false);
            if (piece.getEffect() != null) {
                recursiveExpressionAdding(((EffectFactory) piece.getEffect()).getExpressionArgs(), pieceExpressionsBox);
            } else if (piece.getEvent() != null) {
                Label title = new Label("Event: " + piece.getEvent().getCode());
                title.setFont(new Font(16));
                pieceExpressionsBox.getChildren().add(title);
                if (piece.getEvent() instanceof WhenEventFactory) {
                    Label regex = new Label("Regex: " + ((WhenEventFactory) piece.getEvent()).getRegex());
                    pieceExpressionsBox.getChildren().add(regex);
                }
                if (piece.getEvent().getRunChunk() != null) {
                    Button openEventTab = new Button("Open event tab");
                    openEventTab.setOnAction(actionEvent -> {
                        Tab tab = getTabForCodeState(piece.getEvent().getRunChunk());
                        if (!tabPane.getTabs().contains(tab)) {
                            tabPane.getTabs().add(tab);
                        }
                        tabPane.getSelectionModel().select(tab);
                    });
                    pieceExpressionsBox.getChildren().add(openEventTab);
                }
            }
            Button button = new Button("Run");
            button.setOnAction(actionEvent -> piece.run());
            pieceExpressionsBox.getChildren().add(button);
            pieceExpressions.setExpanded(false);
            codePiecesBox.getChildren().add(pieceExpressions);
        }
        HBox codeBox = new HBox();
        TextField codeField = new TextField();
        Button activate = new Button("Activate code");
        codeBox.getChildren().addAll(codeField, activate);
        codeBox.setSpacing(5);
        activate.setOnAction(actionEvent -> {
            CodePiece piece = SyntaxManager.genCodePieceFromCode(codeField.getText(), null, 0);
            piece.setCodeChunk((CodeChunk) state);
            ((CodeChunk) state).runPiece(piece);
        });
        Button runChunk = new Button("Run Chunk");
        runChunk.setOnAction(actionEvent -> ((CodeChunk) state).run());
        Pane emptyPane = new Pane();
        emptyPane.setMinHeight(150);
        topBox.getChildren().addAll(variables, expressions, codePieces, codeBox, runChunk, emptyPane);
        ScrollPane scrollPane = new ScrollPane(topBox);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        Tab stateTab = new Tab(state.getCode(), scrollPane);
        tabFromState.put(state, stateTab);
        Button reload = new Button("Refresh info");
        reload.setOnAction(actionEvent -> {
            tabFromState.remove(state);
            stateTab.setContent(getTabForCodeState(state).getContent());
            tabFromState.put(state, stateTab);
        });
        topBox.getChildren().add(reload);
        reload.toBack();
        return stateTab;
    }

    public void recursiveExpressionAdding(ArrayList<ExpressionFactory<?>> expressionFactories, VBox pieceExpressionsBox) {
        for (ExpressionFactory<?> expressionFactory : expressionFactories) {
            if (expressionFactory.getExpressionArgs().isEmpty()) {
                Label title = new Label("Expression: " + expressionFactory.getCode());
                title.setFont(new Font(16));
                Label expression = new Label("Class: " + expressionFactory.getGenericClass().getSimpleName());
                Label regex = new Label("Regex: " + expressionFactory.getRegex());
                pieceExpressionsBox.getChildren().addAll(title, expression, regex);
                continue;
            }
            VBox innerExpressionsBox = new VBox();
            TitledPane innerExpressions = new TitledPane(expressionFactory.getRegex(), innerExpressionsBox);
            recursiveExpressionAdding(expressionFactory.getExpressionArgs(), innerExpressionsBox);
            pieceExpressionsBox.getChildren().add(innerExpressions);
        }
    }

}
