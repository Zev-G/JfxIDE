package tmw.me.com.ide.codeEditor.highlighting;

import tmw.me.com.ide.codeEditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexStyleSpansFactory extends StyleSpansFactory<Collection<String>> {

    private Pattern regex;

    public RegexStyleSpansFactory(Highlighter highlighter, Pattern regex) {
        this(highlighter.getEditor(), regex);
    }

    public RegexStyleSpansFactory(IntegratedTextEditor editor, Pattern regex) {
        super(editor);
        this.regex = regex;
    }
    public RegexStyleSpansFactory(IntegratedTextEditor editor) {
        super(editor);
    }

    @Override
    public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
        if (regex == null) {
            return Collections.emptyList();
        }
        ArrayList<SortableStyleSpan<Collection<String>>> styleSpans = new ArrayList<>();

        Matcher matcher = regex.matcher(text);
        LanguageSupport editorLanguage = editor.getLanguage();

        while (matcher.find()) {
            String styleClass = editorLanguage.styleClass(matcher);
            if (styleClass != null && styleClass.length() > 0) {
                int start = matcher.start();
                int end = matcher.end();
                styleSpans.add(new SortableStyleSpan<>(Collections.singleton(styleClass), start, end));
            }
        }

        return styleSpans;
    }

    public Pattern getRegex() {
        return regex;
    }

    public void setRegex(Pattern regex) {
        this.regex = regex;
    }
}
