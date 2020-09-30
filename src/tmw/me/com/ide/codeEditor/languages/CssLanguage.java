package tmw.me.com.ide.codeEditor.languages;

import tmw.me.com.ide.codeEditor.IntegratedTextEditor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The file support for CSS. This file is just highlighting, see {@link LanguageSupport} for information on the methods used.
 */
public class CssLanguage extends LanguageSupport {

    private static final String[] KEYWORDS = { "italic", "bold" };

    private static final String KEYWORDS_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "\\/\\*.*?\\*\\/";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String PAREN_PATTERN = "((|([A-z]*))\\()|\\)";
    private static final String CLASS_PATTERN = "\\.([A-z]|-|\\\\|\\|)+";
    private static final String COLOR_CODE_PATTERN = "#([A-z]|[0-9])+";
    private static final String PSEUDO_CLASS_PATTERN = ":([A-z]|-)+";

    private static final Pattern PATTERN = Pattern.compile("" +
            "(?<COMMENT>" + COMMENT_PATTERN + ")" +
            "|(?<STRING>" + STRING_PATTERN + ")" +
            "|(?<NUMBER>" + NUMBER_PATTERN + ")" +
            "|(?<KEYWORD>" + KEYWORDS_PATTERN + ")" +
            "|(?<CLASS>" + CLASS_PATTERN + ")" +
            "|(?<PSEUDOCLASS>" + PSEUDO_CLASS_PATTERN + ")" +
            "|(?<COLORCODE>" + COLOR_CODE_PATTERN + ")" +
            "|(?<PAREN>" + PAREN_PATTERN + ")" +
            "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" +
            "|(?<BRACE>" + BRACE_PATTERN + ")" +
            "");

    public CssLanguage() {
        super(CssLanguage.class.getResource("styles/css.css").toExternalForm(), "Css");
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
            matcher.group("PSEUDOCLASS") != null ? "pseudo-class" :
            matcher.group("COLORCODE") != null ? "color-code" :
            matcher.group("PAREN") != null ? "paren" :
            matcher.group("SEMICOLON") != null ? "semicolon" :
            matcher.group("BRACE") != null ? "brace" :
            null;
    }

    @Override
    public void addBehaviour(IntegratedTextEditor integratedTextEditor) {

    }
}
