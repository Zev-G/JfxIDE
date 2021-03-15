package tmw.me.com.ide.codeEditor.languages;

import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;

public abstract class Behavior {

    public abstract void apply(IntegratedTextEditor integratedTextEditor);

    public abstract void remove(IntegratedTextEditor integratedTextEditor);

}
