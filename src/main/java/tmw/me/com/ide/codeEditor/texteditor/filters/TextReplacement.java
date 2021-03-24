package tmw.me.com.ide.codeEditor.texteditor.filters;

public class TextReplacement {

    private final int start;
    private final int end;
    private final String replacement;

    public TextReplacement(int start, int end, String replacement) {
        this.start = start;
        this.end = end;
        this.replacement = replacement;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getReplacement() {
        return replacement;
    }
}
