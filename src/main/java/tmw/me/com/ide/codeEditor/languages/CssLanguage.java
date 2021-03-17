package tmw.me.com.ide.codeEditor.languages;

import javafx.scene.paint.Color;
import org.fxmisc.richtext.model.StyledSegment;
import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.highlighting.FactoriesCombiner;
import tmw.me.com.ide.codeEditor.highlighting.SameStyleSameTextFactory;
import tmw.me.com.ide.codeEditor.highlighting.SortableStyleSpan;
import tmw.me.com.ide.codeEditor.highlighting.StyleSpansFactory;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.visualcomponents.tooltip.EditorTooltip;
import tmw.me.com.ide.tools.colorpicker.ColorMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The file support for CSS. This contains: highlighting, autocomplete, selected-word highlighting, and tooltips. See {@link LanguageSupport} for information on the methods used.
 */
public class CssLanguage extends LanguageSupport {

    private static final String[] KEYWORDS = {"italic", "bold", "!important", "bolder", "light", "lighter", "normal", "px", "em"};

    private static final String KEYWORDS_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "/\\*.*?\\*/";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String PAREN_PATTERN = "[A-z]+?\\(((\\n|.)*?)\\)";
    private static final String CLASS_PATTERN = "\\.([A-z]|-|\\\\|\\|)+";
    private static final String VALUE_PATTERN = "([A-z]|-)+?: ";
    private static final String COLOR_CODE_PATTERN = "#([A-z]|[0-9])+";
    private static final String PSEUDO_CLASS_PATTERN = ":([A-z]|-)+";

    private static final ArrayList<Character> ALLOWED_CHARS = new ArrayList<>(Arrays.asList(
            ' ', ':', ';', '\n'
    ));

    private static final Pattern PATTERN = Pattern.compile("" +
            "(?<COMMENT>" + COMMENT_PATTERN + ")" +
            "|(?<STRING>" + STRING_PATTERN + ")" +
            "|(?<NUMBER>" + NUMBER_PATTERN + ")" +
            "|(?<KEYWORD>" + KEYWORDS_PATTERN + ")" +
            "|(?<CLASS>" + CLASS_PATTERN + ")" +
            "|(?<PSEUDOCLASS>" + PSEUDO_CLASS_PATTERN + ")" +
            "|(?<VALUE>" + VALUE_PATTERN + ")" +
            "|(?<COLORCODE>" + COLOR_CODE_PATTERN + ")" +
            "|(?<PAREN>" + PAREN_PATTERN + ")" +
            "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" +
            "|(?<BRACE>" + BRACE_PATTERN + ")" +
            "");

    public CssLanguage() {
        super(Styles.forName("css.css"), "Css");
        usingAutoComplete = true;
    }


    @Override
    public Pattern generatePattern() {
        return PATTERN;
    }

    @Override
    public String styleClass(Matcher matcher) {
        return
                matcher.group("COMMENT") != null ? "comment" :
                        matcher.group("STRING") != null ? "string" :
                                matcher.group("NUMBER") != null ? "number" :
                                        matcher.group("KEYWORD") != null ? "keyword" :
                                                matcher.group("CLASS") != null ? "class" :
                                                        matcher.group("PSEUDOCLASS") != null ? "pseudo-class" :
                                                                matcher.group("VALUE") != null ? "value" :
                                                                        matcher.group("COLORCODE") != null ? "color-code" :
                                                                                matcher.group("PAREN") != null ? "paren" :
                                                                                        matcher.group("SEMICOLON") != null ? "semicolon" :
                                                                                                matcher.group("BRACE") != null ? "brace" :
                                                                                                        null;
    }

    @Override
    public Behavior[] addBehaviour(IntegratedTextEditor integratedTextEditor) {
        customStyleSpansFactory = new FactoriesCombiner(integratedTextEditor.getHighlighter(), new StyleSpansFactory<>(integratedTextEditor) {
            @Override
            public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
                ArrayList<SortableStyleSpan<Collection<String>>> textColors = new ArrayList<>();
                HashMap<String, Color> colorMap = ColorMapper.getColorMap();
                text = text.toLowerCase();
                int textEnd = text.length();
                char[] textArray = text.toCharArray();
                for (String s : colorMap.keySet()) {
                    Matcher matcher = Pattern.compile(s.toLowerCase()).matcher(text);
                    while (matcher.find()) {
                        int start = matcher.start();
                        int end = matcher.end();
                        if ((start == 0 || end == textEnd - 1) || (ALLOWED_CHARS.contains(textArray[start - 1]) && ALLOWED_CHARS.contains(textArray[end]))) {
                            textColors.add(new SortableStyleSpan<>(Collections.singleton("color-code"), matcher.start(), matcher.end()));
                        }
                    }
                }
                return textColors;
            }
        }, new SameStyleSameTextFactory(integratedTextEditor.getHighlighter(), Arrays.asList("class", "value"), "selected-word"));
        return null;
    }

    @Override
    public ArrayList<IdeSpecialParser.PossiblePiecePackage> getPossiblePieces(String line, IntegratedTextEditor editor) {
        ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = new ArrayList<>();
        String[] words = line.split(" ");
        String lastWord = words[words.length - 1];
        ArrayList<String> highlightWords = new ArrayList<>(Arrays.asList(KEYWORDS));
        if (lastWord.length() >= 2) {
            editor.getParagraphs().forEach(par -> par.getStyledSegments().stream()
                    .filter(segment -> (segment.getStyle().contains("class") || segment.getStyle().contains("value")) && !highlightWords.contains(segment.getSegment().trim()))
                    .forEachOrdered(segment -> highlightWords.add(segment.getSegment().trim())));

            ColorMapper.getColorMap().keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(lastWord))
                    .sequential().collect(Collectors.toCollection(() -> highlightWords));
        }
        for (String keyWord : highlightWords) {
            if (keyWord.startsWith(lastWord)) {
                String notFilledIn = keyWord.substring(lastWord.length());
                String putIn = line.trim() + notFilledIn;
                IdeSpecialParser.PossiblePiecePackage piecePackage = new IdeSpecialParser.PossiblePiecePackage(lastWord, notFilledIn, putIn);
                possiblePiecePackages.add(piecePackage);
            }
        }
        return possiblePiecePackages;
    }

    @Override
    public boolean showingTooltip(EditorTooltip tooltip, int pos) {
        IntegratedTextEditor editor = tooltip.getEditor();
        StyledSegment<String, Collection<String>> segmentAtPos = editor.getSegmentAtPos(pos + 1);
        if (segmentAtPos.getStyle().contains("class") || segmentAtPos.getStyle().contains("value")) {
            return LanguageUtils.loadSameTextViewTooltip(tooltip, pos, segment -> segment.getStyle().contains("class") || segment.getStyle().contains("value"));
        } else if ((segmentAtPos.getStyle().contains("color-code") || ColorMapper.isValidColorFunction(segmentAtPos.getSegment())) && segmentAtPos.getStyle().size() == 1) {
            return LanguageUtils.loadColorChangerTooltip(tooltip, pos);
        }
        return false;
    }

}
