package tmw.me.com.ide.codeEditor.languages;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyledSegment;
import org.reactfx.collection.LiveList;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.visualcomponents.tooltip.EditorTooltip;
import tmw.me.com.ide.tools.NodeUtils;
import tmw.me.com.ide.tools.colorpicker.ColorMapper;
import tmw.me.com.ide.tools.colorpicker.MyCustomColorPicker;
import tmw.me.com.ide.tools.concurrent.schedulers.ChangeListenerScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public final class LanguageUtils {

    public static boolean loadSameTextViewTooltip(EditorTooltip tooltip, int pos, Function<StyledSegment<String, Collection<String>>, Boolean> posIsValidFunction) {
        IntegratedTextEditor editor = tooltip.getEditor(); // Utility variable to store the editor which the tooltip is attached to.
        StyledSegment<String, Collection<String>> segmentAtPos = editor.getSegmentAtPos(pos + 1); // Gets and stores the segment which the user hovered over.
        int line = editor.lineFromAbsoluteLocation(pos); // Stores the line which the user hovered over.
        // Insures that the segment isn't null and that the segment contains the "variable" style since we only want to show lines for variables.
        if (segmentAtPos != null && posIsValidFunction.apply(segmentAtPos)) {
            VBox variableReferences = new VBox(); // This VBox is used to layout the entire style, it is used as the content for the tooltip.
            variableReferences.setSpacing(4); // Add some spacing between the lines.
            ArrayList<Node> newChildren = new ArrayList<>();
            int pars = editor.getParagraphs().size(); // Store the number of lines.
            int found = 0; // Store the number of lines containing the variable we've found. This is later used to insure that we don't show more than our max number of lines.
            int lastIndex = -1; // Store the last location we found a line containing the variable at.
            LiveList<Paragraph<Collection<String>, String, Collection<String>>> paragraphs = editor.getParagraphs(); // Store the list of paragraphs.
            int totalDigits = (int) (Math.log10(paragraphs.size()));
            // Loop through the paragraphs.
            for (int i = 0; i < pars; i++) {
                Paragraph<Collection<String>, String, Collection<String>> par = paragraphs.get(i); // The loop-paragraph.
                if (par.getText().contains(segmentAtPos.getSegment())) {
                    // Check if we have yet to find 12 lines.
                    TextFlow flow = new TextFlow(); // The TextFlow which is used to display the code on the line.
                    // Loop through the segments in the paragraph.
                    for (StyledSegment<String, Collection<String>> segment : par.getStyledSegments()) {
                        // Check if the segment is the variable-containing segment, if it is add the "tooltip-highlight" style class to the list of styles.
                        if (segment.getSegment().equals(segmentAtPos.getSegment())) {
                            ArrayList<String> styles = new ArrayList<>(segment.getStyle());
                            styles.add("tooltip-highlight");
                            segment = new StyledSegment<>(segment.getSegment(), styles);
                        }
                        flow.getChildren().add(editor.copySegment(segment)); // Add the segment to the text flow.
                    }
                    int digitsInI = (int) (Math.log10(i + 1) + 1);
                    TextExt lineNum = new TextExt(" ".repeat(totalDigits + 1 - digitsInI) + (i + 1) + ":"); // Creates the TextExt node used to display the line number.
                    lineNum.getStyleClass().addAll("apply-font", "underline-hover-label");
                    lineNum.setCursor(Cursor.HAND);
                    int finalI = i;
                    lineNum.setOnMousePressed(event -> {
                        tooltip.hide();
                        editor.selectLine(finalI);
                        editor.showParagraphAtTop(finalI);
                    });
                    Pane lineNumHolder = new BorderPane(lineNum); // Create a BorderPane to store the line number in.
                    HBox layoutBox = new HBox(lineNumHolder, flow); // The HBox which stores the line number and the line's code in it.
                    layoutBox.setSpacing(editor.getFontSize());
                    layoutBox.getStylesheets().addAll(Ide.STYLE_SHEET, IntegratedTextEditor.STYLE_SHEET, editor.getLanguage().getStyleSheet()); // Add the relevant stylesheets to the layoutBox.
                    // Highlight the line if it is the line which the tooltip was sourced from.
                    if (line == i) {
                        layoutBox.getStyleClass().add("lighter-background");
                    }
                    // Add the separator if we've skipped one or more lines between the one we're currently showing and the last one which was added.
                    if (lastIndex >= 0 && lastIndex + 1 != i) {
                        BorderPane dividerPane = new BorderPane();
                        dividerPane.getStyleClass().add("tooltip-divider");
                        newChildren.add(dividerPane);
                    }
                    newChildren.add(layoutBox); // Add the line's code to the VBox.
                    lastIndex = i; // Update the lastIndex.
                    found++; // Update the number of lines found.
                }
            }
            // Add the "...and x more" text to the VBox.
            variableReferences.getChildren().addAll(found > 12 ? newChildren.subList(0, 12) : newChildren);
            ScrollPane scroller = new ScrollPane(variableReferences);
            if (found > 12) {
                Label loadMore = new Label("...and " + (found - 12) + " more");
                loadMore.setCursor(Cursor.HAND);
                loadMore.getStyleClass().add("underline-hover-label");
                variableReferences.getChildren().add(loadMore);
                loadMore.setOnMouseReleased(event -> {
                    variableReferences.getChildren().remove(loadMore);
                    variableReferences.getChildren().addAll(newChildren.subList(12, newChildren.size()));
                    scroller.setPrefWidth(scroller.getWidth() + editor.getFontSize());
                });
            } else if (found == 1) {
                return false;
            }
            scroller.getStyleClass().add("ac-scroller");
            scroller.setFitToWidth(true);
            variableReferences.getStyleClass().add("ac-items-box");
            tooltip.setContent(scroller); // Set the content of the tooltip
            return true;
        }
        return false; // Return false if we aren't hovering over a variable.
    }

    public static boolean loadColorChangerTooltip(EditorTooltip tooltip, int pos) {
        IntegratedTextEditor editor = tooltip.getEditor();
        StyledSegment<String, Collection<String>> segmentAtPos = editor.getSegmentAtPos(pos + 1);
        Color ogColor;
        try {
            ogColor = ColorMapper.fromString(segmentAtPos.getSegment());
        } catch (Exception e) {
            return false;
        }
        ObjectProperty<Color> currentColor = new SimpleObjectProperty<>(ogColor);
        Pane colorPane = new Pane();
        colorPane.setMinSize(75, 35);
        colorPane.setBackground(new Background(new BackgroundFill(currentColor.get(), new CornerRadii(7.5), Insets.EMPTY)));
        colorPane.getStyleClass().add("small-black-border");
        AtomicReference<String> text = new AtomicReference<>(segmentAtPos.getSegment());
        int startOfSegment = editor.absoluteStartOfSegment(segmentAtPos);
        currentColor.addListener(new ChangeListenerScheduler<>(5, (observableValue, color, t1) -> {
            colorPane.setBackground(new Background(new BackgroundFill(t1, new CornerRadii(7.5), Insets.EMPTY)));
            String oldText = text.get();
            text.set(NodeUtils.colorToWeb(t1));
            editor.replace(startOfSegment, startOfSegment + oldText.length(), text.get(), Collections.singleton("color-code"));
        }));
        colorPane.setOnMouseClicked(mouseEvent -> {
            MyCustomColorPicker colorPicker = new MyCustomColorPicker();
            colorPicker.setOpacity(0);
            colorPicker.setCurrentColor(currentColor.get());
            colorPicker.customColorProperty().addListener((observableValue, color, t1) -> {
                currentColor.set(t1);
            });
            Popup colorChooser = new Popup();
            colorChooser.getContent().add(colorPicker);
            colorChooser.setAutoHide(true);
            colorChooser.show(colorPane.getScene().getWindow(), mouseEvent.getScreenX() - 10, mouseEvent.getScreenY() - 10);
            NodeUtils.transOpacity(colorPicker, 1, 150, actionEvent ->
                    colorPicker.setOnMouseExited(event -> colorChooser.hide()));
        });
        tooltip.setContent(colorPane);
        return true;
    }

}
