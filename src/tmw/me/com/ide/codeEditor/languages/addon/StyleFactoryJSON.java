package tmw.me.com.ide.codeEditor.languages.addon;

public class StyleFactoryJSON {

    public String id;
    public String regex;
    public String style;

    @Override
    public String toString() {
        return "StyleFactoryJSON{" +
                "regex='" + regex + '\'' +
                ", style='" + style + '\'' +
                '}';
    }
}
