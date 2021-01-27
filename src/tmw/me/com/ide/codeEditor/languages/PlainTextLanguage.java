package tmw.me.com.ide.codeEditor.languages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainTextLanguage extends LanguageSupport {

    private static final Pattern PATTERN = Pattern.compile("");

    public PlainTextLanguage() {
        super(PlainTextLanguage.class.getResource("styles/plain.css").toExternalForm(), "Plain Text");
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
