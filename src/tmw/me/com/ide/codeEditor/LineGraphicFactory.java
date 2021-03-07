package tmw.me.com.ide.codeEditor;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;
import tmw.me.com.ide.tools.builders.tooltip.ToolTipBuilder;

import java.util.function.IntFunction;

/**
 * This class is our custom implementation of the LineFactory for the {@link IntegratedTextEditor}.
 */
public class LineGraphicFactory implements IntFunction<Node> {

    private static final Insets DEFAULT_INSETS = new Insets(0.0D, 5.0D, 0.0D, 5.0D);
    private static final Background DEFAULT_BACKGROUND;
    private final Val<Integer> nParagraphs;
    private final IntFunction<String> format;

    private final IntegratedTextEditor integratedTextEditor;

    public static IntFunction<Node> get(IntegratedTextEditor area) {
        return get(area, (digits) -> "%1$" + digits + "s");
    }

    public static IntFunction<Node> get(IntegratedTextEditor area, IntFunction<String> format) {
        return new LineGraphicFactory(area, format);
    }

    private LineGraphicFactory(IntegratedTextEditor area, IntFunction<String> format) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
        this.integratedTextEditor = area;
    }

    public Node apply(int idx) {
        Val<String> formatted = this.nParagraphs.map((n) -> this.format(idx + 1, n));
        Label lineNo = new Label(String.valueOf(idx + 1));
        lineNo.setBackground(DEFAULT_BACKGROUND);
        lineNo.setPadding(DEFAULT_INSETS);
        lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));
        lineNo.setAlignment(Pos.TOP_RIGHT);
        lineNo.getStyleClass().add("lineno");
        if (integratedTextEditor.getErrorLines().contains(idx + 1)) {
            lineNo.getStyleClass().add("error-lineno");
        }

        HBox graphicBox = new HBox(lineNo);

        integratedTextEditor.getErrorLines().addListener((ListChangeListener<Integer>) change -> {
            if (change.getList().contains(idx + 1)) {
                if (!lineNo.getStyleClass().contains("error-lineno")) {
                    lineNo.getStyleClass().add("error-lineno");
                    lineNo.setTooltip(
                            ToolTipBuilder.create().setHeader("Error")
                                    .setMainText("This line has an error on it.\nIf your stuck debugging remember\nyou can always use our debugging wiki").build());
                }
            } else {
                lineNo.getStyleClass().remove("error-lineno");
                lineNo.setTooltip(null);
            }
        });

        graphicBox.setAlignment(Pos.TOP_RIGHT);
        graphicBox.setPadding(DEFAULT_INSETS);
        graphicBox.setBackground(DEFAULT_BACKGROUND);
        graphicBox.setFillHeight(true);

        graphicBox.getStyleClass().add("left-holder");

        graphicBox.setCursor(Cursor.DEFAULT);

        return graphicBox;
    }

    private String format(int x, int max) {
        int digits = (int)Math.floor(Math.log10(max)) + 1;
        return String.format(this.format.apply(digits), x);
    }

    static {
        DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.rgb(36, 49, 64), null, null));
    }

}
