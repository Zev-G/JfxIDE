package tmw.me.com.ide.codeEditor.texteditor;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexRange;
import javafx.scene.input.ScrollEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyledSegment;
import tmw.me.com.Resources;
import tmw.me.com.ide.settings.IdeSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TextEditorBase extends CodeArea {

    public static final String INDENT = "  ";
    public static final String STYLE_SHEET = Resources.getExternalForm(Resources.EDITOR_STYLES + "ite.css");

    protected final IntegerProperty fontSize = new SimpleIntegerProperty();
    protected final ArrayList<TextEditorBase> linkedTextEditors = new ArrayList<>();

    public TextEditorBase() {
        fontSize.addListener(this::fontSizeChanged);
        addEventFilter(ScrollEvent.ANY, e -> {
            if (e.isControlDown()) {
                e.consume();
                double amount = (fontSize.get() * 0.1) + 1;
                if (e.getDeltaY() != 0) {
                    if (e.getDeltaY() < 0) {
                        amount *= -1;
                    }
                    if (fontSize.get() + amount >= 6)
                        fontSize.set(fontSize.get() + (int) amount);
                }
            }
        });

        this.multiPlainChanges().subscribe(plainTextChanges -> {
            if (alternateIsFocused()) {
                for (PlainTextChange plainTextChange : plainTextChanges) {
                    for (TextEditorBase link1 : linkedTextEditors) {
                        if (link1.getScene() != null && link1 != this && !link1.getText().equals(this.getText())) {
                            if (plainTextChange.getRemoved().length() > 0) {
                                link1.deleteText(plainTextChange.getPosition(), plainTextChange.getRemovalEnd());
                            }
                            if (plainTextChange.getInserted().length() > 0) {
                                link1.insertText(plainTextChange.getPosition(), plainTextChange.getInserted());
                            }
                            if (!link1.getText().equals(this.getText())) {
                                link1.replaceText(this.getText());
                            }
                        }
                    }
                }
            }
        });

        Platform.runLater(() -> setFontSize(IdeSettings.getEditorJSON().fontSize));
    }

    protected void fontSizeChanged(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
        String newStyle = "-fx-font-size: " + newVal.intValue() + ";";
        setStyle(newStyle);
    }

    protected abstract boolean alternateIsFocused();

    public abstract void highlight();

    public StyledSegment<String, Collection<String>> getSegmentAtPos(int pos) {
        int loopPos = 0;
        for (Paragraph<Collection<String>, String, Collection<String>> par : getParagraphs()) {
            int newPos = loopPos + par.getText().length() + 1;
            if (newPos >= pos) {
                for (StyledSegment<String, Collection<String>> segment : par.getStyledSegments()) {
                    newPos = loopPos + segment.getSegment().length();
                    if (newPos >= pos) {
                        return segment;
                    } else {
                        loopPos = newPos;
                    }
                }
            } else {
                loopPos = newPos;
            }
        }
        return null;
    }

    public void setFontSize(int i) {
        fontSize.set(i);
    }

    public int getFontSize() {
        return fontSize.get();
    }

    public IntegerProperty fontSizeProperty() {
        return fontSize;
    }

    public String getTabbedText() {
        String text = super.getText();
        text = text.replaceAll(IdeSettings.tabSize(), "\t");
        return text;
    }

    public static void linkITEs(TextEditorBase... links) {

        if (links.length > 1) {
            List<TextEditorBase> linksList = Arrays.asList(links);
            for (TextEditorBase link : links) {
                ArrayList<TextEditorBase> listWithoutSelf = new ArrayList<>(linksList);
                listWithoutSelf.remove(link);
                link.linkToITEs(listWithoutSelf.toArray(new TextEditorBase[0]));
            }
        }
    }

    public ArrayList<TextEditorBase> getLinkedTextEditors() {
        return linkedTextEditors;
    }

    public void linkToITEs(TextEditorBase... links) {
        this.linkedTextEditors.addAll(Arrays.asList(links));
        recursivelyChangeLinkedITEs();
    }

    protected void recursivelyChangeLinkedITEs() {
        for (TextEditorBase link : linkedTextEditors) {
            if (!link.getLinkedTextEditors().equals(linkedTextEditors)) {
                link.getLinkedTextEditors().clear();
                link.getLinkedTextEditors().addAll(linkedTextEditors);
                link.recursivelyChangeLinkedITEs();
            }
        }
    }

    // Utility oriented methods
    public String properlyIndentString(String text) {
        String[] lines = text.split("\n");
        String indentCharacter = INDENT;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                indentCharacter = "\t";
                break;
            } else if (line.startsWith(INDENT)) {
                break;
            }
        }
        int minimumIndent = 999;
        for (String line : lines) {
            int lineIndent = 0;
            while (line.startsWith(indentCharacter)) {
                lineIndent++;
                line = line.substring(indentCharacter.length());
            }
            if (lineIndent < minimumIndent) {
                minimumIndent = lineIndent;
            }
        }
        StringBuilder newVersion = new StringBuilder();
        for (String line : lines) {
            newVersion.append(line.substring(minimumIndent)).append("\n");
        }
        String newString = newVersion.toString();
        return newString.substring(0, newString.length() - 1);
    }

    public String indentBackwards(String text) {
        if (text.length() <= 1) {
            return text;
        }
        String[] lines = text.split("\n");
        String indentCharacter = INDENT;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                indentCharacter = "\t";
                break;
            } else if (line.startsWith(INDENT)) {
                break;
            }
        }
        StringBuilder newString = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith(indentCharacter)) {
                line = line.substring(indentCharacter.length());
            }
            newString.append("\n").append(line);
        }
        return newString.substring(1);
    }

    public String indentForwards(String text) {
        String[] lines = text.split("\n");
        String indentCharacter = INDENT;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                indentCharacter = "\t";
                break;
            } else if (line.startsWith(INDENT)) {
                break;
            }
        }
        if (text.length() <= 1) {
            return indentCharacter + text;
        }
        StringBuilder newString = new StringBuilder();
        for (String line : lines) {
            newString.append("\n").append(indentCharacter).append(line);
        }
        return newString.substring(1);
    }

    public int[] expandFromPoint(int caretPosition, Character... stopAt) {
        int right = expandInDirection(caretPosition, 1, stopAt);
        right = right < getText().length() ? right + 1 : right;
        return new int[]{expandInDirection(caretPosition, -1, stopAt), right};
    }

    public int expandInDirection(int start, int dir, Character... stopAt) {
        String text = this.getText();
        List<Character> characters = Arrays.asList(stopAt);
        while (start > 0 && start < text.length() && !characters.contains(text.charAt(start))) start += dir;
        return start;
    }

    public ArrayList<IndexRange> allInstancesOfStringInString(String lookIn, String lookFor) {
        ArrayList<IndexRange> areas = new ArrayList<>();
        Matcher matcher = Pattern.compile("(?=" + Pattern.quote(lookFor) + ")").matcher(lookIn);
        while (matcher.find()) {
            areas.add(new IndexRange(matcher.start(), matcher.start() + lookFor.length()));
        }
        return areas;
    }

    public int absolutePositionFromLine(int line) {
        int lineStart = 0;
        for (int i = line - 1; i >= 0; i--) {
            lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
        }
        return lineStart;
    }

    public int lineFromAbsoluteLocation(int absoluteLocation) {
        int at = 0;
        for (int i = 0; i < getParagraphs().size(); i++) {
            at += getParagraph(i).getText().length() + 1;
            if (at >= absoluteLocation)
                return i;
        }
        return -1;
    }

    public int absoluteStartOfSegment(StyledSegment<String, Collection<String>> segmentAtPos) {
        int at = 0;
        for (int i = 0; i < getParagraphs().size(); i++) {
            Paragraph<Collection<String>, String, Collection<String>> par = getParagraph(i);
            if (par.getStyledSegments().contains(segmentAtPos)) {
                int startAt = at;
                for (StyledSegment<String, Collection<String>> segment : par.getStyledSegments()) {
                    if (segment != segmentAtPos) {
                        at += segment.getSegment().length();
                    } else {
                        return at;
                    }
                }
                at = startAt;
            }
            at += par.getText().length() + 1;
        }
        return -1;
    }

    protected int stringOccurrences(String string, char checkFor) {
        int occurrences = 0;
        for (char c : string.toCharArray()) {
            if (c == checkFor) occurrences++;
        }
        return occurrences;
    }

    public List<IndexRange> connectIndexesToNeighbors(List<IndexRange> indexRanges) {
        if (indexRanges.isEmpty())
            return indexRanges;
        ArrayList<IndexRange> newIndexRanges = new ArrayList<>();
        IndexRange lastIndexRange = indexRanges.get(0);
        for (int i = 1; i < indexRanges.size(); i++) {
            IndexRange currentIndexRange = indexRanges.get(i);
            if (lastIndexRange.getEnd() == currentIndexRange.getStart())
                lastIndexRange = new IndexRange(lastIndexRange.getStart(), currentIndexRange.getEnd());
            else {
                newIndexRanges.add(lastIndexRange);
                lastIndexRange = currentIndexRange;
            }
        }
        newIndexRanges.add(lastIndexRange);
        return newIndexRanges;
    }

}
