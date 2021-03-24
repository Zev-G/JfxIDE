package tmw.me.com.ide.codeEditor.texteditor.filters;

import javafx.collections.ObservableList;
import tmw.me.com.ide.codeEditor.texteditor.TextEditorBase;

public interface TextFiltered<T extends TextEditorBase> {

    ObservableList<TextFilter<T>> getTextFilters();
    default void removeTextFilter(TextFilter<T> remove) {
        getTextFilters().remove(remove);
    }
    default void addTextFilter(TextFilter<T> add) {
        getTextFilters().remove(add);
    }

}
