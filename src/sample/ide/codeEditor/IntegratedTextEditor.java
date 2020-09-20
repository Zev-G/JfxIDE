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
import sample.language.interpretation.SyntaxManager;
import sample.language.syntaxPiece.SyntaxPieceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegratedTextEditor extends CodeArea {

    // Static Fields

    private final ArrayList<String> ADDED_SYNTAX_PATTERNS = new ArrayList<>();
//
    private final String[] KEYWORDS = { "function", "if", "else" };
    private final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private final String COMMENT_PATTERN = "#[^\\n]*";
    private final String NUMBER_PATTERN = "[0-9]+";
    private final String VARIABLE_PATTERN = "\\{([^\"\\\\]|\\\\.)*?}|[^\\s]+(: | = )";
    private final String SPECIAL_PATTERN = "\\b([A-z]+-[A-z]+)\\b";
    private final String INPUT_PATTERN = "%([^\\s]*?)%";

    private final String EXPRESSION_PATTERN = generateSyntaxPattern(SyntaxManager.getAllExpressionFactories());
    private final String EFFECT_PATTERN = generateSyntaxPattern(SyntaxManager.EFFECT_FACTORIES);
    private final String EVENT_PATTERN = generateSyntaxPattern(SyntaxManager.EVENT_FACTORIES);

    private final Pattern PATTERN = Pattern.compile("" +
            "(?<COMMENT>" + COMMENT_PATTERN + ")" +
            "|(?<STRING>" + STRING_PATTERN + ")" +
            "|(?<VARIABLE>" + VARIABLE_PATTERN + ")" +
            "|(?<NUMBER>" + NUMBER_PATTERN + ")" +
            "|(?<SPECIAL>" + SPECIAL_PATTERN + ")" +
            "|(?<INPUT>" + INPUT_PATTERN + ")" +
            "|(?<EXPRESSION>" + EXPRESSION_PATTERN + ")" +
            "|(?<EFFECT>" + EFFECT_PATTERN + ")" +
            "|(?<EVENT>" + EVENT_PATTERN + ")" +
            "|(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
//            "|(?<>" +  + ")" +
            "");



//    private final Pattern PATTERN = Pattern.compile("(?<COMMENT>#[^\\n]*)|(?<KEYWORD>\\b(function|if|else)\\b)|(?<STRING>\"([^\"\\\\]|\\\\.)*\")|(?<VARIABLE>\\{([^\"\\\\]|\\\\.)*?})|(?<NUMBER>[0-9]+)|(?<EXPRESSION>\\b((|new )vbox|title property of|text property of|(|new )label|(|new )stage|(|new )pane|(|new )background colo(u|)red|with corner radius|(|new )button|(|new )hbox|files in|random number between|and|random integer between|children of|web colo(u|)r|blue|red|green|yellow|dark blue|dark red|dark green|light blue|light green|light yellow|aqua|beige|black|brown|cyan|dark grey|grey|light grey|gold|indigo|lime|magenta|maroon|navy|purple|silver|snow|white|teal|violet|without( the|) last character|without( the|) first character|without character (at |)|text of|path of|text in|computer is connected to the internet|free space in|length of|space taken up by|size of|value of|(load file|file loaded) from|is a multiple of|\\!|appended to|true|false)\\b)|(?<EFFECT>\\b(create new file|create new directory|delete|move|to|write|return|set|remove|from|add|set title of|show|print|stop program|set fill color of|put|into|set text of|set background color of|set scale x of|set scale y of|set scale of|set background of|push|to front|to back|set rotation of|set style of|set opacity of|disable|enable|set value of|bind|transition scale of|over|second(s|)|transition x position of|transition y position of|transition position of|transition opacity of|(|set )full screen|(|set )(normal|unfull) screen|(|set )maximize|(|set )un maximize|make|resizable|not resizable|hide|set icon of|visible|invisible|mouse transparent|not mouse transparent|set spacing of vbox|set spacing of hbox)\\b)|(?<EVENT>\\b((when|on)|is pressed|is clicked|(when|on) mouse drag enters|(when|on) mouse drag exits|(when|on) mouse moves over|is released|(when|on) drag is detected for|(when|on) mouse enters|(when|on) mouse exits|(when|on) drag is done for|(when|on) drag is dropped on|(when|on) drag enters|(when|on) drag exits|(when|on) drag is over|(when|on) key is pressed for|(when|on) key is released for|(when|on) key is typed for|changes|$expression (.*?)|in|every|chance of|loop|times)\\b)");


    // Non Static Fields

    private final ArrayList<IdeSpecialParser.PossiblePiecePackage<SyntaxPieceFactory>> factoryOrder = new ArrayList<>();
    private final VBox popupBox = new VBox();
    private final ScrollPane popupScrollPane = new ScrollPane(popupBox);
    private final Popup autoCompletePopup = new Popup();
    private final ArrayList<String> selectionQueue = new ArrayList<>();

    private int selectionIndex = 0;


    private final EventHandler<MouseEvent> popupItemEvent = mouseEvent -> {
        if (mouseEvent.getSource() instanceof Node) {
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
//        System.out.println(PATTERN);
        popupScrollPane.setOnKeyPressed(keyEvent -> {
            keyEvent.consume();
            System.out.println(keyEvent.getCode());
            if (keyEvent.getCode() == KeyCode.TAB) {
                String text = factoryOrder.get(selectionIndex).getNotFilledIn();
                this.insertText(this.getCaretPosition(), text);
                selectionQueue.clear();
                selectNext();
            }
        });
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));
        this.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change -> {
            HighlightingThread highlightingThread = new HighlightingThread();
            highlightingThread.setText(this.getText());
            highlightingThread.start();
        });

        this.getStylesheets().add(IntegratedTextEditor.class.getResource("ide.css").toExternalForm());
        popupScrollPane.getStylesheets().add(Ide.class.getResource("main.css").toExternalForm());
        autoCompletePopup.getContent().add(popupScrollPane);

        this.addEventHandler( KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.W) {
                keyEvent.consume();
                this.selectWord();
            }
        });
        Pattern whiteSpace = Pattern.compile( "^\\s+" );

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

        this.popupScrollPane.setMaxHeight(300);

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
                if (keyCode == KeyCode.QUOTE || keyCode == KeyCode.OPEN_BRACKET) {
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
            } else if (keyCode == KeyCode.OPEN_BRACKET && !textAfterCaret.startsWith("}") && !keyEvent.isConsumed()) {
                Platform.runLater( () -> {
                    this.insertText(this.getCaretPosition(), "}");
                    this.moveTo(this.getCaretPosition() - 1);
                });
            } else if (keyCode == KeyCode.QUOTE && !textAfterCaret.startsWith("\"") && !keyEvent.isConsumed()) {
                Platform.runLater(() -> {
                    this.insertText(this.getCaretPosition(), "\"");
                    this.moveTo(this.getCaretPosition() - 1);
                });
            }
            if (keyCode == KeyCode.ENTER) {
                Matcher m = whiteSpace.matcher(line);
                Platform.runLater( () -> this.insertText( this.getCaretPosition(), (m.find() ? m.group() : "") + (line.endsWith(":") ? "  " : "")));
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
                        System.out.println("Text");
                        keyEvent.consume();
                        String text = factoryOrder.get(selectionIndex).getNotFilledIn();
                        if (text.length() > 0 && this.getSelectedText().equals("")) {
                            this.insertText(this.getCaretPosition(), text);
                            selectionQueue.clear();
                            selectNext();
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
            ArrayList<SyntaxPieceFactory> allSyntaxPieceFactories = new ArrayList<>();
            allSyntaxPieceFactories.addAll(SyntaxManager.EFFECT_FACTORIES);
            allSyntaxPieceFactories.addAll(SyntaxManager.EVENT_FACTORIES);
//            allSyntaxPieceFactories.addAll(SyntaxManager.getAllExpressionFactories());
            factoryOrder.addAll(IdeSpecialParser.possibleSyntaxPieces(line, allSyntaxPieceFactories));
            popupBox.getChildren().clear();
            for (IdeSpecialParser.PossiblePiecePackage<SyntaxPieceFactory> entry : factoryOrder) {
                Label filledIn = new Label(entry.getFilledIn());
                Label notFilledIn = new Label(entry.getNotFilledIn());
                filledIn.getStyleClass().add("highlighted-label");
                filledIn.setFont(new Font(16));
                notFilledIn.setFont(new Font(16));
                HBox box = new HBox(filledIn, notFilledIn);
                box.setOnMousePressed(popupItemEvent);
                box.setAccessibleText(entry.getNotFilledIn());
                popupBox.getChildren().add(box);
            }
            if (!popupBox.getChildren().isEmpty()) {
                popupBox.getChildren().get(0).getStyleClass().add("selected-syntax");
                selectionIndex = 0;
                autoCompletePopup.show(this.getScene().getWindow());
                autoCompletePopup.setHeight(popupScrollPane.getHeight());
            } else {
                autoCompletePopup.hide();
            }
        } else {
            autoCompletePopup.hide();
        }
    }

    private <T extends SyntaxPieceFactory> String generateSyntaxPattern(Collection<T> factories) {
        ArrayList<String> patterns = new ArrayList<>();
        for (SyntaxPieceFactory factory : factories) {
            if (!factory.getUsage().contains("IGNORE")) {
                String[] pieces = factory.getRegex().split("( %(.*?)%|%(.*?)% |%(.*?)%)");
                for (String piece : pieces) {
                    piece = piece.trim();
                    if (!ADDED_SYNTAX_PATTERNS.contains(piece) &&
                            !piece.contains("\"") && !piece.contains("[0-9]")) {
                        patterns.add(piece);
                        ADDED_SYNTAX_PATTERNS.add(piece);
                    }
                }
            }
        }
        String pattern = String.join("|", patterns);
        pattern = pattern.replaceAll("\\|\\|", "|");
        return "\\b(" + pattern + ")\\b";
    }



    private class HighlightingThread extends Thread {

        private String text;

        public HighlightingThread() {
            super();
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
            Matcher matcher = PATTERN.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            while(matcher.find()) {
                String styleClass =
                        matcher.group("STRING") != null ? "string" :
                        matcher.group("COMMENT") != null ? "comment" :
                        matcher.group("NUMBER") != null ? "number" :
                        matcher.group("VARIABLE") != null ? "variable" :
                        matcher.group("SPECIAL") != null ? "special" :
                        matcher.group("INPUT") != null ? "input" :
                        matcher.group("EXPRESSION") != null ? "expression" :
                        matcher.group("EFFECT") != null ? "effect" :
                        matcher.group("EVENT") != null ? "event" :
                        matcher.group("KEYWORD") != null ? "keyword" :
                        null;
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
