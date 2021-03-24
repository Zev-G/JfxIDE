package tmw.me.com.ide.codeEditor.texteditor.filters;

import javafx.scene.input.KeyEvent;
import tmw.me.com.ide.codeEditor.texteditor.TextEditorBase;

public abstract class KeyFilter<T extends TextEditorBase> extends FilterBase<T> {

    public boolean validateInput(KeyEvent event, T editor) {
        return true;
    }
    public abstract void receiveAcceptedInput(KeyEvent event, T editor);

}
