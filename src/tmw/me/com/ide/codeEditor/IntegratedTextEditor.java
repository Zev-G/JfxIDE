package tmw.me.com.ide.codeEditor;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Window;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import tmw.me.com.ide.Ide;
import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;
import tmw.me.com.ide.codeEditor.languages.SfsLanguage;
import tmw.me.com.ide.tools.concurrent.RunnableEventScheduler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>This {@link CodeArea} adds lots of extra functionality, much of which can be customized with custom {@link LanguageSupport}.</p>
 * <br/>
 * <h3>Features</h3>
 * <ul>
 *     <li>Auto Indentation</li>
 *     <li>Comment continuation</li>
 *     <li>Backwards Indentation</li>
 *     <li>Surround selection with bracket/quote</li>
 *     <li>Auto double quotes and double brackets</li>
 *     <li>Code Highlighting*</li>
 *     <li>Auto Complete*</li>
 *     <li>Error line number highlighting*</li>
 * </ul>
 * <h3>Planned Features</h3>
 * <ul>
 *     <li>Find and Replace</li>
 *     <li>More interactive highlighting features</li>
 *     <li>A nice context menu</li>
 * </ul>
 * <p>*Controlled by LanguageSupport</p>
 */
public class IntegratedTextEditor extends CodeArea {

    public static final String INDENT = "  ";

    private final ObservableList<Integer> errorLines = FXCollections.observableArrayList();
    private final ObjectProperty<LanguageSupport> languageSupport = new SimpleObjectProperty<>();

    private final ArrayList<IdeSpecialParser.PossiblePiecePackage> factoryOrder = new ArrayList<>();
    private final VBox popupBox = new VBox();
    private final ScrollPane popupScrollPane = new SmoothishScrollpane(popupBox);
    private final Popup autoCompletePopup = new Popup();
    private final ArrayList<String> selectionQueue = new ArrayList<>();

    private final VirtualizedScrollPane<IntegratedTextEditor> virtualizedScrollPane = new VirtualizedScrollPane<>(this);
    private final AnchorPane textAreaHolder = new AnchorPane(virtualizedScrollPane);

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


    /**
     * Constructs a new IntegratedTextEditor with {@link SfsLanguage} as it's language support.
     */
    public IntegratedTextEditor() {
        this(new SfsLanguage());
    }

