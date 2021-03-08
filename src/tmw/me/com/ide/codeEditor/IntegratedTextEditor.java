package tmw.me.com.ide.codeEditor;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyleSpans;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.highlighting.Highlighter;
import tmw.me.com.ide.codeEditor.highlighting.LanguageSupportStyleSpansFactory;
import tmw.me.com.ide.codeEditor.highlighting.StyleSpansFactory;
import tmw.me.com.ide.codeEditor.languages.LanguageLibrary;
import tmw.me.com.ide.codeEditor.languages.LanguageSupplier;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;
import tmw.me.com.ide.codeEditor.languages.PlainTextLanguage;
import tmw.me.com.ide.codeEditor.languages.components.Behavior;
import tmw.me.com.ide.codeEditor.visualcomponents.FindAndReplace;
import tmw.me.com.ide.tools.concurrent.schedulers.ConsumerEventScheduler;
import tmw.me.com.ide.tools.tabPane.ComponentTabContent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>This {@link CodeArea} adds lots of extra functionality, much of which can be customized with custom {@link LanguageSupport}.</p>
 * <br/>
 * <h2>Features</h3>
 * <ul>
 *     <li>Code Formatting</li>
 *     <li>Find and Replace</li>
 *     <li>Code Highlighting*</li>
 *     <li>A powerful highlighting pipeline fueled by the {@link Highlighter} class.</li>
 *     <li>Auto Complete*</li>
 *     <li>Error line number highlighting*</li>
 * </ul>
 * <h2>Planned Features</h3>
 * <ul>
 *     <li>A nice context menu</li>
 *     <li>Tooltips</li>
 *     <li>A Minimap (WIP)</li>
 * </ul>
 * <h3>Notes:</h3>
 * <p>*Controlled by LanguageSupport</p>
 */
public class IntegratedTextEditor extends CodeArea implements ComponentTabContent<IntegratedTextEditor> {

    // Settings
    private static final boolean USING_MINIMAP = false;
    private static final boolean USING_AUTOCOMPLETE = true;

    public static final String INDENT = "  ";
    public static final String STYLE_SHEET = IntegratedTextEditor.class.getResource("ite.css").toExternalForm();

    // Properties
    private final ObservableList<Integer> errorLines = FXCollections.observableArrayList();
    private final ObservableList<Behavior> behaviors = FXCollections.observableArrayList();
    private final ObjectProperty<LanguageSupport> languageSupport = new SimpleObjectProperty<>();
    private final IntegerProperty fontSize = new SimpleIntegerProperty();

    private final ArrayList<IntegratedTextEditor> linkedTextEditors = new ArrayList<>();

    private final ArrayList<IdeSpecialParser.PossiblePiecePackage> factoryOrder = new ArrayList<>();
    private final VBox popupBox = new VBox();
    private final ScrollPane popupScrollPane = new SmoothishScrollpane(popupBox);
    private final BorderPane titleBox = new BorderPane();
    private final VBox popupParent = new VBox(titleBox, popupScrollPane);
    private final Label popupTitleText = new Label("Autocomplete Suggestions");
    private final BoundsPopup autoCompletePopup = new BoundsPopup();
    private final ArrayList<String> selectionQueue = new ArrayList<>();

    private final MiniMap miniMap = USING_MINIMAP ? new MiniMap() : null;

    private final FindAndReplace findAndReplace = new FindAndReplace(this);

    private final ChoiceBox<LanguageSupplier<? extends LanguageSupport>> currentLanguage = new ChoiceBox<>();
    private final HBox bottomPane = new HBox(currentLanguage);

    private final VirtualizedScrollPane<IntegratedTextEditor> virtualizedScrollPane = new VirtualizedScrollPane<>(this);
    private final AnchorPane textAreaHolder = new AnchorPane(virtualizedScrollPane, findAndReplace, bottomPane);

    private int selectionIndex = 0;

