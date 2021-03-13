package tmw.me.com.ide.codeEditor.languages;

import com.jfoenix.controls.JFXColorPicker;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyledSegment;
import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.visualcomponents.tooltip.EditorTooltip;
import tmw.me.com.ide.tools.NodeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The file support for CSS. This file is just highlighting, see {@link LanguageSupport} for information on the methods used.
 */
public class CssLanguage extends LanguageSupport {

    private static final String[] KEYWORDS = { "italic", "bold", "!important", "bolder", "light", "lighter", "normal", "px", "em" };

    private static final String KEYWORDS_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "\\/\\*.*?\\*\\/";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String PAREN_PATTERN = "((|([A-z]*))\\()|\\)";
    private static final String CLASS_PATTERN = "\\.([A-z]|-|\\\\|\\|)+";
    private static final String VALUE_PATTERN = "([A-z]|-)+?: ";
    private static final String COLOR_CODE_PATTERN = "#([A-z]|[0-9])+";
    private static final String PSEUDO_CLASS_PATTERN = ":([A-z]|-)+";

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
        super(CssLanguage.class.getResource("styles/css.css").toExternalForm(), "Css");
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
        return null;
    }

    @Override
    public ArrayList<IdeSpecialParser.PossiblePiecePackage> getPossiblePieces(String line, IntegratedTextEditor editor) {
        ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = new ArrayList<>();
        String[] words = line.split(" ");
        String lastWord = words[words.length - 1];
        ArrayList<String> highlightWords = new ArrayList<>(Arrays.asList(KEYWORDS));
        for (Paragraph<Collection<String>, String, Collection<String>> par : editor.getParagraphs()) {
            for (StyledSegment<String, Collection<String>> segment : par.getStyledSegments()) {
                if (segment.getStyle().contains("class") && !highlightWords.contains(segment.getSegment())) {
                    highlightWords.add(segment.getSegment());
                }
            }
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
            return LanguageUtils.loadSimpleTooltip(tooltip, pos, segment -> segment.getStyle().contains("class") || segment.getStyle().contains("value"));
        } else if (segmentAtPos.getStyle().contains("color-code")) {
            ColorPicker colorPicker = new ColorPicker();
            Pane colorPane = new Pane(colorPicker);
            colorPane.setMinSize(75, 35);
            colorPane.setBackground(new Background(new BackgroundFill(Color.web(segmentAtPos.getSegment()), new CornerRadii(7.5), Insets.EMPTY)));
            AtomicReference<String> text = new AtomicReference<>(segmentAtPos.getSegment());
//            colorPicker.valueProperty().addListener((observableValue, color, t1) -> {
//                colorPane.setBackground(new Background(new BackgroundFill(t1, new CornerRadii(7.5), Insets.EMPTY)));
//                String oldText = text.get();
//                text.set(NodeUtils.colorToWeb(t1));
//                editor.replace(pos, pos + oldText.length(), text.get(), Collections.singleton("color-code"));
//            });
            colorPane.setOnMouseClicked(mouseEvent -> {
//                colorPicker.show();
            });
            tooltip.setContent(colorPane);
            return true;
        }
        return false;
    }

}
