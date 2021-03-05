package tmw.me.com.ide.codeEditor.highlighting;

import javafx.scene.control.IndexRange;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SimpleRangeStyleSpansFactory extends StyleSpansFactory<Collection<String>> {

    private final IndexRange[] ranges;

    public SimpleRangeStyleSpansFactory(IntegratedTextEditor editor, IndexRange... ranges) {
        super(editor);
        this.ranges = ranges;
    }

    @Override
    public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
        ArrayList<SortableStyleSpan<Collection<String>>> styleSpans = new ArrayList<>();
        for (IndexRange range : ranges) {
            if (text.length() > range.getEnd()) {
                styleSpans.add(new SortableStyleSpan<>(Collections.singleton("selected-word"), range.getStart(), range.getEnd()));
            }
        }
        return styleSpans;
    }
}
