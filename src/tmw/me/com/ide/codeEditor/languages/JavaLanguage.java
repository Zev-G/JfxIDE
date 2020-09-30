package tmw.me.com.ide.codeEditor.languages;

import tmw.me.com.ide.codeEditor.IntegratedTextEditor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The (very minimal) file support for Java. This is just highlighting, see {@link LanguageSupport} for information on the methods used.
 */
public class JavaLanguage extends LanguageSupport {

    private static final String[] KEYWORDS = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double", "else", "enum",
                                               "extends", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
                                               "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
                                               "try", "void", "volatile", "while", "String", "Math"};

    private static final String KEYWORDS_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String PAREN_PATTERN = "((|[A-z]*)\\()|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String VARIABLE_CALL_PATTERN = "\\b([a-z][A-z]+?\\.)\\b";
    private static final String CLASS_PATTERN = "\\b([A-Z][A-z]+)\\b";

    private static final Pattern PATTERN = Pattern.compile("" +
            "(?<COMMENT>" + COMMENT_PATTERN + ")" +
            "|(?<STRING>" + STRING_PATTERN + ")" +
            "|(?<NUMBER>" + NUMBER_PATTERN + ")" +
            "|(?<KEYWORD>" + KEYWORDS_PATTERN + ")" +
            "|(?<CLASS>" + CLASS_PATTERN + ")" +
            "|(?<VARCALL>" + VARIABLE_CALL_PATTERN + ")" +
            "|(?<PAREN>" + PAREN_PATTERN + ")" +
            "|(?<BRACKET>" + BRACKET_PATTERN + ")" +
            "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" +
            "|(?<BRACE>" + BRACE_PATTERN + ")" +
            "");

    public JavaLanguage() {
        super(JavaLanguage.class.getResource("styles/java.css").toExternalForm(), "Java");
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
            matcher.group("NUMBER") != null ? "number" :
            matcher.group("KEYWORD") != null ? "keyword" :
            matcher.group("CLASS") != null ? "class" :
            matcher.group("VARCALL") != null ? "var-call" :
            matcher.group("PAREN") != null ? "paren" :
            matcher.group("BRACKET") != null ? "bracket" :
            matcher.group("SEMICOLON") != null ? "semicolon" :
            matcher.group("BRACE") != null ? "brace" :
            null;
    }

    @Override
    public void addBehaviour(IntegratedTextEditor integratedTextEditor) {

    }

}
