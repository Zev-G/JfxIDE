package tmw.me.com.ide.codeEditor.highlighting;

import tmw.me.com.ide.codeEditor.texteditor.HighlightableTextEditor;

import java.util.Collection;

public abstract class StyleSpansFactory<T> {

    protected HighlightableTextEditor editor;

    public StyleSpansFactory(Highlighter highlighter) {
        editor = highlighter.getEditor();
    }

    public StyleSpansFactory(HighlightableTextEditor editor) {
        this.editor = editor;
    }

    public Collection<SortableStyleSpan<T>> genSpans() {
        return genSpans(editor.getText());
    }

    public abstract Collection<SortableStyleSpan<T>> genSpans(String text);

    public HighlightableTextEditor getEditor() {
        return editor;
    }
}
