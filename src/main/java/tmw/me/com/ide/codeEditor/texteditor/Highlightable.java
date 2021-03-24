package tmw.me.com.ide.codeEditor.texteditor;

import tmw.me.com.ide.codeEditor.highlighting.Highlighter;

public interface Highlightable {

    void highlight();
    Highlighter getHighlighter();

}
