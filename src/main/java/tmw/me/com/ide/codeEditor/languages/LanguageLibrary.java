package tmw.me.com.ide.codeEditor.languages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;

public final class LanguageLibrary {

    public static ObservableList<LanguageSupplier<? extends LanguageSupport>> defaultLanguages = FXCollections.observableArrayList(
            LanguageSupplier.fromLanguages(new PlainTextLanguage(), new SfsLanguage(), new JavaLanguage(), new CssLanguage(), new MathLanguage())
    );

    public static HashMap<String, LanguageSupplier<? extends LanguageSupport>> genLanguagesMap() {
        HashMap<String, LanguageSupplier<? extends LanguageSupport>> languagesMap = new HashMap<>();
        for (LanguageSupplier<? extends LanguageSupport> langSupplier : defaultLanguages) {
            languagesMap.put(langSupplier.getName(), langSupplier);
        }
        return languagesMap;
    }
}
