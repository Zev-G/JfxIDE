package sample.ide.codeEditor.languages;

import sample.ide.IdeSpecialParser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LanguageSupport {

    private static SfsLanguage SFS;
    public static SfsLanguage getSFS() {
        if (SFS == null) SFS = new SfsLanguage();
        return SFS;
    }
    private static JavaLanguage JAVA;
    public static JavaLanguage getJava() {
        if (JAVA == null) JAVA = new JavaLanguage();
        return JAVA;
    }

    protected static final String NUMBER_PATTERN = "[0-9]+";
    protected static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";

    protected String styleSheet;
    protected String languageName;

    public abstract Pattern generatePattern();
    public String getStyleSheet() {
        return styleSheet;
    }
    public String getLanguageName() {
        return languageName;
    }

    public abstract String styleClass(Matcher matcher);
    public abstract boolean checkFileEnding(String ending);
    public ArrayList<IdeSpecialParser.PossiblePiecePackage> getPossiblePieces(String line) {
        return null;
    }

}
