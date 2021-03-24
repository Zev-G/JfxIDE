package tmw.me.com.ide.codeEditor.texteditor;

import javafx.beans.property.ObjectProperty;
import tmw.me.com.ide.AddonBase;

public interface LanguageControlled<T extends AddonBase> {

    ObjectProperty<T> languageProperty();
    default T getLanguage() {
        return languageProperty().get();
    }
    default void setLanguage(T language) {
        languageProperty().set(language);
    }

}
