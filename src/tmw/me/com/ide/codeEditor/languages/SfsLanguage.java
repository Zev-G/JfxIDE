package tmw.me.com.ide.codeEditor.languages;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyledSegment;
import org.reactfx.collection.LiveList;
import tmw.me.com.betterfx.TextModifier;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.highlighting.SimpleRangeStyleSpansFactory;
import tmw.me.com.ide.codeEditor.highlighting.StyleSpansFactory;
import tmw.me.com.ide.codeEditor.visualcomponents.tooltip.EditorTooltip;
import tmw.me.com.ide.tools.concurrent.schedulers.ChangeListenerScheduler;
import tmw.me.com.language.FXScript;
import tmw.me.com.language.interpretation.parse.Parser;
import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.language.syntaxPiece.SyntaxPieceFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This file contains the main language support for the IDE, {@link LanguageSupport} contains lots of information on the methods used whereas the docs here refer more to the specific elements of this class.
 */
public class SfsLanguage extends LanguageSupport {

    private static final boolean ERROR_HIGHLIGHTING = false;

    private static final String[] KEYWORDS = { "function", "if", "else" };
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "#[^\\n]*";
    private static final String VARIABLE_PATTERN = "\\{([^\"\\\\]|\\\\.)*?}|[^\\s]+(: | = )";
    private static final String SPECIAL_PATTERN = "\\b([A-z]+-[A-z]+)\\b";
    private static final String INPUT_PATTERN = "%([^\\s]*?)%";
    private static final String SYNTAX_START_PATTERN = "(expression|effect) (.*?)-";
    private static final String SYNTAX_END_PATTERN = "> (.*?):";

    private final ArrayList<String> addedSyntaxPatterns = new ArrayList<>();

    private final String expressionPattern = generateSyntaxPattern(SyntaxManager.SYNTAX_MANAGER.getAllExpressionFactories());
    private final String effectPattern = generateSyntaxPattern(SyntaxManager.SYNTAX_MANAGER.EFFECT_FACTORIES);
    private final String eventPattern = generateSyntaxPattern(SyntaxManager.SYNTAX_MANAGER.EVENT_FACTORIES);

    /**
     * This pattern is a collection of other fields connected with Reg-ex named sections.
     */
    private final Pattern PATTERN = Pattern.compile("" +
            "(?<COMMENT>" + COMMENT_PATTERN + ")" +
            "|(?<STRING>" + STRING_PATTERN + ")" +
            "|(?<VARIABLE>" + VARIABLE_PATTERN + ")" +
            "|(?<INPUT>" + INPUT_PATTERN + ")" +
            "|(?<SYNTAX1>" + SYNTAX_START_PATTERN + ")" +
            "|(?<SYNTAX2>" + SYNTAX_END_PATTERN + ")" +
            "|(?<NUMBER>" + NUMBER_PATTERN + ")" +
            "|(?<SPECIAL>" + SPECIAL_PATTERN + ")" +
            "|(?<EXPRESSION>" + expressionPattern + ")" +
            "|(?<EFFECT>" + effectPattern + ")" +
            "|(?<EVENT>" + eventPattern + ")" +
            "|(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
            "");

    private final ArrayList<IndexRange> highlightedVariables = new ArrayList<>();
    private ChangeListenerScheduler<Integer> caretListener;
    private ChangeListenerScheduler<String> textListener;

    /**
     * This hashmap represents the relationship between a snippet's activation code and it's insert code.
     */
    private static final HashMap<String, String> SNIPPETS = new HashMap<>();
    static {
        SNIPPETS.put("@def", "set {stage} to new stage\nset {vbox} to new vbox\nset {button} to new button\nwhen {button} is pressed:\n  set text of {button} to \"Pressed\"\nadd {button} to children of {vbox}\nput {vbox} into {stage}\nset text of {button} to \"Some simple text\"\nshow {stage}");
        SNIPPETS.put("function", "function %type% %name%():");
    }

    public SfsLanguage() {
        super(SfsLanguage.class.getResource("styles/sfs.css").toExternalForm(), "Software Scripting");
        runnable = true;
        usingAutoComplete = true;
    }

