package tmw.me.com.ide.codeEditor.highlighting;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class FactoriesCombiner extends StyleSpansFactory<Collection<String>> {

    private final ObservableList<StyleSpansFactory<Collection<String>>> factories = FXCollections.observableArrayList();

    @SafeVarargs
    public FactoriesCombiner(Highlighter highlighter, StyleSpansFactory<Collection<String>>... styleSpanFactories) {
        this(highlighter, Arrays.asList(styleSpanFactories));
    }

    public FactoriesCombiner(Highlighter highlighter, Collection<StyleSpansFactory<Collection<String>>> styleSpanFactories) {
        super(highlighter);
        factories.addAll(styleSpanFactories);
    }

    public ObservableList<StyleSpansFactory<Collection<String>>> getFactories() {
        return factories;
    }

    @Override
    public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
        ArrayList<SortableStyleSpan<Collection<String>>> styleSpans = new ArrayList<>();
        factories.stream().filter(Objects::nonNull).forEach(collectionStyleSpansFactory -> styleSpans.addAll(collectionStyleSpansFactory.genSpans(text)));
        return styleSpans;
    }

    @Override
    public String toString() {
        return "FactoriesCombiner{" +
                "factories=" + factories +
                '}';
    }
}
