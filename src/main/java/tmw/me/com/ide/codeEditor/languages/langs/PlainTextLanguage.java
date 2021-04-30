package tmw.me.com.ide.codeEditor.languages.langs;

import tmw.me.com.ide.codeEditor.languages.RegexBasedLangSupport;
import tmw.me.com.ide.codeEditor.languages.Styles;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainTextLanguage extends RegexBasedLangSupport {

    private static final Pattern PATTERN = Pattern.compile("");

    public PlainTextLanguage() {
        super(Styles.forName("plain"), "Plain Text");
    }

    @Override
    public Pattern generatePattern() {
        return PATTERN;
    }

    @Override
    public String styleClass(Matcher matcher) {
        return null;
    }

}
