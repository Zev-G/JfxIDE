package tmw.me.com.ide.codeEditor.languages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaLanguage extends LanguageSupport {

    private static final String[] KEYWORDS = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double", "else", "enum",
                                               "extends", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
                                               "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
                                               "try", "void", "volatile", "while", "String", "Math"};

    private static final String KEYWORDS_PATTERN = "\\b" + String.join("|", KEYWORDS) + "\\b";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = ";";

    private static final Pattern PATTERN = Pattern.compile("" +
            "(?<COMMENT>" + COMMENT_PATTERN + ")" +
            "|(?<STRING>" + STRING_PATTERN + ")" +
            "|(?<NUMBER>" + NUMBER_PATTERN + ")" +
            "|(?<KEYWORD>" + KEYWORDS_PATTERN + ")" +
            "|(?<PAREN>" + PAREN_PATTERN + ")" +
            "|(?<BRACKET>" + BRACKET_PATTERN + ")" +
            "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" +
            "|(?<BRACE>" + BRACE_PATTERN + ")" +
            "");

    public JavaLanguage() {
        styleSheet = JavaLanguage.class.getResource("styles/java.css").toExternalForm();
        languageName = "Java";
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
            matcher.group("PAREN") != null ? "paren" :
            matcher.group("BRACKET") != null ? "bracket" :
            matcher.group("SEMICOLON") != null ? "semicolon" :
            matcher.group("BRACE") != null ? "brace" :
            null;
    }

    @Override
    public boolean checkFileEnding(String ending) {
        return ending.endsWith("java") || ending.endsWith("class");
    }

}