    private final EventHandler<MouseEvent> popupItemEvent = mouseEvent -> {
        if (mouseEvent.getSource() instanceof Node) {
            mouseEvent.consume();
            if (selectionIndex == popupBox.getChildren().indexOf((Node) mouseEvent.getSource())) {
                this.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false));
            } else {
                Node selected = this.popupBox.getChildren().get(selectionIndex);
                if (selected != null) {
                    int indexOf = this.popupBox.getChildren().indexOf((Node) mouseEvent.getSource());
                    selected.getStyleClass().remove("selected-syntax");
                    popupBox.getChildren().get(indexOf).getStyleClass().add("selected-syntax");
                    selectionIndex = indexOf;
                }
            }
        }
    };

    private Highlighter highlighter = new Highlighter(this, new LanguageSupportStyleSpansFactory(this), findAndReplace.getFactory());

    /**
     * Constructs a new IntegratedTextEditor with {@link PlainTextLanguage} as it's language support.
     */
    public IntegratedTextEditor() {
        this(new PlainTextLanguage());
    }

    /**
     *
     * @param languageSupport The specified {@link LanguageSupport} for the text editor.
     */
    public IntegratedTextEditor(LanguageSupport languageSupport) {

        if (USING_MINIMAP) {
            textAreaHolder.getChildren().add(1, miniMap);
        }

        // Linking
        fontSize.addListener((observableValue, number, t1) -> setStyle("-fx-font-size: " + t1.intValue() + ";"));
        setFontSize(18);
        addEventFilter(ScrollEvent.ANY, e -> {
            if (e.isControlDown()) {
                e.consume();
                double amount = (fontSize.get() * 0.1) + 1;
                if (e.getDeltaY() != 0) {
                    if (e.getDeltaY() < 0) {
                        amount *= -1;
                    }
                    if (fontSize.get() + amount >= 6)
                        fontSize.set(fontSize.get() + (int) amount);
                }
            }
        });

        this.multiPlainChanges().subscribe(plainTextChanges -> {
            if (this.isFocused() || this.autoCompletePopup.isFocused()) {
                for (PlainTextChange plainTextChange : plainTextChanges) {
                    for (IntegratedTextEditor link1 : linkedTextEditors) {
                        if (link1.getScene() != null && link1 != this && !link1.getText().equals(this.getText())) {
                            if (plainTextChange.getRemoved().length() > 0) {
                                link1.deleteText(plainTextChange.getPosition(), plainTextChange.getRemovalEnd());
                            }
                            if (plainTextChange.getInserted().length() > 0) {
                                link1.insertText(plainTextChange.getPosition(), plainTextChange.getInserted());
                            }
                            if (!link1.getText().equals(this.getText())) {
                                link1.replaceText(this.getText());
                            }
                        }
                    }
                }
            }
        });

        // Some other listeners
        parentProperty().addListener((observableValue, parent, t1) -> {
            if (t1 != virtualizedScrollPane && t1 != null)
                System.err.println("Integrated Text Area's parent must always be equal to textAreaHolder");
        });

        currentLanguage.setConverter(new StringConverter<>() {
            @Override
            public String toString(LanguageSupplier<? extends LanguageSupport> languageSupport) {
                return languageSupport.getName();
            }

            @Override
            public LanguageSupplier<LanguageSupport> fromString(String s) {
                return null;
            }
        });
        for (LanguageSupplier<? extends LanguageSupport> loopSupport : LanguageLibrary.defaultLanguages) {
            if (loopSupport != null && !loopSupport.getName().equals(languageSupport.getLanguageName())) {
                currentLanguage.getItems().add(loopSupport);
            } else {
                currentLanguage.getItems().add(languageSupport.toSupplier());
            }
        }
        currentLanguage.getSelectionModel().select(languageSupport.toSupplier());
        currentLanguage.getSelectionModel().selectedItemProperty().addListener((observableValue, languageSupport1, t1) -> languageSupportProperty().set(t1.get()));

        behaviors.addListener((ListChangeListener<Behavior>) change -> {
            while (change.next()) {
                for (Behavior behavior : change.getAddedSubList()) {
                    behavior.apply(this);
                }
                for (Behavior behavior : change.getRemoved()) {
                    behavior.remove(this);
                }
            }
        });

        this.languageSupport.addListener((observableValue, languageSupport1, t1) -> {
            if (languageSupport1 != null) {
                this.getStylesheets().remove(languageSupport1.getStyleSheet());
                if (t1 != null) {
                    highlight();
                    selectionQueue.clear();
                    autoCompletePopup.hide();
                }
                Behavior[] removedBehaviors = languageSupport1.removeBehaviour(this);
                if (removedBehaviors != null) {
                    behaviors.removeAll(removedBehaviors);
                }
            }
            if (t1 != null) {
                this.getStylesheets().add(t1.getStyleSheet());
                Behavior[] addedBehaviors = t1.addBehaviour(this);
                if (addedBehaviors != null) {
                    Collections.addAll(behaviors, addedBehaviors);
                }
            }
        });

        // Language
        this.languageSupport.set(languageSupport);

        // Highlighting
        this.plainTextChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(new ConsumerEventScheduler<>(200, false, plainTextChange -> highlight()));

        // Style classes
        popupTitleText.getStyleClass().add("popup-title");
        popupParent.getStyleClass().add("auto-complete-parent");
        currentLanguage.getStyleClass().add("language-choice-box");
        bottomPane.getStyleClass().add("transparent-background");

        // Value tweaking and value setting
        this.popupScrollPane.setMaxHeight(300);
        this.getStylesheets().add(STYLE_SHEET);
        popupParent.getStylesheets().add(Ide.STYLE_SHEET);
        autoCompletePopup.getContent().add(popupParent);
        autoCompletePopup.setAutoHide(true);
        titleBox.setCenter(popupTitleText);
        popupScrollPane.setFitToWidth(true);
        popupBox.setFillWidth(true);
        popupParent.setEffect(new DropShadow());
        bottomPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        this.setParagraphGraphicFactory(LineGraphicFactory.get(this));

        // Layout
        AnchorPane.setTopAnchor(virtualizedScrollPane, 0D); AnchorPane.setBottomAnchor(virtualizedScrollPane, 25D);
        AnchorPane.setLeftAnchor(virtualizedScrollPane, 0D);

        AnchorPane.setTopAnchor(findAndReplace, 0D);
        AnchorPane.setRightAnchor(findAndReplace, 25D);

        AnchorPane.setLeftAnchor(bottomPane, 0D); AnchorPane.setRightAnchor(bottomPane, 0D);
        AnchorPane.setBottomAnchor(bottomPane, 0D);

        double divideBy = 9.5;
        AnchorPane.setRightAnchor(virtualizedScrollPane, getWidth() / divideBy);

        // This event handlers

        this.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);
        this.sceneProperty().addListener((observableValue, scene, t1) -> {
            if (t1 != null) {
                Window t11 = t1.getWindow();
                if (t11 != null) {
                    t11.xProperty().addListener((observableValue11, number, t111) -> autoCompletePopup.hide());
                    t11.yProperty().addListener((observableValue11, number, t111) -> autoCompletePopup.hide());
                    t11.widthProperty().addListener((observableValue2, number, t12) -> autoCompletePopup.hide());
                    t11.heightProperty().addListener((observableValue2, number, t12) -> autoCompletePopup.hide());
                }
            }
        });
        this.setOnMousePressed(mouseEvent -> {
            autoCompletePopup.hide();
            selectionQueue.clear();
        });
        this.textProperty().addListener((observableValue, s, t1) -> {
            if (this.getCaretPosition() < t1.length()) {
                String lastCharacter = this.getText(this.getCaretPosition(), this.getCaretPosition() + 1);
                int bracketsBefore = stringOccurrences(s, '{');
                int bracketsAfter = stringOccurrences(t1, '{');
                if (bracketsAfter == bracketsBefore - 1 && lastCharacter.equals("}")) {
                    this.replaceText(this.getCaretPosition(), this.getCaretPosition() + 1, "");
                } else {
                    int quotesBefore = stringOccurrences(s, '"');
                    int quotesAfter = stringOccurrences(t1, '"');
                    if (quotesAfter == quotesBefore - 1 && lastCharacter.equals("\"")) {
                        this.replaceText(this.getCaretPosition(), this.getCaretPosition() + 1, "");
                    }
                }
            }
            Platform.runLater(() -> fillBox(this.getParagraph(this.getCurrentParagraph()).getSegments().get(0)));
        });

        // Other event handlers
        popupScrollPane.setOnKeyPressed(keyEvent -> {
            keyEvent.consume();
            if (autoCompletePopup.isFocused()) {
                this.fireEvent(new KeyEvent(this, this, keyEvent.getEventType(), keyEvent.getCharacter(), keyEvent.getText(), keyEvent.getCode(), keyEvent.isShiftDown(), keyEvent.isControlDown(), keyEvent.isAltDown(), keyEvent.isMetaDown()));
            }
            if (keyEvent.getCode() == KeyCode.TAB) {
                insertAutocomplete();
            }
        });

        if (USING_MINIMAP) {
            miniMap.loadFromITE(this);
            widthProperty().addListener((observableValue, number, t1) -> {
                miniMap.setMaxWidth(t1.doubleValue() / divideBy);
                miniMap.setMinWidth(t1.doubleValue() / divideBy);
                AnchorPane.setRightAnchor(virtualizedScrollPane, getWidth() / divideBy);
            });
            miniMap.setMaxWidth(getWidth() / divideBy);
            miniMap.setMinWidth(getWidth() / divideBy);
            AnchorPane.setRightAnchor(miniMap, 0D); AnchorPane.setTopAnchor(miniMap, 0D);
            AnchorPane.setBottomAnchor(miniMap, 15D);
        }
    }

    private void keyPressed(KeyEvent keyEvent) {
        // Getting certain important variables which are used a lot in other places.
        String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
        KeyCode keyCode = keyEvent.getCode();
        String textAfterCaret = this.getText().substring(this.getCaretPosition());
        // This code shows or closes the Find And Replace window
        // TODO replace this with something which loops over a list of VisualComponents.
        findAndReplace.receiveKeyEvent(keyEvent);
        // Navigation in the AutoComplete Popup
        if (!factoryOrder.isEmpty() && (keyCode == KeyCode.LEFT || keyCode == KeyCode.RIGHT || keyCode == KeyCode.UP || keyCode == KeyCode.DOWN)) {
            System.out.println("Got here");
            if (autoCompletePopup.isShowing() && (keyCode == KeyCode.UP || keyCode == KeyCode.DOWN)) {
                System.out.println("Got here2");
                if (popupBox.getChildren().size() > selectionIndex) {
                    keyEvent.consume();
                    Node selected = this.popupBox.getChildren().get(selectionIndex);
                    if (selected != null) {
                        int indexOf = this.popupBox.getChildren().indexOf(selected);
                        if (keyCode == KeyCode.UP) {
                            indexOf--;
                        } else {
                            indexOf++;
                        }
                        if (indexOf >= this.popupBox.getChildren().size()) {
                            indexOf = 0;
                        } else if (indexOf < 0) {
                            indexOf = this.popupBox.getChildren().size() - 1;
                        }
                        selected.getStyleClass().remove("selected-syntax");
                        Node selecting = popupBox.getChildren().get(indexOf);
                        selecting.getStyleClass().add("selected-syntax");
                        selectionIndex = indexOf;
                        Platform.runLater(() -> {
                            Bounds locOfNode = selecting.getBoundsInParent();
                            double yLoc = locOfNode.getMinY();
                            double parentHeight = popupBox.getChildren().get(popupBox.getChildren().size() - 1).getBoundsInParent().getMinY();
                            popupScrollPane.setVvalue(yLoc / parentHeight);
                        });
                    }
                }
            } else {
                selectionQueue.clear();
            }
        }
        // Auto close brackets and quotes
        // TODO add auto closing for parentheses and square brackets and single quotes
        if (this.getSelectedText().length() > 0) {
            if (keyEvent.isShiftDown() && (keyCode == KeyCode.QUOTE || keyCode == KeyCode.OPEN_BRACKET)) {
                String selectionText = this.getSelectedText();
                IndexRange selected = this.getSelection();
                if (keyCode == KeyCode.QUOTE) {
                    selectionText = "\"" + selectionText + "\"";
                } else {
                    selectionText = "{" + selectionText + "}";
                }
                String finalSelectionText = selectionText;
                int start = selected.getStart();
                int end = selected.getEnd();
                this.replaceText(selected.getStart(), selected.getEnd(), finalSelectionText);
                Platform.runLater(() -> {
                    this.replaceText(end + 2, end + 3, "");
                    this.selectRange(start + 1, end + 1);
                });
            }
        } else if (keyEvent.isShiftDown() && keyCode == KeyCode.OPEN_BRACKET && !textAfterCaret.startsWith("}") && !keyEvent.isConsumed()) {
            Platform.runLater( () -> {
                this.insertText(this.getCaretPosition(), "}");
                this.moveTo(this.getCaretPosition() - 1);
            });
        } else if (keyEvent.isShiftDown() && keyCode == KeyCode.QUOTE && !textAfterCaret.startsWith("\"") && !keyEvent.isConsumed()) {
            Platform.runLater(() -> {
                this.insertText(this.getCaretPosition(), "\"");
                this.moveTo(this.getCaretPosition() - 1);
            });
        }
        // Auto indent
        if (keyCode == KeyCode.ENTER) {
            Pattern whiteSpace = Pattern.compile( "^\\s+" );
            Matcher m = whiteSpace.matcher(line);
            Platform.runLater( () -> this.insertText(this.getCaretPosition(), (line.trim().startsWith("#") ? "#" : "") + (m.find() ? m.group() : "") + (line.endsWith(":") ? INDENT : "")));
        } else if (keyEvent.isShiftDown()) {
            if (keyCode == KeyCode.TAB) {
                if (this.getSelectedText().length() <= 1) {
                    Pattern whiteSpace = Pattern.compile( "^\\s+" );
                    Matcher matcher = whiteSpace.matcher(line);
                    if (matcher.find()) {
                        String whiteSpaceInLine = matcher.group();
                        if (whiteSpaceInLine.startsWith("\t") || whiteSpaceInLine.startsWith(INDENT)) {
                            String replaced = (whiteSpaceInLine.startsWith("\t") ? whiteSpaceInLine.replaceFirst("\t", "") : whiteSpaceInLine.replaceFirst(" {2}", ""));
                            int lineStart = 0;
                            for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
                                lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
                            }
                            int textEnd = lineStart + whiteSpaceInLine.length();
                            int finalLineStart = lineStart;
                            Platform.runLater(() -> this.replaceText(finalLineStart, textEnd, replaced));
                        }
                    }
                } else {
                    int start = this.getSelection().getStart();
                    int end = this.getSelection().getEnd();
                    String indentedBackwards = indentBackwards(getSelectedText());
                    this.replaceText(start, end, indentedBackwards);
                    this.selectRange(start,
                            end + (indentedBackwards.length() - (end - start))
                    );
                }
            }
        } else if (keyCode == KeyCode.TAB) {
            if (!keyEvent.isControlDown() && autoCompletePopup.isShowing()) {
                if (factoryOrder.size() > selectionIndex) {
                    keyEvent.consume();
                    String text = factoryOrder.get(selectionIndex).getPutIn();
                    if (text.length() > 0 && this.getSelectedText().equals("")) {
                        insertAutocomplete();
                    } else {
                        int lineStart = 0;
                        for (int i = this.getCurrentParagraph(); i >= 0; i--) {
                            lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
                        }
                        lineStart--;
                        this.replaceText(lineStart, lineStart, "");
                    }
                }
            } else if (!keyEvent.isControlDown() && !selectionQueue.isEmpty() && this.getSelectedText().equals("")) {
                selectNext();
                keyEvent.consume();
            } else if (!keyEvent.isControlDown() && selectionQueue.isEmpty() && !this.getSelectedText().equals("")) {
                keyEvent.consume();
                int start = this.getSelection().getStart();
                int end = this.getSelection().getEnd();
                String indentForwards = indentForwards(getSelectedText());
                this.replaceText(start, end, indentForwards);
                this.selectRange(start,
                        end + (indentForwards.length() - (end - start))
                );
            }
        }
        // Scrolling
        if (keyEvent.isControlDown()) {
            if (keyCode == KeyCode.EQUALS) {
                if (keyEvent.isShiftDown()) {
                    keyEvent.consume();
                    fontSize.set(fontSize.get() + 2);
                } else {
                    keyEvent.consume();
                    fontSize.set(18);
                }
            } else if (keyCode == KeyCode.MINUS) {
                keyEvent.consume();
                if (fontSize.get() >= 8) {
                    fontSize.set(fontSize.get() - 2);
                }
            }
        }
    }

    public AnchorPane getTextAreaHolder() {
        return textAreaHolder;
    }

    public FindAndReplace getFindAndReplace() {
        return findAndReplace;
    }

    /**
     * Computes and applies the highlighting on a separate thread.
     */
    public void highlight() {
        HighlightingThread highlightingThread = new HighlightingThread(this);
        highlightingThread.setText(this.getText());
        highlightingThread.start();
    }

    /**
     * Inserts the selected item in the auto complete popup.
     */
    public void insertAutocomplete() {
        if (!autoCompletePopup.isShowing()) return;
        String text = factoryOrder.get(selectionIndex).getPutIn();
        if (factoryOrder.get(selectionIndex).isReplaceLine()) {
            int lineStart = 0;
            for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
                lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
            }
            String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
            Pattern whiteSpace = Pattern.compile("^\\s+");
            Matcher matcher = whiteSpace.matcher(line);
            if (matcher.find()) {
                text = matcher.group() + text;
            }
            this.replaceText(lineStart, this.getCaretPosition(), text);
        } else {
            this.insertText(this.getCaretPosition(), text);
        }
        selectionQueue.clear();
        selectNext();

    }

    /**
     * Selects the next item in the auto complete popup; wraps around if needed.
     */
    public void selectNext() {
        StringBuilder builder = new StringBuilder();
        boolean inPercentageSign = false;
        int loops = 0;
        int lineStart = 0;
        int parenStart = 0;
        for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
            lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
        }
        boolean first = true;
        String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
        for (char c : line.toCharArray()) {
            if (c == '%') {
                if (inPercentageSign) {
                    inPercentageSign = false;
                    builder.append(c);
                    if (first) {
                        this.selectRange(parenStart + lineStart, loops + lineStart + 1);
                        first = false;
                    } else {
                        selectionQueue.add(builder.toString());
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

    public boolean isCurrentlyUsingAutoComplete() {
        return USING_AUTOCOMPLETE && languageSupport.get().isUsingAutoComplete();
    }

    /**
     *
     * @param line The line which the text is sampled from; should be switched to an int, this is largely controlled by {@link LanguageSupport}
     */
    private void fillBox(String line) {
        if (isCurrentlyUsingAutoComplete() &&  (line.trim().length() > 0 && (this.isFocused() || this.autoCompletePopup.isFocused()))) {
            factoryOrder.clear();
            ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = languageSupport.get().getPossiblePieces(line);
            if (possiblePiecePackages != null && !possiblePiecePackages.isEmpty()) {
                ArrayList<Node> newChildren = new ArrayList<>();
                boolean showOverride = false;
                for (IdeSpecialParser.PossiblePiecePackage entry : languageSupport.get().getPossiblePieces(line)) {
                    factoryOrder.add(entry);
                    Label filledIn = new Label(entry.getFilledIn());
                    Label notFilledIn = new Label(entry.getNotFilledIn());
                    if (entry.getNotFilledIn().length() == 0) {
                        showOverride = true;
                    }
                    filledIn.getStyleClass().add("highlighted-label");
                    filledIn.setFont(new Font(16));
                    notFilledIn.setFont(new Font(16));
                    HBox box = new HBox(filledIn, notFilledIn);
                    box.setOnMousePressed(popupItemEvent);
                    box.setAccessibleText(entry.getPutIn());
                    newChildren.add(box);
                }
                if (!showOverride && !newChildren.isEmpty()) {
                    popupBox.getChildren().setAll(newChildren);
                    popupBox.getChildren().get(0).getStyleClass().add("selected-syntax");
                    selectionIndex = 0;
                    autoCompletePopup.show(this.getScene().getWindow());
                    autoCompletePopup.setHeight(popupScrollPane.getHeight());
                    popupScrollPane.setVvalue(0);
                } else {
                    autoCompletePopup.hide();
                    popupBox.getChildren().clear();
                }
            } else {
                autoCompletePopup.hide();
                popupBox.getChildren().clear();
            }
        } else {
            autoCompletePopup.hide();
        }
    }

    // Utility oriented methods
    public String properlyIndentString(String text) {
        String[] lines = text.split("\n");
        String indentCharacter = INDENT;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                indentCharacter = "\t";
                break;
            } else if (line.startsWith(INDENT)) {
                break;
            }
        }
        int minimumIndent = 999;
        for (String line : lines) {
            int lineIndent = 0;
            while (line.startsWith(indentCharacter)) {
                lineIndent++;
                line = line.substring(indentCharacter.length());
            }
            if (lineIndent < minimumIndent) {
                minimumIndent = lineIndent;
            }
        }
        StringBuilder newVersion = new StringBuilder();
        for (String line : lines) {
            newVersion.append(line.substring(minimumIndent)).append("\n");
        }
        String newString = newVersion.toString();
        return newString.substring(0, newString.length() - 1);
    }
    public String indentBackwards(String text) {
        if (text.length() <= 1) {
            return text;
        }
        String[] lines = text.split("\n");
        String indentCharacter = INDENT;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                indentCharacter = "\t";
                break;
            } else if (line.startsWith(INDENT)) {
                break;
            }
        }
        StringBuilder newString = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith(indentCharacter)) {
                line = line.substring(indentCharacter.length());
            }
            newString.append("\n").append(line);
        }
        return newString.substring(1);
    }
    public String indentForwards(String text) {
        String[] lines = text.split("\n");
        String indentCharacter = INDENT;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                indentCharacter = "\t";
                break;
            } else if (line.startsWith(INDENT)) {
                break;
            }
        }
        if (text.length() <= 1) {
            return indentCharacter + text;
        }
        StringBuilder newString = new StringBuilder();
        for (String line : lines) {
            newString.append("\n").append(indentCharacter).append(line);
        }
        return newString.substring(1);
    }
    public int[] expandFromPoint(int caretPosition, Character... stopAt) {
        int right = expandInDirection(caretPosition, 1, stopAt);
        right = right < getText().length() ? right + 1 : right;
        return new int[] { expandInDirection(caretPosition, -1, stopAt), right };
    }
    public int expandInDirection(int start, int dir, Character... stopAt) {
        String text = this.getText();
        List<Character> characters = Arrays.asList(stopAt);
        while (start > 0 && start < text.length() && !characters.contains(text.charAt(start))) start += dir;
        return start;
    }
    public ArrayList<IndexRange> allInstancesOfStringInString(String lookIn, String lookFor) {
        ArrayList<IndexRange> areas = new ArrayList<>();
        Matcher matcher = Pattern.compile("(?=" + Pattern.quote(lookFor) +")").matcher(lookIn);
        while (matcher.find()) {
            areas.add(new IndexRange(matcher.start(), matcher.start() + lookFor.length()));
        }
        return areas;
    }
    public int absolutePositionFromLine(int line) {
        int lineStart = 0;
        for (int i = line - 1; i >= 0; i--) {
            lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
        }
        return lineStart;
    }
    private int stringOccurrences(String string, char checkFor) {
        int occurrences = 0;
        for (char c : string.toCharArray()) {
            if (c == checkFor) occurrences++;
        }
        return occurrences;
    }
    public List<IndexRange> connectIndexesToNeighbors(List<IndexRange> indexRanges) {
        if (indexRanges.isEmpty())
            return indexRanges;
        ArrayList<IndexRange> newIndexRanges = new ArrayList<>();
        IndexRange lastIndexRange = indexRanges.get(0);
        for (int i = 1; i < indexRanges.size(); i++) {
            IndexRange currentIndexRange = indexRanges.get(i);
            if (lastIndexRange.getEnd() == currentIndexRange.getStart())
                lastIndexRange = new IndexRange(lastIndexRange.getStart(), currentIndexRange.getEnd());
            else {
                newIndexRanges.add(lastIndexRange);
                lastIndexRange = currentIndexRange;
            }
        }
        newIndexRanges.add(lastIndexRange);
        return newIndexRanges;
    }

    // Getters and Setters
    public LanguageSupport getLanguage() {
        return languageSupport.get();
    }
    public ObjectProperty<LanguageSupport> languageSupportProperty() {
        return languageSupport;
    }
    public void setLanguageSupport(LanguageSupport languageSupport) {
        this.languageSupport.set(languageSupport);
    }

    public ObservableList<Integer> getErrorLines() {
        return errorLines;
    }

    public ArrayList<IntegratedTextEditor> getLinkedTextEditors() {
        return linkedTextEditors;
    }

    @Override
    public IntegratedTextEditor getImportantNode() {
        return this;
    }

    @Override
    public Region getMainNode() {
        return getTextAreaHolder();
    }

    @Override
    public void save(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(getText());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IntegratedTextEditor createNewCopy() {
        IntegratedTextEditor newIntegratedTextEditor = new IntegratedTextEditor(languageSupportProperty().get());
        newIntegratedTextEditor.replaceText(getText());
        IntegratedTextEditor.linkITEs(this, newIntegratedTextEditor);
        return newIntegratedTextEditor;
    }

    public void setHighlighter(Highlighter highlighter) {
        this.highlighter = highlighter;
        highlight();
    }

    public ArrayList<StyleSpansFactory<Collection<String>>> getFactories() {
        return highlighter.getFactories();
    }

    public void lockLanguageUI() {
        textAreaHolder.getChildren().remove(bottomPane);
        AnchorPane.setBottomAnchor(virtualizedScrollPane, 0D);
    }

    public void setFontSize(int i) {
        fontSize.set(i);
    }

    public int getFontSize() {
        return fontSize.get();
    }

    public IntegerProperty fontSizeProperty() {
        return fontSize;
    }

    public static class HighlightingThread extends Thread {

        private String text;
        private final IntegratedTextEditor editor;

        public HighlightingThread(IntegratedTextEditor editor) {
            super();
            this.editor = editor;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            editor.getFindAndReplace().setFindSelectionIndex(-1);
            computeHighlighting();
        }


        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        private void computeHighlighting() {
            Pattern pattern = editor.languageSupport.get().generatePattern();
            if (pattern != null) {
                StyleSpans<Collection<String>> styleSpans = editor.getHighlighter().createStyleSpans();
                Platform.runLater(() -> {
                    try {
                        editor.setStyleSpans(0, styleSpans);
                    } catch (IndexOutOfBoundsException ignored) {
                    }
                });
            }
        }

    }

    private Highlighter getHighlighter() {
        return highlighter;
    }

    @Override
    public MenuItem[] addContext() {
        MenuItem run = new MenuItem("Run");
        run.setOnAction(actionEvent -> languageSupport.get().runCalled(this,null ));
        CheckMenuItem rapText = new CheckMenuItem("Wrap text");
        this.wrapTextProperty().unbind();
        this.wrapTextProperty().bind(rapText.selectedProperty());
        return new MenuItem[]{ new SeparatorMenuItem(), run, rapText };
    }

    public static void linkITEs(IntegratedTextEditor... links) {

        if (links.length > 1) {
            List<IntegratedTextEditor> linksList = Arrays.asList(links);
            for (IntegratedTextEditor link : links) {
                ArrayList<IntegratedTextEditor> listWithoutSelf = new ArrayList<>(linksList);
                listWithoutSelf.remove(link);
                link.linkToITEs(listWithoutSelf.toArray(new IntegratedTextEditor[0]));
            }
        }
    }

    public void linkToITEs(IntegratedTextEditor... links) {
        this.linkedTextEditors.addAll(Arrays.asList(links));
        recursivelyChangeLinkedITEs();
    }
    private void recursivelyChangeLinkedITEs() {
        for (IntegratedTextEditor link : linkedTextEditors) {
            if (!link.getLinkedTextEditors().equals(linkedTextEditors)) {
                link.getLinkedTextEditors().clear();
                link.getLinkedTextEditors().addAll(linkedTextEditors);
                link.recursivelyChangeLinkedITEs();
            }
        }
    }

    public VirtualizedScrollPane<IntegratedTextEditor> getVirtualizedScrollPane() {
        return virtualizedScrollPane;
    }

}
