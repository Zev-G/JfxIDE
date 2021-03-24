package tmw.me.com.ide.codeEditor.texteditor.filters;

import tmw.me.com.ide.codeEditor.texteditor.TextEditorBase;

public abstract class TextFilter<T extends TextEditorBase> extends FilterBase<T> {

    public abstract TextReplacement filterText(String input, T editor);

}
