package tmw.me.com.ide.codeEditor.texteditor;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.notifications.ErrorNotification;
import tmw.me.com.ide.settings.IdeSettings;
import tmw.me.com.ide.tools.SVG;
import tmw.me.com.ide.tools.control.SVGHoverButton;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

/**
 * This class is a custom implementation of the LineFactory for the {@link IntegratedTextEditor}.
 */
public class LineGraphicFactory implements IntFunction<Node> {

    private static final String SVG_PATH = SVG.resizePath(SVG.ARROW, 0.5);
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    private static final Insets DEFAULT_INSETS = new Insets(0.0D, 5.0D, 0.0D, 5.0D);
    private static final Background DEFAULT_BACKGROUND;
    private final Val<Integer> nParagraphs;
    private final Val<Integer> currentLine;
    private final IntFunction<String> format;

    private final IntegratedTextEditor integratedTextEditor;
    private final HashMap<Integer, LineGraphic> graphics = new HashMap<>();

    public static IntFunction<Node> get(IntegratedTextEditor area) {
        return get(area, (digits) -> "%1$" + digits + "s");
    }

    public static IntFunction<Node> get(IntegratedTextEditor area, IntFunction<String> format) {
        return new LineGraphicFactory(area, format);
    }

    private LineGraphicFactory(IntegratedTextEditor area, IntFunction<String> format) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.currentLine = Val.map(area.caretPositionProperty(), integer -> area.lineFromAbsoluteLocation(integer + 1));
        this.format = format;
        this.integratedTextEditor = area;
    }

    public Node apply(int idx) {
        LineGraphic lineGraphic = new LineGraphic(idx);
        graphics.put(idx, lineGraphic);
        return lineGraphic;
    }

    private String format(int x, int max) {
        int digits = (int) Math.floor(Math.log10(max)) + 1;
        return String.format(this.format.apply(digits), x);
    }

    static {
        DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.rgb(36, 49, 64), null, null));
    }

    private static int getIndentationOfLine(String line) {
        int i = 0;
        String indent = IdeSettings.tabSize();
        while (line.startsWith(indent)) {
            line = line.substring(indent.length());
            i++;
        }
        return i;
    }

    private int getNumOfLinesInIndent(int lineStart, int startingIndent) {
        String text = integratedTextEditor.getText().substring(integratedTextEditor.absolutePositionFromLine(lineStart + 1));
        int i = 0;
        for (String line : text.split("\n")) {
            if (line.trim().isEmpty()) {
                i++;
                continue;
            }
            int indentOfLine = getIndentationOfLine(line);
            if (startingIndent < indentOfLine) {
                i++;
            } else {
                break;
            }
        }
        return i;
    }

    private class LineGraphic extends HBox {

        private final int idx;
        private final Label lineNo = new Label();
        private final SVGHoverButton foldButton = new SVGHoverButton(SVG_PATH);

        private final AtomicBoolean belowAreFolded = new AtomicBoolean();
        private final String line;

        private boolean foldable;

        public LineGraphic(int idx) {
            this.idx = idx;
            Val<String> formatted = nParagraphs.map((n) -> format(idx + 1, n));

            if (currentLine.getValue() == idx) {
                pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
            }
            currentLine.addListener((observable, oldValue, newValue) -> pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, newValue == idx));

            lineNo.setBackground(DEFAULT_BACKGROUND);
            lineNo.setPadding(DEFAULT_INSETS);
            lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));
            lineNo.setAlignment(Pos.TOP_RIGHT);
            lineNo.getStyleClass().addAll("linenumber");
            if (integratedTextEditor.getErrorLines().contains(idx + 1)) {
                lineNo.getStyleClass().add("error-lineno");
            }

            foldButton.getSvgPath().setRotate(90);
            foldButton.getStyleClass().add("simple-button");
            boolean isBelowFolded = integratedTextEditor.getParagraphs().size() > idx + 1 && integratedTextEditor.getFoldStyleCheck().test(integratedTextEditor.getParagraph(idx + 1).getParagraphStyle());
            boolean isFolded = integratedTextEditor.getFoldStyleCheck().test(integratedTextEditor.getParagraph(idx).getParagraphStyle());
            belowAreFolded.set(isBelowFolded);

            if (isBelowFolded) {
                foldButton.getSvgPath().setRotate(0);
                foldButton.getSvgPath().getStyleClass().add("folded-button");
                lineNo.getStyleClass().add("folded-label");
                foldButton.setFadeOutTo(0.8);
                foldButton.setFadeInTo(1);
            } else {
                foldButton.setVisible(false);
            }

            line = integratedTextEditor.getParagraph(idx).getText();
            String lineAbove = idx > 0 ? integratedTextEditor.getParagraph(idx - 1).getText() : null;
            String lineBelow = getNextNonEmptyStringBelow(idx).orElse("");
            if (lineShouldBeFoldable(line, lineBelow)) {
                makeFoldable();
                foldable = true;
            } else {
                foldable = false;
            }
            if (lineAbove != null) {
                LineGraphic above = graphics.get(idx - 1);
                String below = getNextNonEmptyStringBelow(idx - 1).orElse(null);
                if (above != null) {
                    if (below != null && lineShouldBeFoldable(lineAbove, below)) {
                        above.belowAreFolded.set(isFolded);
                        above.makeFoldable();
                        above.foldable = true;
                    } else {
                        above.foldButton.setVisible(false);
                        above.foldButton.setOnAction(event -> System.out.println("3"));
                        above.foldable = false;
                    }
                }
            }

            getChildren().addAll(lineNo, foldButton);
            this.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(this, Priority.ALWAYS);

            if (isFolded) {
                this.setVisible(false);
            }

            this.setPadding(DEFAULT_INSETS);
            this.setBackground(DEFAULT_BACKGROUND);
            this.setFillHeight(true);

            this.getStyleClass().add("line-holder");

            this.setCursor(Cursor.DEFAULT);

            this.setOnMouseEntered(event -> graphics.values().stream()
                    .filter(LineGraphic::isFoldable)
                    .forEach(lineGraphic -> lineGraphic.foldButton.setVisible(true)));

            this.setOnMouseExited(event -> graphics.values().stream()
                    .filter(lineGraphic -> !lineGraphic.belowAreFolded.get())
                    .forEach(lineGraphic -> lineGraphic.foldButton.setVisible(false)));

        }

        public Optional<String> getNextNonEmptyStringBelow(int below) {
            below++;
            int parSize = integratedTextEditor.getParagraphs().size();
            while (below < parSize) {
                String line = integratedTextEditor.getParagraph(below).getText();
                if (line.trim().isEmpty()) {
                    below++;
                } else {
                    return Optional.of(line);
                }
            }
            return Optional.empty();
        }

        public boolean isFoldable() {
            return foldable;
        }

        private boolean lineShouldBeFoldable(String line, String lineBelow) {
            if (line.trim().isEmpty()) {
                return false;
            }
            return getIndentationOfLine(line) < getIndentationOfLine(lineBelow);
        }

        private void makeFoldable() {
            foldButton.setOnAction(event -> {
                if (!belowAreFolded.get()) {
                    try {
                        foldButton.setVisible(true);
                        integratedTextEditor.foldParagraphs(idx, idx + getNumOfLinesInIndent(idx, getIndentationOfLine(line)));
                        belowAreFolded.set(true);
                        foldButton.getSvgPath().setRotate(0);
                    } catch (ConcurrentModificationException e) {
                        Ide.findIdeInParents(integratedTextEditor).ifPresent(ide -> ide.getNotificationPane().showNotification(new Duration(3000), new ErrorNotification("Ran into error trying to fold paragraphs.", null)));
                    }
                } else {
                    integratedTextEditor.unfoldParagraphs(idx);
                    belowAreFolded.set(false);
                    foldButton.getSvgPath().setRotate(90);
                }
            });
        }

        public AtomicBoolean getBelowAreFolded() {
            return belowAreFolded;
        }

    }

}
