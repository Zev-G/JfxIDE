package tmw.me.com.ide.codeEditor.languages.addon;

import java.io.File;
import java.util.ArrayList;

public class HighlighterJSON {

    public ArrayList<StyleFactoryJSON> styles;

    public static HighlighterJSON fromFile(File file) {
        return JSONHelper.fromFile(file, HighlighterJSON.class);
    }

    public static HighlighterJSON makeDefault() {
        HighlighterJSON json = new HighlighterJSON();
        json.styles = new ArrayList<>();
        return json;
    }

    @Override
    public String toString() {
        return "HighlighterJSON{" +
                "styles=" + styles +
                '}';
    }
}
