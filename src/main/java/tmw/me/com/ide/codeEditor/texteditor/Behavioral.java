package tmw.me.com.ide.codeEditor.texteditor;

import javafx.collections.ObservableList;
import tmw.me.com.ide.codeEditor.Behavior;

public interface Behavioral {

    ObservableList<Behavior> getBehaviors();
    void addBehavior(Behavior behavior);
    void removeBehavior(Behavior behavior);

}
