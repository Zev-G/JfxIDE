package tmw.me.com.ide.codeEditor.visualcomponents;

import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;

public interface VisualComponent<T extends Node> {

    default T get() {
        return (T) this;
    }

    void addToITE(IntegratedTextEditor ite);
    void receiveKeyEvent(KeyEvent event);

}
