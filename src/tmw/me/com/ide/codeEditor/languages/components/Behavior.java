package tmw.me.com.ide.codeEditor.languages.components;

import tmw.me.com.ide.codeEditor.IntegratedTextEditor;

public abstract class Behavior {

    public abstract void apply(IntegratedTextEditor integratedTextEditor);
    public abstract void remove(IntegratedTextEditor integratedTextEditor);

}
