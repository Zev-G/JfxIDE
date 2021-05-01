package tmw.me.com.ide.codeEditor.highlighting;

import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyledSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SameStyleSameTextFactory extends StyleSpansFactory<Collection<String>> {

    private final Collection<String> styles;

    private final List<String> applyStyles;

    public SameStyleSameTextFactory(Highlighter highlighter, Collection<String> styles, String... applyStyles) {
        super(highlighter);
        this.styles = styles;
        this.applyStyles = Arrays.asList(applyStyles);
    }

    @Override
    public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
        ArrayList<SortableStyleSpan<Collection<String>>> styleSpans = new ArrayList<>();
        StyledSegment<String, Collection<String>> segmentAtMouse = editor.getSegmentAtPos(editor.getCaretPosition());
        boolean pass = false;
        if (segmentAtMouse != null) {
            for (String style : segmentAtMouse.getStyle()) {
                if (styles.contains(style)) {
                    pass = true;
                    break;
                }
            }
        } else {
            return styleSpans;
        }
        if (!pass) {
            return styleSpans;
        }
        int at = 0;
        for (Paragraph<Collection<String>, String, Collection<String>> paragraph : editor.getParagraphs()) {
            for (StyledSegment<String, Collection<String>> segment : paragraph.getStyledSegments()) {
                if (segment.getSegment().trim().equals(segmentAtMouse.getSegment().trim())) {
                    for (String style : segment.getStyle()) {
                        if (styles.contains(style)) {
                            styleSpans.add(new SortableStyleSpan<>(applyStyles, at, at + segment.getSegment().trim().length()));
                        }
                    }
                }
                at += segment.getSegment().length();
            }
            at += 1;
        }
        return styleSpans;
    }
}