    /**
     *
     * @param languageSupport The specified {@link LanguageSupport} for the text editor.
     */
    public IntegratedTextEditor(LanguageSupport languageSupport) {

        parentProperty().addListener((observableValue, parent, t1) -> {
            if (t1 != virtualizedScrollPane && t1 != null) {
                System.err.println("Integrated Text Area's parent must always be equal to textAreaHolder");
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
            }
            if (t1 != null) {
                this.getStylesheets().add(t1.getStyleSheet());
            }
        });

        this.languageSupport.set(languageSupport);
        languageSupport.addBehaviour(this);

        // Highlighting
        RunnableEventScheduler runnableEventScheduler = new RunnableEventScheduler(200, this::highlight);
        runnableEventScheduler.setRunOnFx(false);
        this.plainTextChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(plainTextChange -> runnableEventScheduler.run());


        // Value tweaking and value setting
        this.popupScrollPane.setMaxHeight(300);
        this.getStylesheets().add(IntegratedTextEditor.class.getResource("ide.css").toExternalForm());
        popupScrollPane.getStylesheets().add(Ide.class.getResource("styles/main.css").toExternalForm());
        autoCompletePopup.getContent().add(popupScrollPane);
        autoCompletePopup.setAutoHide(true);
        this.setParagraphGraphicFactory(LineGraphicFactory.get(this));

        // Layout
        AnchorPane.setTopAnchor(virtualizedScrollPane, 0D); AnchorPane.setBottomAnchor(virtualizedScrollPane, 0D);
        AnchorPane.setRightAnchor(virtualizedScrollPane, 0D); AnchorPane.setLeftAnchor(virtualizedScrollPane, 0D);

        // This event handlers
        Pattern whiteSpace = Pattern.compile( "^\\s+" );
        this.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
            KeyCode keyCode = keyEvent.getCode();
            String textAfterCaret = this.getText().substring(this.getCaretPosition());
            if (!selectionQueue.isEmpty() && keyCode == KeyCode.LEFT || keyCode == KeyCode.RIGHT || keyCode == KeyCode.UP || keyCode == KeyCode.DOWN) {
                if (autoCompletePopup.isShowing() && (keyCode == KeyCode.UP || keyCode == KeyCode.DOWN)) {
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
                            popupBox.getChildren().get(indexOf).getStyleClass().add("selected-syntax");
                            selectionIndex = indexOf;
                        }
                    }
                } else {
                    selectionQueue.clear();
                }
            }
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
            if (keyCode == KeyCode.ENTER) {
                Matcher m = whiteSpace.matcher(line);
                Platform.runLater( () -> this.insertText(this.getCaretPosition(), (line.trim().startsWith("#") ? "#" : "") + (m.find() ? m.group() : "") + (line.endsWith(":") ? INDENT : "")));
            } else if (keyEvent.isShiftDown()) {
                if (keyCode == KeyCode.TAB) {
                    if (this.getSelectedText().length() <= 1) {
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
        });
        this.caretBoundsProperty().addListener((observableValue, integer, t1) -> {
            if (this.caretBoundsProperty().getValue().isPresent()) {
                Bounds bounds = this.caretBoundsProperty().getValue().get();
                Platform.runLater(() -> {
                    autoCompletePopup.setX(bounds.getMaxX());
                    autoCompletePopup.setY(bounds.getMaxY());
                });
            }
        });
        this.sceneProperty().addListener((observableValue, scene, t1) -> {
            if (t1 != null) {
                Window t11 = t1.getWindow();
                t11.xProperty().addListener((observableValue11, number, t111) -> autoCompletePopup.hide());
                t11.yProperty().addListener((observableValue11, number, t111) -> autoCompletePopup.hide());
                t11.widthProperty().addListener((observableValue2, number, t12) -> autoCompletePopup.hide());
                t11.heightProperty().addListener((observableValue2, number, t12) -> autoCompletePopup.hide());
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
    }

    public AnchorPane getTextAreaHolder() {
        return textAreaHolder;
    }

    /**
     * Computes and applies the highlighting on a separate thread.
     */
    public void highlight() {
        HighlightingThread highlightingThread = new HighlightingThread();
        highlightingThread.setText(this.getText());
        highlightingThread.start();
    }

    /**
     * Inserts the selected item in the auto complete popup.
     */
    public void insertAutocomplete() {
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
     * Selects the next item in the auto complete popup; wraps around.
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

    /**
     *
     * @param line The line which the text is sampled from; should be switched to an int, this is largely controlled by {@link LanguageSupport}
     */
    private void fillBox(String line) {
        if (line.trim().length() > 0) {
            factoryOrder.clear();
            popupBox.getChildren().clear();
            ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = languageSupport.get().getPossiblePieces(line);
            if (possiblePiecePackages != null && !possiblePiecePackages.isEmpty()) {
                for (IdeSpecialParser.PossiblePiecePackage entry : languageSupport.get().getPossiblePieces(line)) {
                    factoryOrder.add(entry);
                    Label filledIn = new Label(entry.getFilledIn());
                    Label notFilledIn = new Label(entry.getNotFilledIn());
                    filledIn.getStyleClass().add("highlighted-label");
                    filledIn.setFont(new Font(16));
                    notFilledIn.setFont(new Font(16));
                    HBox box = new HBox(filledIn, notFilledIn);
                    box.setOnMousePressed(popupItemEvent);
                    box.setAccessibleText(entry.getPutIn());
                    popupBox.getChildren().add(box);
                }
                if (!popupBox.getChildren().isEmpty()) {
                    popupBox.getChildren().get(0).getStyleClass().add("selected-syntax");
                    selectionIndex = 0;
                    autoCompletePopup.show(this.getScene().getWindow());
                    autoCompletePopup.setHeight(popupScrollPane.getHeight());
                    popupScrollPane.setVvalue(0);
                } else {
                    autoCompletePopup.hide();
                }
            } else {
                autoCompletePopup.hide();
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

    private class HighlightingThread extends Thread {

        private String text;

        public HighlightingThread() {
            super();
            this.setDaemon(true);
        }

        @Override
        public void run() {
            computeHighlighting(text);
        }


        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        private void computeHighlighting(String text) {
            Pattern pattern = languageSupport.get().generatePattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(text);
                int lastKwEnd = 0;
                StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
                while (matcher.find()) {
                    String styleClass = languageSupport.get().styleClass(matcher);
                    spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                    spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                    lastKwEnd = matcher.end();
                }
                spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
                StyleSpans<Collection<String>> styleSpans = spansBuilder.create();
                Platform.runLater(() -> {
                    try {
                        setStyleSpans(0, styleSpans);
                    } catch (IndexOutOfBoundsException ignored) {
                    }
                });
            }
        }



    }

}