    /**
     * This adds some important functionality to the text editor. The first feature is that all instances of a variable are highlighted.
     * The second feature is that any lines with an error on them are highlighted on the {@link tmw.me.com.ide.codeEditor.LineGraphicFactory}, this is done by updating {@link IntegratedTextEditor#getErrorLines()}
     * <p>
     *     This method makes use of the {@link ChangeListenerScheduler} to stop lag when the cursor is moved around a lot or tons of text is typed repeatedly, if any expensive calculations need to be done
     *     frequently it is recommended that this class is used.
     * </p>
     * @param integratedTextEditor A reference to the {@link IntegratedTextEditor} which all functionality should be added onto.
     */
    public Behavior[] addBehaviour(IntegratedTextEditor integratedTextEditor) {
        caretListener = new ChangeListenerScheduler<>(150, (observableValue, integer, t1) -> {
            if ((integratedTextEditor.getSelectedText() == null || integratedTextEditor.getSelectedText().length() <= 1) && !t1.equals(integer) && integratedTextEditor.getFindAndReplace().getFindSelectedIndex() < 0) {
                String fullText = integratedTextEditor.getText();
                highlightedVariables.clear();
                int[] range = integratedTextEditor.expandFromPoint(t1, '{', '}', ' ', '%');
                if (range[0] > 0 && range[1] <= fullText.length()) {
                    String text = integratedTextEditor.getText(range[0], range[1]);
                    if (text.startsWith("{") && text.endsWith("}")) {
                        for (IndexRange variableRange : integratedTextEditor.allInstancesOfStringInString(fullText, text)) {
                            if (variableRange.getStart() <= fullText.length()) {
                                Collection<String> collection = new ArrayList<>(integratedTextEditor.getStyleAtPosition(variableRange.getStart() + 1));
                                if (!collection.isEmpty()) {
                                    highlightedVariables.add(variableRange);
                                }
                            }
                        }
                    }
                }
                integratedTextEditor.highlight();
            }
        });
        SyntaxManager syntaxManager = new SyntaxManager();
        textListener = new ChangeListenerScheduler<>(600, false, (observableValue, s, t1) -> {
            FXScript.restart(syntaxManager);
            ArrayList<Integer> errors = new ArrayList<>();
            Parser parser = new Parser(syntaxManager, parseError -> errors.add(parseError.getLineNumber()));
            parser.parseChunk(integratedTextEditor.getTabbedText(), null);
            Platform.runLater(() -> integratedTextEditor.getErrorLines().setAll(errors));
        });
        if (ERROR_HIGHLIGHTING) {
            integratedTextEditor.textProperty().addListener(textListener);
        }
        integratedTextEditor.caretPositionProperty().addListener(caretListener);
        return null;
    }

    @Override
    public Behavior[] removeBehaviour(IntegratedTextEditor integratedTextEditor) {
        super.removeBehaviour(integratedTextEditor);
        integratedTextEditor.caretPositionProperty().removeListener(caretListener);
        integratedTextEditor.textProperty().removeListener(textListener);
        return null;
    }

    @Override
    public StyleSpansFactory<Collection<String>> getCustomStyleSpansFactory(IntegratedTextEditor editor) {
        return new SimpleRangeStyleSpansFactory(editor, Collections.singleton("selected-word"), highlightedVariables.toArray(new IndexRange[0]));
    }

    /**
     *
     * @return {@link SfsLanguage#PATTERN}
     */
    @Override
    public Pattern generatePattern() {
        return PATTERN;
    }

    /**
     *
     * @param matcher The matcher which the style class should be determined from, calling {@link Matcher#find()} will break highlighting.
     * @return Connects the matcher group to the style class.
     */
    @Override
    public String styleClass(Matcher matcher) {
        return
            matcher.group("COMMENT") != null ? "comment" :
            matcher.group("STRING") != null ? "string" :
            matcher.group("VARIABLE") != null ? "variable" :
            matcher.group("INPUT") != null ? "input" :
            matcher.group("SYNTAX1") != null ? "syntax-start" :
            matcher.group("SYNTAX2") != null ? "syntax-finish" :
            matcher.group("NUMBER") != null ? "number" :
            matcher.group("SPECIAL") != null ? "special" :
            matcher.group("EXPRESSION") != null ? "expression" :
            matcher.group("EFFECT") != null ? "effect" :
            matcher.group("EVENT") != null ? "event" :
            matcher.group("KEYWORD") != null ? "keyword" :
            null;
    }

    /**
     * This helps generate the pattern which is used in this class' highlighting.
     */
    private <T extends SyntaxPieceFactory> String generateSyntaxPattern(Collection<T> factories) {
        ArrayList<String> patterns = new ArrayList<>();
        for (SyntaxPieceFactory factory : factories) {
            if (!factory.getUsage().contains("IGNORE")) {
                String[] pieces = factory.getRegex().split("( %(.*?)%|%(.*?)% |%(.*?)%)");
                for (String piece : pieces) {
                    piece = piece.trim();
                    if (!addedSyntaxPatterns.contains(piece) &&
                            !piece.contains("\"") && !piece.contains("[0-9]")) {
                        patterns.add(piece);
                        addedSyntaxPatterns.add(piece);
                    }
                }
            }
        }
        String pattern = String.join("|", patterns);
        if (pattern.startsWith("|")) {
            pattern = pattern.substring(1);
        }
        pattern = pattern.replaceAll("\\|\\|", "|");
        return "\\b(" + pattern + ")\\b";
    }

