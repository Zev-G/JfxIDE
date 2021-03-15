package tmw.me.com.ide.codeEditor.highlighting;

import javafx.scene.control.IndexRange;
import tmw.me.com.ide.codeEditor.texteditor.HighlightableTextEditor;

import java.util.ArrayList;
import java.util.Collection;

public class SimpleRangeStyleSpansFactory extends StyleSpansFactory<Collection<String>> {

    private final IndexRange[] ranges;
    private final Collection<String> style;

    public SimpleRangeStyleSpansFactory(HighlightableTextEditor editor, Collection<String> style, IndexRange... ranges) {
        super(editor);
        this.style = style;
        this.ranges = ranges;
    }

    @Override
    public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
        ArrayList<SortableStyleSpan<Collection<String>>> styleSpans = new ArrayList<>();
        for (IndexRange range : ranges) {
            if (text.length() > range.getEnd()) {
                styleSpans.add(new SortableStyleSpan<>(style, range.getStart(), range.getEnd()));
            }
        }
        return styleSpans;
    }
}
