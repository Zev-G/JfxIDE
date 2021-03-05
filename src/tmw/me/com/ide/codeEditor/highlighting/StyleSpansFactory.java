package tmw.me.com.ide.codeEditor.highlighting;

import tmw.me.com.ide.codeEditor.IntegratedTextEditor;

import java.util.Collection;

public abstract class StyleSpansFactory<T> {

    protected IntegratedTextEditor editor;

    public StyleSpansFactory(Highlighter highlighter) {
        editor = highlighter.getEditor();
    }
    public StyleSpansFactory(IntegratedTextEditor editor) {
        this.editor = editor;
    }

    public Collection<SortableStyleSpan<T>> genSpans() {
        return genSpans(editor.getText());
    }
    public abstract Collection<SortableStyleSpan<T>> genSpans(String text);

    public IntegratedTextEditor getEditor() {
        return editor;
    }
}
