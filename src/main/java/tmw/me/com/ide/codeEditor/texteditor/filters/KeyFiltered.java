package tmw.me.com.ide.codeEditor.texteditor.filters;

import javafx.collections.ObservableList;
import tmw.me.com.ide.codeEditor.texteditor.TextEditorBase;

public interface KeyFiltered<T extends TextEditorBase> {

    ObservableList<KeyFilter<T>> getKeyFilters();
    default void addKeyFilter(KeyFilter<T> keyFilter) {
        getKeyFilters().add(keyFilter);
    }
    default void removeKeyFilter(KeyFilter<T> keyFilter) {
        getKeyFilters().remove(keyFilter);
    }

}
