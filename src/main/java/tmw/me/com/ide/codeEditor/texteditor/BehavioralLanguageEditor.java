package tmw.me.com.ide.codeEditor.texteditor;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import tmw.me.com.ide.codeEditor.Behavior;
import tmw.me.com.ide.codeEditor.highlighting.LanguageSupportStyleSpansFactory;
import tmw.me.com.ide.codeEditor.highlighting.StyleSpansFactory;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;
import tmw.me.com.ide.codeEditor.languages.PlainTextLanguage;

import java.util.Collection;
import java.util.Collections;

public class BehavioralLanguageEditor extends BehavioralEditor implements LanguageControlled<LanguageSupport> {

    protected final ObjectProperty<LanguageSupport> languageSupport = new SimpleObjectProperty<>();

    public BehavioralLanguageEditor() {
        this(new PlainTextLanguage());
    }
    public BehavioralLanguageEditor(LanguageSupport initialLanguage) {
        languageSupport.addListener((observable, oldValue, newValue) -> {
            anyLanguageChanged(oldValue, newValue);
            if (oldValue != null) {
                languageRemoved(oldValue);
            }
            if (newValue != null) {
                languageAdded(newValue);
            }
        });

        getFactories().add(new LanguageSupportStyleSpansFactory(this));

        Platform.runLater(() -> {
            this.languageSupport.set(initialLanguage);
            highlight();
        });
    }

    @Override
    public Collection<? extends StyleSpansFactory<Collection<String>>> getExtraFactories() {
        if (getLanguage() == null) {
            return Collections.emptyList();
        }
        return Collections.singleton(getLanguage().getCustomStyleSpansFactory(this));
    }

    protected void anyLanguageChanged(LanguageSupport oldValue, LanguageSupport newValue) {
        highlight();
    }
    protected void languageAdded(LanguageSupport language) {
        Behavior[] addedBehaviors = language.addBehaviour(this);
        if (addedBehaviors != null) {
            Collections.addAll(getBehaviors(), addedBehaviors);
        }
        getStylesheets().add(language.getStyleSheet());
    }
    protected void languageRemoved(LanguageSupport language) {
        getStylesheets().remove(language.getStyleSheet());
        Behavior[] removedBehaviors = language.removeBehaviour(this);
        if (removedBehaviors != null) {
            getBehaviors().removeAll(removedBehaviors);
        }
    }

    @Override
    public ObjectProperty<LanguageSupport> languageProperty() {
        return languageSupport;
    }

}