    /**
     *
     * @param line The line which the user is typing on.
     * @return A list of {@link tmw.me.com.ide.IdeSpecialParser.PossiblePiecePackage} which is curated from the possible effect and events grabbed from what the user has punched in and from the valid snippets.
     */
    @Override
    public ArrayList<IdeSpecialParser.PossiblePiecePackage> getPossiblePieces(String line) {
        line = line.trim();
        ArrayList<SyntaxPieceFactory> allSyntaxPieceFactories = new ArrayList<>();
        allSyntaxPieceFactories.addAll(SyntaxManager.SYNTAX_MANAGER.EFFECT_FACTORIES);
        allSyntaxPieceFactories.addAll(SyntaxManager.SYNTAX_MANAGER.EVENT_FACTORIES);
        ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = new ArrayList<>(IdeSpecialParser.possibleSyntaxPieces(line, allSyntaxPieceFactories));
        for (Map.Entry<String, String> entry : SNIPPETS.entrySet()) {
            if (entry.getKey().startsWith(line)) {
                possiblePiecePackages.add(new IdeSpecialParser.PossiblePiecePackage(line, entry.getKey().substring(line.length()), entry.getValue(), true));
            }
        }
        return possiblePiecePackages;
    }

    /**
     * This runs the code in the specified textEditor, it also prints the errors to the IDE's console allowing them to be pressed as to highlight their relevant lines.
     * @param textEditor A reference to the text editor this language is attached to.
     * @param ide A reference to the Ide that is running the code.
     */
    @Override
    public void run(IntegratedTextEditor textEditor, Ide ide) {
        super.run(textEditor, ide);
        FXScript.restart();
        System.out.println("Parsing code...");
        if (ide != null) {
            SyntaxManager manager = new SyntaxManager();
            manager.printHandler = s -> ide.getRunConsole().addText(s, true);
            Parser parser = new Parser(manager, gotten -> {
                String[] longMessageLines = gotten.createLongErrorMessage().split("\n");
                for (String line : longMessageLines) {
                    if (line.startsWith("\t\tNumber: ")) {
                        Text textButton = ide.getRunConsole().genButton(line.replaceFirst("\t\t", ""), TextModifier.colorFromChar('c'));
                        textButton.setUnderline(true);
                        int num = Integer.parseInt(line.replaceFirst("\t\tNumber: ", ""));

                        textButton.setOnMousePressed(mouseEvent -> textEditor.selectRange(textEditor.absolutePositionFromLine(num - 1), textEditor.absolutePositionFromLine(num) - 1));
                        ide.getRunConsole().addTexts(ide.getRunConsole().getDefaultText("\t\t"), textButton, ide.getRunConsole().getDefaultText("\n"));
                    } else {
                        ide.getRunConsole().addTexts(ide.getRunConsole().genText("&c" + line + "\n"));
                    }
                }
            });
            System.out.println(textEditor.getTabbedText() + "\t");
            CodeChunk chunk = parser.parseChunk(textEditor.getTabbedText().replaceAll("\t", "  "), null);
            System.out.println("Finished Parsing. Running");
            chunk.run();
        }
//        Parser parser = new Parser(SyntaxManager.SYNTAX_MANAGER, parseError -> {});

    }

