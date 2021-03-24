package tmw.me.com.ide.codeEditor.highlighting;

import tmw.me.com.ide.codeEditor.texteditor.HighlightableTextEditor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClosingBracketFactory extends StyleSpansFactory<Collection<String>> {

    private final char start;
    private final char end;
    private final String[] style;
    private String[] bannedStyles;

    public ClosingBracketFactory(Highlighter highlighter, char start, char end, String[] style, String... bannedStyles) {
        super(highlighter);
        this.start = start;
        this.end = end;
        this.style = style;
        this.bannedStyles = bannedStyles;
    }

    public ClosingBracketFactory(HighlightableTextEditor editor, char start, char end, String[] style, String... bannedStyles) {
        super(editor);
        this.start = start;
        this.end = end;
        this.style = style;
        this.bannedStyles = bannedStyles;
    }

    public void setBannedStyles(String... bannedStyles) {
        this.bannedStyles = bannedStyles;
    }

    @Override
    public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
        Character charToLeft = getCharToLeftOfCaret();
        Character charToRight = getCharToRightOfCaret();
        if (charToLeft == null && charToRight == null) {
            return Collections.emptyList();
        }
        int bracketStartPos;
        if (charToLeft != null && charToLeft.equals(start)) {
            bracketStartPos = editor.getCaretPosition() - 1;
        } else if (charToRight != null && charToRight.equals(start)) {
            bracketStartPos = editor.getCaretPosition();
        } else {
            return Collections.emptyList();
        }
        SortableStyleSpan<Collection<String>> startStyleSpan = new SortableStyleSpan<>(Arrays.asList(style), bracketStartPos, bracketStartPos + 1);
        int textLength = editor.getText().length();
        String editorText = editor.getText();
        List<String> bannedStylesAsList = Arrays.asList(bannedStyles);

        int inside = 0;
        for (int i = bracketStartPos + 1; i < textLength; i++) {
            char charAt = editorText.charAt(i);
            if (charAt == end && editor.getStyleAtPosition(i).stream().noneMatch(bannedStylesAsList::contains)) {
                if (inside == 0)
                    return Arrays.asList(startStyleSpan, new SortableStyleSpan<>(Arrays.asList(style), i, i + 1));
                else
                    inside--;
            } else if (charAt == start && editor.getStyleAtPosition(i).stream().noneMatch(bannedStylesAsList::contains)) {
                inside++;
            }
        }
        return Collections.emptyList();
    }

    public Character getCharToLeftOfCaret() {
        int caretPos = editor.getCaretPosition() - 1;
        if (caretPos < 0 || caretPos >= editor.getText().length()) {
            return null;
        }
        return editor.getText().charAt(caretPos);
    }
    public Character getCharToRightOfCaret() {
        int caretPos = editor.getCaretPosition();
        if (caretPos < 0 || caretPos >= editor.getText().length()) {
            return null;
        }
        return editor.getText().charAt(caretPos);
    }

}
