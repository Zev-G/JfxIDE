package tmw.me.com.ide.codeEditor.languages;

import tmw.me.com.ide.codeEditor.Behavior;
import tmw.me.com.ide.codeEditor.highlighting.FactoriesCombiner;
import tmw.me.com.ide.codeEditor.highlighting.RegexStyleSpansFactory;
import tmw.me.com.ide.codeEditor.highlighting.StyleSpansFactory;
import tmw.me.com.ide.codeEditor.texteditor.BehavioralLanguageEditor;
import tmw.me.com.ide.codeEditor.texteditor.HighlightableTextEditor;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexBasedLangSupport extends LanguageSupport {

    private RegexStyleSpansFactory regexStyleSpansFactory;

    public RegexBasedLangSupport(String styleSheet, String languageName) {
        super(styleSheet, languageName);
    }

    public RegexBasedLangSupport(String styleSheet) {
        super(styleSheet);
    }

    @Override
    public Behavior[] addBehaviour(BehavioralLanguageEditor editor) {
        regexStyleSpansFactory = new RegexStyleSpansFactory(editor, this::generatePattern, this::styleClass);
        return super.addBehaviour(editor);
    }

    /**
     * @return A pattern which is used for sectioning the highlighting, if you don't want your languageSupport to have highlighting you can always
     * make this return null.
     */
    public abstract Pattern generatePattern();

    public final StyleSpansFactory<Collection<String>> getCustomStyleSpansFactory(HighlightableTextEditor editor) {
        return new FactoriesCombiner(editor.getHighlighter(), regexStyleSpansFactory, getSubCustomStyleSpansFactory(editor));
    }

    public StyleSpansFactory<Collection<String>> getSubCustomStyleSpansFactory(HighlightableTextEditor editor) {
        return customStyleSpansFactory;
    }

    /**
     * @param matcher The matcher which the style class should be determined from, calling {@link Matcher#find()} will break highlighting.
     * @return A String which will be used for the pattern, currently this only supports a single String so you cannot return multiple style classes.
     * The returned style class should have a matching style in this Language's css file (defined in the constructor).
     */
    public abstract String styleClass(Matcher matcher);

}
