package tmw.me.com.ide.tools.tabPane;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

import java.io.File;

public interface ComponentTabContent<T extends Node & ComponentTabContent<T>> {

    T getImportantNode();

    Region getMainNode();

    void save(File file);

    T createNewCopy();

    default MenuItem[] addContext() {
        return new MenuItem[0];
    }

    default boolean canSplitHorizontally() {
        return true;
    }

}
