package tmw.me.com.ide.tools.tabPane;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;

public interface ComponentTabContent<T extends Node & ComponentTabContent<T>> {

    T getImportantNode();
    Node getMainNode();
    String getSaveText();
    T createNewCopy();

    default MenuItem[] addContext() {
        return new MenuItem[0];
    }

}
