package tmw.me.com.ide.codeEditor.minimap;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import tmw.me.com.Resources;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.texteditor.BehavioralLanguageEditor;
import tmw.me.com.ide.codeEditor.texteditor.HighlightableTextEditor;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;

public class Minimap extends AnchorPane {

    private final Pane viewPort = new Pane();

    private final HighlightableTextEditor minimap = new BehavioralLanguageEditor() {

        @Override
        public void onHighlight() {

        }

        @Override
        protected boolean alternateIsFocused() {
            return false;
        }

        @Override
        public void setFontSize(int i) {

        }
    };
    private final VirtualizedScrollPane<HighlightableTextEditor> minimapContainer = new VirtualizedScrollPane<>(minimap, ScrollPane.ScrollBarPolicy.NEVER, ScrollPane.ScrollBarPolicy.NEVER);

    private boolean linked = false;

    private double viewPortDragStartLayoutY = Integer.MIN_VALUE;;
    private double viewPortDragStartSceneY;

    public Minimap() {
        getChildren().addAll(minimapContainer, viewPort);

        minimapContainer.setMouseTransparent(true);

        AnchorPane.setTopAnchor(minimapContainer, 0D);
        AnchorPane.setBottomAnchor(minimapContainer, 0D);
        AnchorPane.setLeftAnchor(minimapContainer, 0D);
        AnchorPane.setRightAnchor(minimapContainer, 0D);

        AnchorPane.setLeftAnchor(viewPort, 0D);
        AnchorPane.setRightAnchor(viewPort, 0D);

        getStylesheets().addAll(Ide.STYLE_SHEET);
        this.getStylesheets().addAll(IntegratedTextEditor.STYLE_SHEET, Resources.getExternalForm(Resources.EDITOR_STYLES + "minimap"));

        viewPort.getStyleClass().add("viewport");
    }

    public void link(IntegratedTextEditor editor) {
        if (linked)
            return;
        linked = true;

        minimap.setStyle("-fx-font-size: 8 !important;");
        HighlightableTextEditor.controlFrom(editor, minimap);

        syncViewportHeight(editor);
        editor.heightProperty().addListener((observableValue, aDouble, t1) -> Platform.runLater(() -> syncViewportHeight(editor)));

        viewPort.setOnMousePressed(event -> {
            viewPortDragStartLayoutY = viewPort.getLayoutY();
            viewPortDragStartSceneY = event.getSceneY();
        });
        viewPort.setOnMouseDragged(event -> {
            double min = 0;
            double max = Math.min(getHeight(), minimap.getTotalHeightEstimate()) - viewPort.getHeight();
            double newLayoutY = viewPortDragStartLayoutY + (event.getSceneY() - viewPortDragStartSceneY);
            if (newLayoutY > max) {
                newLayoutY = max;
            }
            if (newLayoutY < min) {
                newLayoutY = min;
            }
            viewPort.setLayoutY(newLayoutY);

            double relativeEditorPixel = (newLayoutY / max) * editor.getTotalHeightEstimate();
            System.out.println("ST: " + (newLayoutY / max) + " * " + editor.getTotalHeightEstimate() + " = " + relativeEditorPixel);
            editor.scrollYToPixel(relativeEditorPixel);
        });
        viewPort.setOnMouseReleased(event -> {
            viewPortDragStartLayoutY = Integer.MIN_VALUE;
        });
        editor.estimatedScrollYProperty().addListener((observableValue, aDouble, t1) -> {
            Platform.runLater(() -> {
                try {
                    if (viewPortDragStartLayoutY == Integer.MIN_VALUE) {
                        double layoutY = (t1 / editor.getTotalHeightEstimate()) * (Math.min(minimap.getTotalHeightEstimate(), getHeight()) - viewPort.getHeight());
                        System.out.println("LY: " + layoutY);
                        viewPort.setLayoutY(layoutY);
                    }
                } catch (NullPointerException ignored) {
                    // Sadly idk why this is happening D:, doesn't seem to be causing any issues at least.
                }
            });
        });
    }

    private void syncViewportHeight(IntegratedTextEditor editor) {
        double relativeHeight = (editor.getViewportHeight() / editor.getTotalHeightEstimate()) * minimap.getTotalHeightEstimate();
        viewPort.setMinHeight(relativeHeight);
    }

}
