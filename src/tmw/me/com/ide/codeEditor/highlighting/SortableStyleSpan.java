package tmw.me.com.ide.codeEditor.highlighting;

import org.fxmisc.richtext.model.StyleSpan;

import java.util.Objects;

public class SortableStyleSpan<T> {

    private final T style;
    private final int start;
    private final int end;

    private StyleSpan<T> styleSpan;

    public SortableStyleSpan(T style, int start, int end) {
        this.style = style;
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public T getStyle() {
        return style;
    }

    public StyleSpan<T> toStyleSpan() {
        if (styleSpan == null) {
            styleSpan = new StyleSpan<>(style, length());
        }
        return styleSpan;
    }

    public int length() {
        return end - start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SortableStyleSpan<?> that = (SortableStyleSpan<?>) o;
        return start == that.start &&
                end == that.end &&
                Objects.equals(style, that.style);
    }

    @Override
    public int hashCode() {
        return Objects.hash(style, start, end);
    }

    @Override
    public String toString() {
        return "SortableStyleSpan{" +
                "style=" + style +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
