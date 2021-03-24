package tmw.me.com.ide.codeEditor.texteditor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyEvent;
import tmw.me.com.ide.codeEditor.texteditor.filters.*;

public abstract class FilteredEditor extends HighlightableTextEditor implements TextFiltered<FilteredEditor>, KeyFiltered<FilteredEditor> {

    private final ObservableList<TextFilter<FilteredEditor>> textFilters = FXCollections.observableArrayList();
    private final ObservableList<KeyFilter<FilteredEditor>> keyFilters = FXCollections.observableArrayList();

    public FilteredEditor() {
        super();

        setOnKeyPressed(this::keyPressed);
        textProperty().addListener((observable, oldValue, newValue) -> {
            for (TextFilter<FilteredEditor> textFilter : textFilters) {
                TextReplacement result = textFilter.filterText(newValue, this);
                if (!result.getReplacement().equals(newValue)) {
                    replaceText(result.getStart(), result.getEnd(), result.getReplacement());
                    newValue = getText();
                }
            }
        });
    }

    public void keyPressed(KeyEvent event) {
        for (KeyFilter<FilteredEditor> keyFilter : keyFilters) {
            if (!keyFilter.validateInput(event, this)) {
                event.consume();
                return;
            }
        }
        for (KeyFilter<FilteredEditor> keyFilter : keyFilters) {
            keyFilter.receiveAcceptedInput(event, this);
            if (event.isConsumed()) {
                return;
            }
        }
    }

    @Override
    public ObservableList<KeyFilter<FilteredEditor>> getKeyFilters() {
        return keyFilters;
    }

    @Override
    public ObservableList<TextFilter<FilteredEditor>> getTextFilters() {
        return textFilters;
    }

}
