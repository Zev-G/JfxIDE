package tmw.me.com.ide.codeEditor.languages;

import tmw.me.com.ide.codeEditor.IntegratedTextEditor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * NOT FINISHED
 * */
public class XmlLanguage extends LanguageSupport {



    XmlLanguage() {
//        super(XmlLanguage.class.getResource("xml.css").toExternalForm(), "Xml/Fxml");
        super("", "Xml/Fxml");
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
    public void addBehaviour(IntegratedTextEditor integratedTextEditor) {

    }
}
