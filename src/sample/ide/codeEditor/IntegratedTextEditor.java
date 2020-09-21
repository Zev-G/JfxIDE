package sample.ide.codeEditor;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Window;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import sample.ide.Ide;
import sample.ide.IdeSpecialParser;
import sample.ide.codeEditor.languages.LanguageSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegratedTextEditor extends CodeArea {


    private final LanguageSupport language;

    private final ArrayList<String> factoryOrder = new ArrayList<>();
    private final VBox popupBox = new VBox();
    private final ScrollPane popupScrollPane = new SmoothishScrollpane(popupBox);
    private final Popup autoCompletePopup = new Popup();
    private final ArrayList<String> selectionQueue = new ArrayList<>();

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

    public IntegratedTextEditor() {
        this(LanguageSupport.getSFS());
    }
    public IntegratedTextEditor(LanguageSupport languageSupport) {
        language = languageSupport;

        // Highlighting
        this.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change -> {
            HighlightingThread highlightingThread = new HighlightingThread();
            highlightingThread.setText(this.getText());
            highlightingThread.start();
        });

        // Value tweaking and value setting
        this.popupScrollPane.setMaxHeight(300);
        this.getStylesheets().add(IntegratedTextEditor.class.getResource("ide.css").toExternalForm());
        this.getStylesheets().add(language.getStyleSheet());
        popupScrollPane.getStylesheets().add(Ide.class.getResource("styles/main.css").toExternalForm());
        autoCompletePopup.getContent().add(popupScrollPane);
        autoCompletePopup.setAutoHide(true);
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));


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
                Platform.runLater( () -> this.insertText(this.getCaretPosition(), (line.trim().startsWith("#") ? "#" : "") + (m.find() ? m.group() : "") + (line.endsWith(":") ? "  " : "")));
            } else if (keyEvent.isShiftDown()) {
                if (keyCode == KeyCode.TAB) {
                    Matcher matcher = whiteSpace.matcher(line);
                    if (matcher.find()) {
                        String whiteSpaceInLine = matcher.group();
                        if (whiteSpaceInLine.startsWith("\t") || whiteSpaceInLine.startsWith("  ")) {
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
                }
            } else if (keyCode == KeyCode.TAB) {
                if (!keyEvent.isControlDown() && autoCompletePopup.isShowing()) {
                    if (factoryOrder.size() > selectionIndex) {
                        keyEvent.consume();
                        String text = factoryOrder.get(selectionIndex);
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
                }
            }
        });
        Runnable runWhenChanged = () -> {
            if (this.caretBoundsProperty().getValue().isPresent()) {
                Bounds bounds = this.caretBoundsProperty().getValue().get();
                Platform.runLater(() -> {
                    autoCompletePopup.setX(bounds.getMaxX());
                    autoCompletePopup.setY(bounds.getMaxY());
                });
            }
        };
        this.caretBoundsProperty().addListener((observableValue, integer, t1) -> runWhenChanged.run());
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

    public void insertAutocomplete() {
        String text = factoryOrder.get(selectionIndex);
        int lineStart = 0;
        for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
            lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
        }
        String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
        Pattern whiteSpace = Pattern.compile( "^\\s+" );
        Matcher matcher = whiteSpace.matcher(line);
        if (matcher.find()) {
            text = matcher.group() + text;
        }
        this.replaceText(lineStart, this.getCaretPosition(), text);
        selectionQueue.clear();
        selectNext();
    }

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

    private int stringOccurrences(String string, char checkFor) {
        int occurrences = 0;
        for (char c : string.toCharArray()) {
            if (c == checkFor) occurrences++;
        }
        return occurrences;
    }

    private void fillBox(String line) {
        if (line.trim().length() > 0) {
            factoryOrder.clear();
            popupBox.getChildren().clear();
            ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = language.getPossiblePieces(line);
            if (possiblePiecePackages != null && !possiblePiecePackages.isEmpty()) {
                for (IdeSpecialParser.PossiblePiecePackage entry : language.getPossiblePieces(line)) {
                    factoryOrder.add(entry.getPutIn());
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
            Matcher matcher = language.generatePattern().matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            while(matcher.find()) {
                String styleClass = language.styleClass(matcher);
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastKwEnd = matcher.end();
            }
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
            StyleSpans<Collection<String>> styleSpans = spansBuilder.create();
            Platform.runLater(() -> {
                try {
                    setStyleSpans(0, styleSpans);
                } catch (IndexOutOfBoundsException ignored) {}
            });
        }



    }

}
