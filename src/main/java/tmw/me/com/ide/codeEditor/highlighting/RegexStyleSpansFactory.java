package tmw.me.com.ide.codeEditor.highlighting;

import tmw.me.com.ide.codeEditor.texteditor.BehavioralLanguageEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexStyleSpansFactory extends StyleSpansFactory<Collection<String>> {

    private Supplier<Pattern> regex;
    private final BehavioralLanguageEditor editor;
    private final Function<Matcher, String> styleClassConverter;

    public RegexStyleSpansFactory(BehavioralLanguageEditor editor, Supplier<Pattern> regex, Function<Matcher, String> styleClassConverter) {
        super(editor);
        this.editor = editor;
        this.regex = regex;
        this.styleClassConverter = styleClassConverter;
    }

    public RegexStyleSpansFactory(BehavioralLanguageEditor editor, Function<Matcher, String> styleClassConverter) {
        super(editor);
        this.editor = editor;
        this.styleClassConverter = styleClassConverter;
    }

    @Override
    public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
        if (regex == null) {
            return Collections.emptyList();
        }
        ArrayList<SortableStyleSpan<Collection<String>>> styleSpans = new ArrayList<>();

        Matcher matcher = regex.get().matcher(text);

        while (matcher.find()) {
            String styleClass = styleClassConverter.apply(matcher);
            if (styleClass != null && styleClass.length() > 0) {
                int start = matcher.start();
                int end = matcher.end();
                styleSpans.add(new SortableStyleSpan<>(Collections.singleton(styleClass), start, end));
            }
        }

        return styleSpans;
    }

    public Supplier<Pattern> getRegex() {
        return regex;
    }

}
