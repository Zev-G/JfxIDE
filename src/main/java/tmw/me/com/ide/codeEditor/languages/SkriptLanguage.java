package tmw.me.com.ide.codeEditor.languages;

import tmw.me.com.Resources;
import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.visualcomponents.tooltip.EditorTooltip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkriptLanguage extends LanguageSupport {

    private static final String FOLDER = Resources.EDITOR + "languages/skript/";

    private static final String[] KEYWORDS = { "function", "if", "else", "command", "trigger", "return" };
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "#[^\\n#]*";
    private static final String VARIABLE_PATTERN = "\\{([^\"\\\\]|\\\\.)*?}|[^\\s]+: ";
    private static final String SPECIAL_PATTERN = "\\b([A-z]+-[A-z]+)\\b";
    private static final String INPUT_PATTERN = "%([^\\s]*?)%";

    private static final ArrayList<String> addedSyntaxPatterns = new ArrayList<>();

    private static final String EXPRESSION_PATTERN = generateSyntaxPattern("expressions");
    private static final String EFFECT_PATTERN = generateSyntaxPattern("effects");
    private static final String EVENT_PATTERN = generateSyntaxPattern("events");

    private static final ArrayList<String> AUTOCOMPLETE_ENTRIES = generateAutocompleteEntries();

    private static ArrayList<String> generateAutocompleteEntries() {
        ArrayList<String> strings = new ArrayList<>();
        try {
            strings.addAll(Arrays.asList(Resources.getResourceFileAsString(FOLDER + "effects.txt").split("\n")));
            strings.addAll(Arrays.asList(Resources.getResourceFileAsString(FOLDER + "events.txt").split("\n")));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return strings;
    }

    private static String generateSyntaxPattern(String resource) {
        try {
            String text = Resources.getResourceFileAsString(FOLDER + resource + ".txt");
            if (text != null) {
                text = text.strip();
                ArrayList<String> parsedPieces = new ArrayList<>();
                for (String line : text.split("\n")) {
                    String[] pieces = line.split(" ");
                    for (String piece : pieces) {
                        piece = piece.trim();
                        if (!parsedPieces.contains(piece) && !addedSyntaxPatterns.contains(piece)) {
                            parsedPieces.add(piece);
                            addedSyntaxPatterns.add(piece);
                        }
                    }
                }
                String pattern = String.join("|", parsedPieces);
                if (pattern.startsWith("|")) {
                    pattern = pattern.substring(1);
                }
                pattern = pattern.replaceAll("\\|\\|", "|");
                return "\\b(" + pattern + ")\\b";
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static final Pattern PATTERN = Pattern.compile("" +
            "(?<COMMENT>" + COMMENT_PATTERN + ")" +
            "|(?<STRING>" + STRING_PATTERN + ")" +
            "|(?<VARIABLE>" + VARIABLE_PATTERN + ")" +
            "|(?<INPUT>" + INPUT_PATTERN + ")" +
            "|(?<NUMBER>" + NUMBER_PATTERN + ")" +
            "|(?<SPECIAL>" + SPECIAL_PATTERN + ")" +
            "|(?<EXPRESSION>" + EXPRESSION_PATTERN + ")" +
            "|(?<EFFECT>" + EFFECT_PATTERN + ")" +
            "|(?<EVENT>" + EVENT_PATTERN + ")" +
            "|(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
            "");

    public SkriptLanguage() {
        super(Styles.forName("sfs"), "Skript");
        usingAutoComplete = true;
        commentChars = "#";
    }

    @Override
    public Pattern generatePattern() {
        return PATTERN;
    }

    @Override
    public String styleClass(Matcher matcher) {
        return
            matcher.group("COMMENT") != null ? "comment" :
                matcher.group("STRING") != null ? "string" :
                    matcher.group("VARIABLE") != null ? "variable" :
                        matcher.group("INPUT") != null ? "input" :
                            matcher.group("NUMBER") != null ? "number" :
                                matcher.group("SPECIAL") != null ? "special" :
                                    matcher.group("KEYWORD") != null ? "keyword" :
                                        matcher.group("EXPRESSION") != null ? "expression" :
                                            matcher.group("EFFECT") != null ? "effect" :
                                                matcher.group("EVENT") != null ? "event" :
                                                    null;
    }

    @Override
    public boolean showingTooltip(EditorTooltip tooltip, int pos) {
        return LanguageUtils.loadSameTextViewTooltip(tooltip, pos, segment -> segment.getStyle().contains("variable"));
    }

    @Override
    public List<IdeSpecialParser.PossiblePiecePackage> getPossiblePieces(String line, IntegratedTextEditor editor) {
        line = line.trim();
        if (line.length() < 2) {
            return Collections.emptyList();
        }
        ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = new ArrayList<>();
        for (String val : AUTOCOMPLETE_ENTRIES) {
            if (val != null) {
                val = val.trim();
                if (val.startsWith(line)) {
                    possiblePiecePackages.add(new IdeSpecialParser.PossiblePiecePackage(line, val.substring(line.length()), val, true));
                }
            }
        }
        return possiblePiecePackages;
    }

}
