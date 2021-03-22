package tmw.me.com.ide.codeEditor.texteditor;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;
import tmw.me.com.ide.codeEditor.languages.PlainTextLanguage;

public abstract class LanguageControlledTextEditor extends TextEditorBase {

    protected final ObjectProperty<LanguageSupport> languageSupport = new SimpleObjectProperty<>();

    public LanguageControlledTextEditor() {
        this(genDefaultLanguage());
    }

    public LanguageControlledTextEditor(LanguageSupport languageSupport) {
        this.languageSupport.addListener((observableValue, languageSupport1, t1) -> {
            languageChanged(languageSupport1, t1);
            if (languageSupport1 != null) {
                this.getStylesheets().remove(languageSupport1.getStyleSheet());
            }
            if (t1 != null) {
                this.getStylesheets().add(t1.getStyleSheet());
            }
        });

        // Language
        Platform.runLater(() -> {
            this.languageSupport.set(languageSupport);
            highlight();
        });
    }

    protected abstract void languageChanged(LanguageSupport oldLang, LanguageSupport newLang);

    public static LanguageSupport genDefaultLanguage() {
        return new PlainTextLanguage();
    }

    public LanguageSupport getLanguage() {
        return languageSupport.get();
    }

    public ObjectProperty<LanguageSupport> languageSupportProperty() {
        return languageSupport;
    }

    public void setLanguageSupport(LanguageSupport languageSupport) {
        this.languageSupport.set(languageSupport);
    }

}