    @Override
    public boolean showingTooltip(EditorTooltip tooltip, int pos) {
        IntegratedTextEditor editor = tooltip.getEditor(); // Utility variable to store the editor which the tooltip is attached to.
        StyledSegment<String, Collection<String>> segmentAtPos = editor.getSegmentAtPos(pos); // Gets and stores the segment which the user hovered over.
        int line = editor.lineFromAbsoluteLocation(pos); // Stores the line which the user hovered over.
        // Insures that the segment isn't null and that the segment contains the "variable" style since we only want to show lines for variables.
        if (segmentAtPos != null && segmentAtPos.getStyle().contains("variable")) {
            VBox variableReferences = new VBox(); // This VBox is used to layout the entire style, it is used as the content for the tooltip.
            variableReferences.setSpacing(4); // Add some spacing between the lines.
            ArrayList<Node> newChildren = new ArrayList<>();
            int pars = editor.getParagraphs().size(); // Store the number of lines.
            int found = 0; // Store the number of lines containing the variable we've found. This is later used to insure that we don't show more than our max number of lines.
            int lastIndex = -1; // Store the last location we found a line containing the variable at.
            LiveList<Paragraph<Collection<String>, String, Collection<String>>> paragraphs = editor.getParagraphs(); // Store the list of paragraphs.
            int totalDigits = (int) (Math.log(paragraphs.size() - 1));
            System.out.println("total digits: " + totalDigits);
            // Loop through the paragraphs.
            for (int i = 0; i < pars; i++) {
                Paragraph<Collection<String>, String, Collection<String>> par = paragraphs.get(i); // The loop-paragraph.
                if (par.getText().contains(segmentAtPos.getSegment())) {
                    // Check if we have yet to find 12 lines.
                    TextFlow flow = new TextFlow(); // The TextFlow which is used to display the code on the line.
                    // Loop through the segments in the paragraph.
                    for (StyledSegment<String, Collection<String>> segment : par.getStyledSegments()) {
                        // Check if the segment is the variable-containing segment, if it is add the "tooltip-highlight" style class to the list of styles.
                        if (segment.getSegment().equals(segmentAtPos.getSegment())) {
                            ArrayList<String> styles = new ArrayList<>(segment.getStyle());
                            styles.add("tooltip-highlight");
                            segment = new StyledSegment<>(segment.getSegment(), styles);
                        }
                        flow.getChildren().add(editor.copySegment(segment)); // Add the segment to the text flow.
                    }
                    int digitsInI = (int) (Math.log10(i + 1) + 1);
                    System.out.println("digits in i: " + digitsInI + " log10: " + Math.log10(i));
                    TextExt lineNum = new TextExt(" ".repeat(totalDigits - digitsInI) + (i + 1) + ":"); // Creates the TextExt node used to display the line number.
                    lineNum.getStyleClass().addAll("apply-font", "underline-hover-label");
                    lineNum.setCursor(Cursor.HAND);
                    int finalI = i;
                    lineNum.setOnMousePressed(event -> {
                        tooltip.hide();
                        editor.selectLine(finalI);
                        editor.showParagraphAtTop(finalI);
                    });
                    Pane lineNumHolder = new BorderPane(lineNum); // Create a BorderPane to store the line number in.
                    HBox layoutBox = new HBox(lineNumHolder, flow); // The HBox which stores the line number and the line's code in it.
                    layoutBox.setSpacing(editor.getFontSize());
                    layoutBox.getStylesheets().addAll(Ide.STYLE_SHEET, IntegratedTextEditor.STYLE_SHEET, editor.getLanguage().getStyleSheet()); // Add the relevant stylesheets to the layoutBox.
                    // Highlight the line if it is the line which the tooltip was sourced from.
                    if (line == i) {
                        layoutBox.getStyleClass().add("lighter-background");
                    }
                    // Add the separator if we've skipped one or more lines between the one we're currently showing and the last one which was added.
                    if (lastIndex >= 0 && lastIndex + 1 != i) {
                        BorderPane dividerPane = new BorderPane();
                        dividerPane.getStyleClass().add("tooltip-divider");
                        newChildren.add(dividerPane);
                    }
                    newChildren.add(layoutBox); // Add the line's code to the VBox.
                    lastIndex = i; // Update the lastIndex.
                    found++; // Update the number of lines found.
                }
            }
            // Add the "...and x more" text to the VBox.
            variableReferences.getChildren().addAll(found > 12 ? newChildren.subList(0, 12) : newChildren);
            ScrollPane scroller = new ScrollPane(variableReferences);
            if (found > 12) {
                Label loadMore = new Label("...and " + (found - 12) + " more");
                loadMore.setCursor(Cursor.HAND);
                loadMore.getStyleClass().add("underline-hover-label");
                variableReferences.getChildren().add(loadMore);
                loadMore.setOnMouseReleased(event -> {
                    variableReferences.getChildren().remove(loadMore);
                    variableReferences.getChildren().addAll(newChildren.subList(12, newChildren.size()));
                    scroller.setPrefWidth(scroller.getWidth() + editor.getFontSize());
                });
            }
            scroller.getStyleClass().add("ac-scroller");
            scroller.setFitToWidth(true);
            variableReferences.getStyleClass().add("ac-items-box");
            tooltip.setContent(scroller); // Set the content of the tooltip
            return true;
        }
        return false; // Return false if we aren't hovering over a variable.
    }
}
