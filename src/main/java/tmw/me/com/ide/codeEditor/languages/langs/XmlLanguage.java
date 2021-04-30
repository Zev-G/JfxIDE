package tmw.me.com.ide.codeEditor.languages.langs;

import tmw.me.com.ide.codeEditor.languages.RegexBasedLangSupport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NOT FINISHED
 */
public class XmlLanguage extends RegexBasedLangSupport {

    public XmlLanguage() {
        super("Xml/Fxml");
    }

    @Override
    public Pattern generatePattern() {
        return null;
    }

    @Override
    public String styleClass(Matcher matcher) {
        return null;
    }

}
