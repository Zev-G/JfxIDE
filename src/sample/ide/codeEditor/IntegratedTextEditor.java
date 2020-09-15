package sample.ide.codeEditor;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Window;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.w3c.dom.ranges.Range;
import sample.ide.Ide;
import sample.ide.IdeSpecialParser;
import sample.test.interpretation.SyntaxManager;
import sample.test.syntaxPiece.SyntaxPiece;
import sample.test.syntaxPiece.SyntaxPieceFactory;
import sample.test.syntaxPiece.effects.EffectFactory;

import java.awt.font.NumericShaper;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegratedTextEditor extends CodeArea {

    // Static Fields

    private final ArrayList<String> ADDED_SYNTAX_PATTERNS = new ArrayList<>();

    private final String[] KEYWORDS = { "function", "if", "else" };
    private final String KEYWORD_PATTERN = "(" + String.join("|", KEYWORDS) + ")";
    private final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private final String COMMENT_PATTERN = "#[^\\n]*";
    private final String NUMBER_PATTERN = "[0-9]+";
    private final String VARIABLE_PATTERN = "\\{([^\"\\\\]|\\\\.)*?}";

    private final String EXPRESSION_PATTERN = generateSyntaxPattern(SyntaxManager.getAllExpressionFactories());
    private final String EFFECT_PATTERN = generateSyntaxPattern(SyntaxManager.EFFECT_FACTORIES);
    private final String EVENT_PATTERN = generateSyntaxPattern(SyntaxManager.EVENT_FACTORIES);

    private final Pattern PATTERN = Pattern.compile("" +
            "(?<COMMENT>" + COMMENT_PATTERN + ")" +
            "|(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
            "|(?<STRING>" + STRING_PATTERN + ")" +
            "|(?<VARIABLE>" + VARIABLE_PATTERN + ")" +
            "|(?<NUMBER>" + NUMBER_PATTERN + ")" +
            "|(?<EFFECT>" + EFFECT_PATTERN + ")" +
            "|(?<EXPRESSION>" + EXPRESSION_PATTERN + ")" +
            "|(?<EVENT>" + EVENT_PATTERN + ")" +
//            "|(?<>" +  + ")" +
            "");


    // Non Static Fields

    private final ArrayList<IdeSpecialParser.PossiblePiecePackage<SyntaxPieceFactory>> factoryOrder = new ArrayList<>();
    private final VBox popupBox = new VBox();
    private final ScrollPane popupScrollPane = new ScrollPane(popupBox);
    private final Popup autoCompletePopup = new Popup();
    private final ArrayList<IndexRange> selectionQueue = new ArrayList<>();

    public IntegratedTextEditor() {
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));
        this.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change -> this.setStyleSpans(0, computeHighlighting(this.getText())));

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
                System.out.println(lastCharacter);
                int bracketsBefore = stringOccurrences(s, '{');
                int bracketsAfter = stringOccurrences(t1, '{');
                System.out.println(bracketsBefore + " " + bracketsAfter);
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

        this.addEventFilter( KeyEvent.KEY_PRESSED, keyEvent -> {
            String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
            KeyCode keyCode = keyEvent.getCode();
            String textAfterCaret = this.getText().substring(this.getCaretPosition());
            if (keyCode == KeyCode.ENTER) {
                Matcher m = whiteSpace.matcher(line);
                Platform.runLater( () -> this.insertText( this.getCaretPosition(), (m.find() ? m.group() : "") + (line.endsWith(":") ? "  " : "")));
            } else if (keyCode == KeyCode.OPEN_BRACKET && !textAfterCaret.startsWith("}")) {
                Platform.runLater( () -> {
                    this.insertText(this.getCaretPosition(), "}");
                    this.moveTo(this.getCaretPosition() - 1);
                });
            } else if (keyCode == KeyCode.QUOTE && !textAfterCaret.startsWith("\"")) {
                Platform.runLater( () -> {
                    this.insertText(this.getCaretPosition(), "\"");
                    this.moveTo(this.getCaretPosition() - 1);
                });
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
                if (autoCompletePopup.isShowing()) {
                    keyEvent.consume();
                    this.insertText(this.getCaretPosition(), factoryOrder.get(0).getNotFilledIn());
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
        this.caretBoundsProperty().addListener((observableValue, integer, t1) -> {
            runWhenChanged.run();
        });
        this.sceneProperty().addListener((observableValue, scene, t1) -> {
            Window t11 = t1.getWindow();
            t11.xProperty().addListener((observableValue11, number, t111) -> autoCompletePopup.hide());
            t11.yProperty().addListener((observableValue11, number, t111) -> autoCompletePopup.hide());
            t11.widthProperty().addListener((observableValue2, number, t12) -> autoCompletePopup.hide());
            t11.heightProperty().addListener((observableValue2, number, t12) -> autoCompletePopup.hide());
        });
        this.setOnMousePressed(mouseEvent -> autoCompletePopup.hide());

    }

    private int stringOccurrences(String string, char checkFor) {
        int occurrences = 0;
        for (char c : string.toCharArray()) {
            if (c == checkFor) occurrences++;
        }
        return occurrences;
    }

    private void fillBox(String line) {
        if (line.length() > 0) {
            factoryOrder.clear();
            System.out.println("---00--- Searching ---00---");
            ArrayList<SyntaxPieceFactory> allSyntaxPieceFactories = new ArrayList<>();
            allSyntaxPieceFactories.addAll(SyntaxManager.EFFECT_FACTORIES);
            allSyntaxPieceFactories.addAll(SyntaxManager.EVENT_FACTORIES);
//            allSyntaxPieceFactories.addAll(SyntaxManager.getAllExpressionFactories());
            factoryOrder.addAll(IdeSpecialParser.possibleSyntaxPieces(line, allSyntaxPieceFactories));
            System.out.println("Got: " + factoryOrder + "(size: " + factoryOrder.size() + ")");
            popupBox.getChildren().clear();
            for (IdeSpecialParser.PossiblePiecePackage<SyntaxPieceFactory> entry : factoryOrder) {
                Label label = new Label(entry.getSyntaxPieceFactory().getUsage());
                label.setFont(new Font(16));
                popupBox.getChildren().add(new HBox(label));
            }
            if (!popupBox.getChildren().isEmpty()) {
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
            String[] pieces = factory.getRegex().split("( %(.*?)%|%(.*?)% |%(.*?)%)");
            for (String piece : pieces) {
                piece = piece.trim();
                if (!ADDED_SYNTAX_PATTERNS.contains(piece) &&
                        !piece.contains("\"") && !piece.contains("[0-9]"))
                {
                    patterns.add(piece);
                    ADDED_SYNTAX_PATTERNS.add(piece);
                }
            }
        }
        String pattern = String.join("|", patterns);
        pattern = pattern.replaceAll("\\|\\|", "|");
        return "\\b(" + pattern + ")\\b";
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {
//            System.out.println(matcher.group("EXPRESSION"));
            String styleClass =
                matcher.group("KEYWORD") != null ? "keyword" :
                matcher.group("STRING") != null ? "string" :
                matcher.group("COMMENT") != null ? "comment" :
                matcher.group("NUMBER") != null ? "number" :
                matcher.group("VARIABLE") != null ? "variable" :
                matcher.group("EXPRESSION") != null ? "expression" :
                matcher.group("EFFECT") != null ? "effect" :
                matcher.group("EVENT") != null ? "event" :
                null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }


}
