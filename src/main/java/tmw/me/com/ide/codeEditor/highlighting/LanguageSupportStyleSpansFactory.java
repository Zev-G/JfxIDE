package tmw.me.com.ide.codeEditor.highlighting;

import tmw.me.com.ide.codeEditor.texteditor.BehavioralLanguageEditor;

import java.util.Collection;
import java.util.Collections;

public class LanguageSupportStyleSpansFactory extends RegexStyleSpansFactory {

    private final BehavioralLanguageEditor editor;

    public LanguageSupportStyleSpansFactory(BehavioralLanguageEditor editor) {
        super(editor);
        this.editor = editor;
    }

    @Override
    public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
        if (editor.getLanguage() == null) {
            return Collections.emptyList();
        }
        setRegex(editor.getLanguage().generatePattern());
        return super.genSpans(text);
    }

}
