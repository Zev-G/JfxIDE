package tmw.me.com.ide.codeEditor.languages;

import tmw.me.com.ide.codeEditor.Behavior;
import tmw.me.com.ide.codeEditor.texteditor.BehavioralLanguageEditor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NOT FINISHED
 */
public class XmlLanguage extends LanguageSupport {

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

    @Override
    public Behavior[] addBehaviour(BehavioralLanguageEditor integratedTextEditor) {
        return null;
    }

}
