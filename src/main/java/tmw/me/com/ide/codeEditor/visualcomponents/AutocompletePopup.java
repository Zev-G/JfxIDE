package tmw.me.com.ide.codeEditor.visualcomponents;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;
import tmw.me.com.ide.codeEditor.texteditor.BehavioralEditor;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;
import tmw.me.com.ide.settings.IdeSettings;
import tmw.me.com.ide.tools.NodeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutocompletePopup extends Popup implements VisualComponent<AutocompletePopup> {

    public static final Color SHADOW_COLOR = Color.web("#121324");

    private final VBox itemsBox = new VBox();
    private final ScrollPane itemsScroller = new ScrollPane(itemsBox) {
        @Override
        public void requestFocus() {
        }
    };
    private final Label resultsCount = new Label("Results: 0");
    private final Label selectedLabel = new Label("Selected: 1/1");
    private final BorderPane bottomPane = new BorderPane();
    private final VBox topBox = new VBox(itemsScroller, bottomPane);

    private final ArrayList<String> selectionQueue = new ArrayList<>();

    private final ArrayList<IdeSpecialParser.PossiblePiecePackage> factoryOrder = new ArrayList<>();
    private int selectionIndex = 0;

    private final EventHandler<MouseEvent> popupItemEvent = mouseEvent -> {
        if (mouseEvent.getSource() instanceof Node) {
            mouseEvent.consume();
            if (selectionIndex == itemsBox.getChildren().indexOf((Node) mouseEvent.getSource())) {
                this.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false));
            } else {
                Node selected = itemsBox.getChildren().get(selectionIndex);
                if (selected != null) {
                    int indexOf = itemsBox.getChildren().indexOf((Node) mouseEvent.getSource());
                    selected.getStyleClass().remove("selected-syntax");
                    itemsBox.getChildren().get(indexOf).getStyleClass().add("selected-syntax");
                    selectionIndex = indexOf;
                }
            }
        }
    };

    public AutocompletePopup(IntegratedTextEditor editor) {
        topBox.getStyleClass().add("auto-complete-parent");
        itemsScroller.getStyleClass().add("ac-scroller");
        itemsBox.getStyleClass().add("ac-items-box");
        bottomPane.getStyleClass().add("ac-bottom");
        resultsCount.getStyleClass().add("ac-results");
        selectedLabel.getStyleClass().add("ac-out-of");
        itemsScroller.setMaxHeight(300);

        topBox.getStylesheets().addAll(Ide.STYLE_SHEET);
        NodeUtils.bindParentToIDEStyle(topBox, IdeSettings.currentTheme);

        getContent().add(topBox);
        setAutoHide(true);
        itemsScroller.setFitToWidth(true);
        itemsBox.setFillWidth(true);
        topBox.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, SHADOW_COLOR, 20, 0.3, 0, 2));

        bottomPane.setLeft(resultsCount);
        bottomPane.setRight(selectedLabel);

        itemsBox.getChildren().addListener((ListChangeListener<Node>) change -> selectedLabel.setText((selectionIndex + 1) + "/" + change.getList().size()));

        itemsScroller.setOnKeyPressed(keyEvent -> {
            keyEvent.consume();
            if (isFocused() && !itemsScroller.isFocused()) {
                editor.fireEvent(new KeyEvent(this, editor, keyEvent.getEventType(), keyEvent.getCharacter(), keyEvent.getText(), keyEvent.getCode(), keyEvent.isShiftDown(), keyEvent.isControlDown(), keyEvent.isAltDown(), keyEvent.isMetaDown()));
            }
            if (keyEvent.getCode() == KeyCode.TAB) {
                insertAutocomplete(editor);
            }
        });
    }

    @Override
    public void apply(BehavioralEditor ite) {

    }

    @Override
    public void receiveKeyEvent(KeyEvent keyEvent, BehavioralEditor editor) {
        KeyCode keyCode = keyEvent.getCode();
        if (!factoryOrder.isEmpty() && (keyCode == KeyCode.LEFT || keyCode == KeyCode.RIGHT || keyCode == KeyCode.UP || keyCode == KeyCode.DOWN)) {
            if (isShowing() && (keyCode == KeyCode.UP || keyCode == KeyCode.DOWN)) {
                if (itemsBox.getChildren().size() > selectionIndex) {
                    keyEvent.consume();
                    Node selected = itemsBox.getChildren().get(selectionIndex);
                    if (selected != null) {
                        int indexOf = itemsBox.getChildren().indexOf(selected);
                        if (keyCode == KeyCode.UP) {
                            indexOf--;
                        } else {
                            indexOf++;
                        }
                        if (indexOf >= itemsBox.getChildren().size()) {
                            indexOf = 0;
                        } else if (indexOf < 0) {
                            indexOf = itemsBox.getChildren().size() - 1;
                        }
                        selected.getStyleClass().remove("selected-syntax");
                        Node selecting = itemsBox.getChildren().get(indexOf);
                        selecting.getStyleClass().add("selected-syntax");
                        selectionIndex = indexOf;
                        Platform.runLater(() -> {
                            Bounds locOfNode = selecting.getBoundsInParent();
                            double yLoc = locOfNode.getMinY();
                            double parentHeight = itemsBox.getChildren().get(itemsBox.getChildren().size() - 1).getBoundsInParent().getMinY();
                            itemsScroller.setVvalue(yLoc / parentHeight);

                            selectedLabel.setText((selectionIndex + 1) + "/" + itemsBox.getChildren().size());
                        });
                    }
                }
            } else {
                selectionQueue.clear();
            }
        }
        if (keyEvent.getCode() == KeyCode.TAB && !keyEvent.isControlDown() && isShowing()) {
            if (factoryOrder.size() > selectionIndex) {
                keyEvent.consume();
                String text = factoryOrder.get(selectionIndex).getPutIn();
                if (text.length() > 0 && editor.getSelectedText().equals("")) {
                    insertAutocomplete(editor);
                } else {
                    int lineStart = 0;
                    for (int i = editor.getCurrentParagraph(); i >= 0; i--) {
                        lineStart = lineStart + editor.getParagraph(i).getText().length() + 1;
                    }
                    lineStart--;
                    editor.replaceText(lineStart, lineStart, "");
                }
            }
        }
    }

    public ArrayList<IdeSpecialParser.PossiblePiecePackage> getFactoryOrder() {
        return factoryOrder;
    }

    public ArrayList<String> getSelectionQueue() {
        return selectionQueue;
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    /**
     * @param line The line which the text is sampled from; should be switched to an int, this is largely controlled by {@link LanguageSupport}
     */
    public void fillBox(IntegratedTextEditor editor, String line) {
        if (editor.isCurrentlyUsingAutoComplete() && (line.trim().length() > 0 && (editor.isFocused() || this.isFocused()))) {
            factoryOrder.clear();
            List<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = editor.getLanguage().getPossiblePieces(line, editor);
            if (possiblePiecePackages != null && !possiblePiecePackages.isEmpty()) {
                ArrayList<Node> newChildren = new ArrayList<>();
                boolean showOverride = false;
                for (IdeSpecialParser.PossiblePiecePackage entry : editor.getLanguage().getPossiblePieces(line, editor)) {
                    factoryOrder.add(entry);
                    Label filledIn = new Label(entry.getFilledIn());
                    Label notFilledIn = new Label(entry.getNotFilledIn());
                    if (entry.getNotFilledIn().length() == 0) {
                        showOverride = true;
                    }
                    filledIn.getStyleClass().addAll("ac-label", "ac-filled-in");
                    notFilledIn.getStyleClass().addAll("ac-label", "ac-not-filled-in");
                    HBox box = new HBox(filledIn, notFilledIn);
                    box.getStyleClass().add("syntax-box");

                    box.setOnMousePressed(popupItemEvent);
                    box.setAccessibleText(entry.getPutIn());
                    newChildren.add(box);
                }
                if (!showOverride && !newChildren.isEmpty()) {
                    if (newChildren.size() > 4) {
                        if (!topBox.getChildren().contains(bottomPane)) {
                            topBox.getChildren().add(bottomPane);
                        }
                        resultsCount.setText("Results: " + newChildren.size());
                    } else {
                        topBox.getChildren().remove(bottomPane);
                    }
                    itemsBox.getChildren().setAll(newChildren);
                    itemsBox.getChildren().get(0).getStyleClass().add("selected-syntax");
                    selectionIndex = 0;
                    selectedLabel.setText((selectionIndex + 1) + "/" + newChildren.size());
                    this.show(editor.getScene().getWindow());
                    this.setHeight(itemsScroller.getHeight());
                    itemsScroller.setVvalue(0);
                } else {
                    this.hide();
                    itemsBox.getChildren().clear();
                }
            } else {
                this.hide();
                itemsBox.getChildren().clear();
            }
        } else {
            this.hide();
        }
    }

    /**
     * Inserts the selected item in the auto complete popup.
     */
    public void insertAutocomplete(BehavioralEditor editor) {
        if (!isShowing()) return;
        String text = getFactoryOrder().get(getSelectionIndex()).getPutIn();
        if (getFactoryOrder().get(getSelectionIndex()).isReplaceLine()) {
            int lineStart = 0;
            for (int i = editor.getCurrentParagraph() - 1; i >= 0; i--) {
                lineStart = lineStart + editor.getParagraph(i).getText().length() + 1;
            }
            String line = editor.getParagraph(editor.getCurrentParagraph()).getSegments().get(0);
            Pattern whiteSpace = Pattern.compile("^\\s+");
            Matcher matcher = whiteSpace.matcher(line);
            if (matcher.find()) {
                text = matcher.group() + text;
            }
            editor.replaceText(lineStart, editor.getCaretPosition(), text);
        } else {
            editor.insertText(editor.getCaretPosition(), text);
        }
        getSelectionQueue().clear();
        selectNext(editor);
    }

    /**
     * Selects the next item in the auto complete popup; wraps around if needed.
     */
    public void selectNext(BehavioralEditor editor) {
        StringBuilder builder = new StringBuilder();
        boolean inPercentageSign = false;
        int loops = 0;
        int lineStart = 0;
        int parenStart = 0;
        for (int i = editor.getCurrentParagraph() - 1; i >= 0; i--) {
            lineStart = lineStart + editor.getParagraph(i).getText().length() + 1;
        }
        boolean first = true;
        String line = editor.getParagraph(editor.getCurrentParagraph()).getSegments().get(0);
        for (char c : line.toCharArray()) {
            if (c == '%') {
                if (inPercentageSign) {
                    inPercentageSign = false;
                    builder.append(c);
                    if (first) {
                        editor.selectRange(parenStart + lineStart, loops + lineStart + 1);
                        first = false;
                    } else {
                        getSelectionQueue().add(builder.toString());
                    }
                    builder = new StringBuilder();
                } else {
                    inPercentageSign = true;
                    builder.append(c);
                    parenStart = loops;
                }
            }
            loops++;
        }
    }

    public VBox getTopBox() {
        return topBox;
    }
}
