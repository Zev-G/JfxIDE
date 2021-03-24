package tmw.me.com.ide.codeEditor.visualcomponents;

import javafx.scene.input.KeyEvent;
import tmw.me.com.ide.codeEditor.Behavior;
import tmw.me.com.ide.codeEditor.texteditor.BehavioralEditor;

public interface VisualComponent<T> extends Behavior {

    default T get() {
        return (T) this;
    }

    void apply(BehavioralEditor ite);

    void receiveKeyEvent(KeyEvent event, BehavioralEditor textEditor);

    @Override
    default void remove(BehavioralEditor integratedTextEditor) {

    }
}
