package tmw.me.com.ide.codeEditor.languages;

import javafx.application.Platform;
import javafx.scene.control.IndexRange;
import javafx.scene.text.Text;
import tmw.me.com.betterfx.TextModifier;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.languages.components.Behavior;
import tmw.me.com.ide.tools.concurrent.schedulers.ChangeListenerScheduler;
import tmw.me.com.language.FXScript;
import tmw.me.com.language.interpretation.parse.Parser;
import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.language.syntaxPiece.SyntaxPieceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This file contains the main language support for the IDE, {@link LanguageSupport} contains lots of information on the methods used whereas the docs here refer more to the specific elements of this class.
 */
public class SfsLanguage extends LanguageSupport {

    private final ArrayList<IndexRange> highlightedVariables = new ArrayList<>();
    private ChangeListenerScheduler<Integer> caretListener;
    private ChangeListenerScheduler<String> textListener;

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
        caretListener = new ChangeListenerScheduler<>(200, (observableValue, integer, t1) -> {
            if (!t1.equals(integer) && integratedTextEditor.getFindAndReplace().getFindSelectedIndex() < 0) {
                String fullText = integratedTextEditor.getText();
                for (IndexRange indexRange : highlightedVariables) {
                    if (indexRange.getStart() > 0 && indexRange.getStart() < fullText.length() && indexRange.getEnd() < fullText.length()) {
                        Collection<String> collection = new ArrayList<>(integratedTextEditor.getStyleAtPosition(indexRange.getStart() + 1));
                        if (collection.contains("selected-word")) {
                            collection.remove("selected-word");
                            integratedTextEditor.setStyle(indexRange.getStart(), indexRange.getEnd(), collection);
                        }
                    }
                }
                highlightedVariables.clear();
                int[] range = integratedTextEditor.expandFromPoint(t1, '{', '}', ' ', '%');
                if (range[0] > 0 && range[1] < fullText.length()) {
                    String text = integratedTextEditor.getText(range[0], range[1]);
                    if (text.startsWith("{") && text.endsWith("}")) {
                        for (IndexRange variableRange : integratedTextEditor.allInstancesOfStringInString(fullText, text)) {
                            if (variableRange.getStart() + 1 < fullText.length()) {
                                Collection<String> collection = new ArrayList<>(integratedTextEditor.getStyleAtPosition(variableRange.getStart() + 1));
                                if (!collection.isEmpty()) {
                                    collection.add("selected-word");
                                    highlightedVariables.add(variableRange);
                                    integratedTextEditor.setStyle(variableRange.getStart(), variableRange.getEnd(), collection);
                                }
                            }
                        }
                    }
                }
            }
        });
        SyntaxManager syntaxManager = new SyntaxManager();
        textListener = new ChangeListenerScheduler<>(600, false, (observableValue, s, t1) -> {
            FXScript.restart(syntaxManager);
            ArrayList<Integer> errors = new ArrayList<>();
            Parser parser = new Parser(syntaxManager, parseError -> errors.add(parseError.getLineNumber()));
            parser.parseChunk(t1, null);
            Platform.runLater(() -> integratedTextEditor.getErrorLines().setAll(errors));
        });
        integratedTextEditor.caretPositionProperty().addListener(caretListener);
        integratedTextEditor.textProperty().addListener(textListener);
        return null;
    }

    @Override
    public Behavior[] removeBehaviour(IntegratedTextEditor integratedTextEditor) {
        super.removeBehaviour(integratedTextEditor);
        integratedTextEditor.caretPositionProperty().removeListener(caretListener);
        integratedTextEditor.textProperty().removeListener(textListener);
        return null;
    }

    private final ArrayList<String> ADDED_SYNTAX_PATTERNS = new ArrayList<>();
    private static final String[] KEYWORDS = { "function", "if", "else" };
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "#[^\\n]*";
    private static final String VARIABLE_PATTERN = "\\{([^\"\\\\]|\\\\.)*?}|[^\\s]+(: | = )";
    private static final String SPECIAL_PATTERN = "\\b([A-z]+-[A-z]+)\\b";
    private static final String INPUT_PATTERN = "%([^\\s]*?)%";
    private static final String SYNTAX_START_PATTERN = "(expression|effect) (.*?)-";
    private static final String SYNTAX_END_PATTERN = "> (.*?):";

    /**
     * This hashmap represents the relationship between a snippet's activation code and it's insert code.
     */
    private static final HashMap<String, String> SNIPPETS = new HashMap<>();
    static {
        SNIPPETS.put("@def", "set {stage} to new stage\nset {vbox} to new vbox\nset {button} to new button\nwhen {button} is pressed:\n  set text of {button} to \"Pressed\"\nadd {button} to children of {vbox}\nput {vbox} into {stage}\nset text of {button} to \"Some simple text\"\nshow {stage}");
        SNIPPETS.put("function", "function %type% %name%():");
    }

    private final String EXPRESSION_PATTERN = generateSyntaxPattern(SyntaxManager.SYNTAX_MANAGER.getAllExpressionFactories());
    private final String EFFECT_PATTERN = generateSyntaxPattern(SyntaxManager.SYNTAX_MANAGER.EFFECT_FACTORIES);
    private final String EVENT_PATTERN = generateSyntaxPattern(SyntaxManager.SYNTAX_MANAGER.EVENT_FACTORIES);

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
            "|(?<EXPRESSION>" + EXPRESSION_PATTERN + ")" +
            "|(?<EFFECT>" + EFFECT_PATTERN + ")" +
            "|(?<EVENT>" + EVENT_PATTERN + ")" +
            "|(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
            "");

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
                    if (!ADDED_SYNTAX_PATTERNS.contains(piece) &&
                            !piece.contains("\"") && !piece.contains("[0-9]")) {
                        patterns.add(piece);
                        ADDED_SYNTAX_PATTERNS.add(piece);
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
            CodeChunk chunk = parser.parseChunk(textEditor.getText(), null);
            System.out.println("Finished Parsing. Running");
            chunk.run();
        }
//        Parser parser = new Parser(SyntaxManager.SYNTAX_MANAGER, parseError -> {});

    }
}
