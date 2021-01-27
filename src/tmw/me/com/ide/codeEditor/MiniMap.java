package tmw.me.com.ide.codeEditor;

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpan;

import java.util.Collection;

/**
 * Not Done
 * Not implemented
 */
public class MiniMap extends AnchorPane {

    private final VBox box = new VBox();
    private final Pane viewPort = new Pane();

    private boolean linked = false;

    private double viewPortDragStartSceneY;
    private double viewPortDragStartLayoutY;

    private final static double VP_INITIAL_OPACITY = 0.045;
    private final static double VP_HOVER_OPACITY = 0.075;

    public MiniMap() {
        getChildren().addAll(box, viewPort);
        box.maxWidthProperty().bind(maxWidthProperty());
        AnchorPane.setLeftAnchor(box, 0D); AnchorPane.setRightAnchor(box, 0D);
        AnchorPane.setTopAnchor(box, 0D);

        AnchorPane.setLeftAnchor(viewPort, 0D); AnchorPane.setRightAnchor(viewPort, 0D);

        viewPort.getStyleClass().add("mm-viewport");
        viewPort.setOpacity(VP_INITIAL_OPACITY);
        viewPort.setOnMouseEntered(mouseEvent -> viewPort.setOpacity(VP_HOVER_OPACITY));
        viewPort.setOnMouseExited(mouseEvent -> viewPort.setOpacity(VP_INITIAL_OPACITY));
    }

    public void loadFromITE(IntegratedTextEditor ite) {
        // Make sure that the method can only be ran once
        if (linked) {
            return;
        }
        linked = true;
        // Keep Minimap text and style synced to ITE
        updateToITE(ite);
        ite.richChanges().subscribe(collectionStringCollectionRichTextChange -> Platform.runLater(() -> updateToITE(ite)));
        // Stylesheet initialization and synchronization
        this.getStylesheets().addAll(ite.getLanguage().getStyleSheet(), IntegratedTextEditor.STYLE_SHEET);
        ite.languageSupportProperty().addListener((observableValue, languageSupport, t1) -> this.getStylesheets().setAll(t1.getStyleSheet(), IntegratedTextEditor.STYLE_SHEET));
        // Scroll functionality, both visual and technical
        viewPort.setOnMousePressed(mouseEvent -> {
            viewPortDragStartLayoutY = viewPort.getLayoutY();
            viewPortDragStartSceneY = mouseEvent.getSceneY();
            viewPort.getStyleClass().add("mm-viewport-drag");
            viewPort.setOpacity(0.1);
        });
        viewPort.setOnMouseReleased(mouseEvent -> {
            viewPort.getStyleClass().remove("mm-viewport-drag");
            if (mouseEvent.getPickResult().getIntersectedNode() == viewPort) {
                viewPort.setOpacity(VP_HOVER_OPACITY);
            } else {
                viewPort.setOpacity(VP_INITIAL_OPACITY);
            }
        });
        viewPort.setOnMouseDragged(mouseEvent -> {
            double newLayoutY = viewPortDragStartLayoutY + (mouseEvent.getSceneY() - viewPortDragStartSceneY);
            if (newLayoutY > (box.getHeight() - viewPort.getHeight())) {
                newLayoutY = box.getHeight() - viewPort.getHeight();
            }
            if (newLayoutY < 0) {
                newLayoutY = 0;
            }
            viewPort.setLayoutY(newLayoutY);
            double relativeITELocation = (newLayoutY / box.getHeight()) * ite.getTotalHeightEstimate();
            ite.scrollYToPixel(relativeITELocation);
        });
        // Sync the selected paragraph of the ITE with that of the Minimap
        ite.currentParagraphProperty().addListener((observableValue, integer, t1) -> {
            if (t1 >= 0 && t1 < box.getChildren().size()) {
                box.getChildren().get(t1).getStyleClass().add("has-caret");
            }
            if (integer >= 0 && integer < box.getChildren().size()) {
                box.getChildren().get(integer).getStyleClass().remove("has-caret");
            }
        });

        // Link the y-position of the viewPort to the y-position of the ITE
        viewPort.setLayoutY(ite.getEstimatedScrollY());
        ite.estimatedScrollYProperty().addListener((observable, number, t1) -> {
            double relativeBoxHeight = (t1 / ite.getTotalHeightEstimate()) * box.getHeight();
            viewPort.setLayoutY(relativeBoxHeight);
        });
        // Link the height of the viewPort to the shown height of the ITE
        Platform.runLater(() -> {
            double relativeBoxHeight = (ite.getViewportHeight() / ite.getTotalHeightEstimate()) * box.getHeight();
            viewPort.setMinHeight(relativeBoxHeight);
        });
        ite.heightProperty().addListener((observableValue, number, t1) ->
            Platform.runLater(() -> {
                double relativeBoxHeight = (ite.getVirtualizedScrollPane().getHeight() / ite.getTotalHeightEstimate()) * box.getHeight();
                viewPort.setMinHeight(relativeBoxHeight);
            }
        ));
    }

    private void updateToITE(IntegratedTextEditor ite) {
        box.getChildren().clear();
        for (int i = 0; i < ite.getParagraphs().size(); i++) {
            HBox paragraph = new HBox();

            Paragraph<Collection<String>, String, Collection<String>> paragraphObj = ite.getParagraph(i);

            int location = 0;
            for (StyleSpan<Collection<String>> styleSpan : ite.getStyleSpans(i)) {
                String textForSpan = paragraphObj.getText().substring(location, location + styleSpan.getLength());
                TextExt textExt = new TextExt(textForSpan);
//                textExt.setFont(Font.font("Montserrat", FontWeight.findByName(textExt.getFont().getStyle()), scale));
                textExt.setMouseTransparent(true);
                textExt.getStyleClass().addAll(styleSpan.getStyle());
                textExt.getStyleClass().add("minimap-text");
                paragraph.getChildren().add(textExt);
                location += styleSpan.getLength();
            }
            if (i == ite.getCurrentParagraph()) {
                paragraph.getStyleClass().add("has-caret");
            }
            box.getChildren().add(paragraph);
        }
    }



}
