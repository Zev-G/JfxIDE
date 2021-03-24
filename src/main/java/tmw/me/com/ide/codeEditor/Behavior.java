package tmw.me.com.ide.codeEditor;

import tmw.me.com.ide.codeEditor.texteditor.BehavioralEditor;

public interface Behavior {

    void apply(BehavioralEditor integratedTextEditor);

    void remove(BehavioralEditor integratedTextEditor);

}
