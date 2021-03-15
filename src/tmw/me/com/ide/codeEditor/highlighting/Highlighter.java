package tmw.me.com.ide.codeEditor.highlighting;

import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import tmw.me.com.ide.codeEditor.texteditor.HighlightableTextEditor;

import java.util.*;

public class Highlighter {

    private final HighlightableTextEditor editor;
    private final ArrayList<StyleSpansFactory<Collection<String>>> factories = new ArrayList<>();

    @SafeVarargs
    public Highlighter(HighlightableTextEditor editor, StyleSpansFactory<Collection<String>>... factories) {
        this.editor = editor;
        this.factories.addAll(Arrays.asList(factories));
    }

    public StyleSpans<Collection<String>> createStyleSpans() {
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();

        ArrayList<SortableStyleSpan<Collection<String>>> sortedByStartSpans = new ArrayList<>();
        ArrayList<SortableStyleSpan<Collection<String>>> sortedByEndSpans = new ArrayList<>();

        String text = editor.getText();
        ArrayList<StyleSpansFactory<Collection<String>>> tempFactories = new ArrayList<>(factories);
        StyleSpansFactory<Collection<String>> languageFactory = editor.getLanguage().getCustomStyleSpansFactory(editor);
        if (languageFactory != null) {
            tempFactories.add(languageFactory);
        }
        for (StyleSpansFactory<Collection<String>> factory : tempFactories) {
            Collection<SortableStyleSpan<Collection<String>>> factoryResult = factory.genSpans(text);
            sortedByStartSpans.addAll(factoryResult);
            sortedByEndSpans.addAll(factoryResult);
        }
        sortedByStartSpans.sort(Comparator.comparingInt(SortableStyleSpan::getStart));
        sortedByEndSpans.sort(Comparator.comparingInt(SortableStyleSpan::getEnd));

        ArrayList<SortableStyleSpan<Collection<String>>> withinSpans = new ArrayList<>();

        int current = 0;
        while (!sortedByEndSpans.isEmpty()) {
            SortableStyleSpan<Collection<String>> firstStart = sortedByStartSpans.isEmpty() ? null : sortedByStartSpans.get(0);
            SortableStyleSpan<Collection<String>> firstEnd = sortedByEndSpans.get(0);

            SortableStyleSpan<Collection<String>> first;
            int firstVal;
            if (firstStart != null && firstStart.getStart() <= firstEnd.getEnd()) {
                first = firstStart;
                firstVal = firstStart.getStart();
            } else {
                first = firstEnd;
                firstVal = firstEnd.getEnd();
            }
            boolean firstIsStart = first == firstStart;

            if (current < firstVal) {
                builder.add(new StyleSpan<>(stringsInSortableSpans(withinSpans), firstVal - current));
                current = firstVal;
            }

            if (firstIsStart) {
                sortedByStartSpans.remove(first);
                withinSpans.add(first);
            } else {
                sortedByEndSpans.remove(first);
                withinSpans.remove(first);
            }
        }

        if (current < text.length() || text.length() == 0)
            builder.add(new StyleSpan<>(Collections.emptyList(), text.length() - current));


        return builder.create();
    }

    private static Collection<String> stringsInSortableSpans(Collection<SortableStyleSpan<Collection<String>>> spans) {
        if (spans.isEmpty())
            return Collections.emptyList();
        ArrayList<String> styles = new ArrayList<>();
        for (SortableStyleSpan<Collection<String>> span : spans) {
            styles.addAll(span.getStyle());
        }
        return styles;
    }

    public HighlightableTextEditor getEditor() {
        return editor;
    }

    public ArrayList<StyleSpansFactory<Collection<String>>> getFactories() {
        return factories;
    }
}
