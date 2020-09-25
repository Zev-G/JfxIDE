package tmw.me.com.ide.codeEditor.languages;

import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.language.syntaxPiece.SyntaxPieceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SfsLanguage extends LanguageSupport {

    public SfsLanguage() {
        styleSheet = SfsLanguage.class.getResource("styles/sfs.css").toExternalForm();
        languageName = "Software Scripting";
    }

    private final ArrayList<String> ADDED_SYNTAX_PATTERNS = new ArrayList<>();
    //
    private static final String[] KEYWORDS = { "function", "if", "else" };
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "#[^\\n]*";
    private static final String VARIABLE_PATTERN = "\\{([^\"\\\\]|\\\\.)*?}|[^\\s]+(: | = )";
    private static final String SPECIAL_PATTERN = "\\b([A-z]+-[A-z]+)\\b";
    private static final String INPUT_PATTERN = "%([^\\s]*?)%";
    private static final String SYNTAX_START_PATTERN = "(expression|effect) (.*?)-";
    private static final String SYNTAX_END_PATTERN = "> (.*?):";

    private static final HashMap<String, String> SNIPPETS = new HashMap<>();
    static {
        SNIPPETS.put("@def", "set {stage} to new stage\nset {vbox} to new vbox\nset {button} to new button\nadd {button} to children of {vbox}\nput {vbox} into {stage}\nset text of {button} to \"Some simple text\"\nshow {stage}");
    }

    private final String EXPRESSION_PATTERN = generateSyntaxPattern(SyntaxManager.getAllExpressionFactories());
    private final String EFFECT_PATTERN = generateSyntaxPattern(SyntaxManager.EFFECT_FACTORIES);
    private final String EVENT_PATTERN = generateSyntaxPattern(SyntaxManager.EVENT_FACTORIES);

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
//            "|(?<>" +  + ")" +
            "");

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

    @Override
    public boolean checkFileEnding(String ending) {
        return true;
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
        if (pattern.startsWith("|")) {
            pattern = pattern.substring(1);
        }
        if (pattern.charAt(pattern.length() - 1) == '|') {
            pattern = pattern.substring(0, pattern.length() - 2);
        }
        pattern = pattern.replaceAll("\\|\\|", "|");
        return "\\b(" + pattern + ")\\b";
    }

    @Override
    public ArrayList<IdeSpecialParser.PossiblePiecePackage> getPossiblePieces(String line) {
        line = line.trim();
        ArrayList<SyntaxPieceFactory> allSyntaxPieceFactories = new ArrayList<>();
        allSyntaxPieceFactories.addAll(SyntaxManager.EFFECT_FACTORIES);
        allSyntaxPieceFactories.addAll(SyntaxManager.EVENT_FACTORIES);
        ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = new ArrayList<>(IdeSpecialParser.possibleSyntaxPieces(line, allSyntaxPieceFactories));
        if (line.startsWith("@")) {
            for (Map.Entry<String, String> entry : SNIPPETS.entrySet()) {
                if (entry.getKey().startsWith(line)) {
                    possiblePiecePackages.add(new IdeSpecialParser.PossiblePiecePackage(line, entry.getKey().substring(line.length()), entry.getValue(), true));
                }
            }
        }
        return possiblePiecePackages;
    }

}
