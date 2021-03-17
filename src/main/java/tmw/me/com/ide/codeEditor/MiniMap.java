package tmw.me.com.ide.codeEditor;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import tmw.me.com.Resources;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;
import tmw.me.com.ide.codeEditor.texteditor.HighlightableTextEditor;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.texteditor.TextEditorBase;

/**
 * Not Done
 * Not implemented
 */
public class MiniMap extends AnchorPane {

    private final HighlightableTextEditor linkedEditor = new HighlightableTextEditor() {

        @Override
        public void onHighlight() {

        }

        @Override
        protected void languageChanged(LanguageSupport oldLang, LanguageSupport newLang) {
            highlight();
        }

        @Override
        protected boolean alternateIsFocused() {
            return false;
        }

        @Override
        protected void fontSizeChanged(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
            syncViewportHeight(linkedTo);
        }

    };
    private final Pane viewPort = new Pane();

    private boolean linked = false;

    private double viewPortDragStartSceneY;
    private double viewPortDragStartLayoutY;

    private final static double VP_INITIAL_OPACITY = 0.045;
    private final static double VP_HOVER_OPACITY = 0.075;

    private IntegratedTextEditor linkedTo;

    private final VirtualizedScrollPane<HighlightableTextEditor> minimapContainer = new VirtualizedScrollPane<>(linkedEditor, ScrollPane.ScrollBarPolicy.NEVER, ScrollPane.ScrollBarPolicy.NEVER);

    public MiniMap() {
        getChildren().addAll(minimapContainer, viewPort);
        linkedEditor.maxWidthProperty().bind(minimapContainer.maxWidthProperty());
//        heightProperty().addListener((observableValue, number, t1) -> minimapContainer.setPrefHeight(t1.doubleValue()));
//        widthProperty().addListener((observableValue, number, t1) -> minimapContainer.setPrefWidth(t1.doubleValue()));
//        heightProperty().addListener((observableValue, number, t1) -> linkedEditor.setPrefHeight(t1.doubleValue()));
//        widthProperty().addListener((observableValue, number, t1) -> linkedEditor.setPrefWidth(t1.doubleValue()));

        AnchorPane.setLeftAnchor(minimapContainer, 0D);
        AnchorPane.setRightAnchor(minimapContainer, 0D);
        AnchorPane.setTopAnchor(minimapContainer, 0D);
        AnchorPane.setBottomAnchor(minimapContainer, 0D);

//        AnchorPane.setTopAnchor(linkedEditor, 0D);
//        AnchorPane.setBottomAnchor(linkedEditor, 0D);

        AnchorPane.setLeftAnchor(viewPort, 0D);
        AnchorPane.setRightAnchor(viewPort, 0D);

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
        linkedTo = ite;

        linkedEditor.setStyle("-fx-font-size: 4 !important;");
        linkedEditor.setMouseTransparent(true);
        linkedEditor.setEditable(false);
        linkedEditor.getStylesheets().add(Resources.getExternalForm(Resources.EDITOR_STYLES + "minimap"));
        minimapContainer.setMouseTransparent(true);
        ite.caretPositionProperty().addListener((observableValue, integer, t1) -> Platform.runLater(() -> {
            if (t1 == ite.getCaretPosition() && linkedEditor.getText().length() >= t1)
                linkedEditor.displaceCaret(t1);
        }));
        ite.languageSupportProperty().addListener((observableValue, support, t1) -> linkedEditor.setLanguageSupport(t1.toSupplier().get()));
        ite.selectionProperty().addListener((observableValue, indexRange, t1) -> linkedEditor.selectRange(t1.getStart(), t1.getEnd()));
        TextEditorBase.linkITEs(ite, linkedEditor);


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
            if (newLayoutY > (linkedEditor.getTotalHeightEstimate() - viewPort.getHeight())) {
                newLayoutY = linkedEditor.getTotalHeightEstimate() - viewPort.getHeight();
            }
            if (newLayoutY < 0) {
                newLayoutY = 0;
            }
            viewPort.setLayoutY(newLayoutY);
            double relativeITELocation = (newLayoutY / linkedEditor.getTotalHeightEstimate()) * ite.getTotalHeightEstimate();
            ite.scrollYToPixel(relativeITELocation);
        });

        // Link the y-position of the viewPort to the y-position of the ITE
        viewPort.setLayoutY(ite.getEstimatedScrollY());
        ite.estimatedScrollYProperty().addListener((observable, number, t1) -> {
            Platform.runLater(() -> {
                try {
                    double relativeBoxHeight = (t1 / ite.getTotalHeightEstimate()) * linkedEditor.getTotalHeightEstimate();
                    viewPort.setLayoutY(relativeBoxHeight);
                    syncViewportScroll(ite);
                } catch (NullPointerException ignored) {
                    // Sadly idk why this is happening D:, doesn't seem to be causing any issues at least.
                }
            });
        });
        // Link the height of the viewPort to the shown height of the ITE
        Platform.runLater(() -> syncViewportHeight(ite));
        ite.heightProperty().addListener((observableValue, number, t1) ->
                Platform.runLater(() -> syncViewportHeight(ite)
                ));
    }

    public VirtualizedScrollPane<HighlightableTextEditor> getMinimapContainer() {
        return minimapContainer;
    }

    private void syncViewportScroll(IntegratedTextEditor ite) {
        double dif = linkedEditor.getTotalHeightEstimate() - minimapContainer.getHeight();
        if (dif > 0) {
            double viewPortPercentage = viewPort.getLayoutY() / minimapContainer.getHeight();
            double scrollTo = dif - (dif * viewPortPercentage);
            scrollTo = getHeight() - scrollTo;
            minimapContainer.scrollYToPixel(scrollTo);
        }
    }

    private void syncViewportHeight(IntegratedTextEditor ite) {
        double relativeBoxHeight = (ite.getVirtualizedScrollPane().getHeight() / ite.getTotalHeightEstimate()) * linkedEditor.getTotalHeightEstimate();
        viewPort.setMinHeight(relativeBoxHeight);
    }



}
