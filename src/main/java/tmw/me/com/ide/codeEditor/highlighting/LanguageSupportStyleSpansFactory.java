package tmw.me.com.ide.codeEditor.highlighting;

import tmw.me.com.ide.codeEditor.texteditor.HighlightableTextEditor;

import java.util.Collection;

public class LanguageSupportStyleSpansFactory extends RegexStyleSpansFactory {

    public LanguageSupportStyleSpansFactory(HighlightableTextEditor editor) {
        super(editor);
    }

    @Override
    public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
        setRegex(editor.getLanguage().generatePattern());
        return super.genSpans(text);
    }
}
