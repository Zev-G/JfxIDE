package tmw.me.com.ide.enclosedpopup;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.*;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import tmw.me.com.jfxhelper.CssUtils;
import tmw.me.com.jfxhelper.NodeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EnclosedPopup extends AnchorPane {

    private static final Object HIDE_MARKER = new Object();

    private static final Duration DEFAULT_FADE_DURATION = new Duration(250);
    private static final CssMetaData<EnclosedPopup, Duration> FADE_DURATION_META_DATA = CssUtils.simpleMetaData("-mfx-fade-duration", DEFAULT_FADE_DURATION, EnclosedPopup::fadeDurationProperty);
    private static final boolean DEFAULT_SHOWN = false;
    private static final CssMetaData<EnclosedPopup, Boolean> SHOWN_META_DATA = CssUtils.simpleMetaData("-mfx-shown", DEFAULT_SHOWN, EnclosedPopup::shownProperty);
    private static final double DEFAULT_BACKGROUND_OPACITY = 0.75;
    private static final CssMetaData<EnclosedPopup, Number> BACKGROUND_OPACITY_META_DATA = CssUtils.simpleMetaData("-mfx-background-opacity", DEFAULT_BACKGROUND_OPACITY, EnclosedPopup::backgroundOpacityProperty);

    private static final List<CssMetaData<EnclosedPopup, ?>> CSS_META_DATA = Arrays.asList(
            FADE_DURATION_META_DATA, SHOWN_META_DATA, BACKGROUND_OPACITY_META_DATA
    );

    private boolean shownState = false;

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content") {
        @Override
        protected void invalidated() {
            contentPane.setCenter(getContent());
        }
    };
    private final StyleableDoubleProperty backgroundOpacity = new SimpleStyleableDoubleProperty(BACKGROUND_OPACITY_META_DATA, this, "background-opacity", DEFAULT_BACKGROUND_OPACITY);
    private final StyleableObjectProperty<Duration> fadeDuration = new SimpleStyleableObjectProperty<>(FADE_DURATION_META_DATA, this, "background-opacity", DEFAULT_FADE_DURATION);
    private final StyleableBooleanProperty shown = new SimpleStyleableBooleanProperty(SHOWN_META_DATA, this, "shown", DEFAULT_SHOWN) {
        @Override
        protected void invalidated() {
            updateShown();
        }
    };

    private final Pane backgroundPane = new Pane();
    private final BorderPane contentPane = new BorderPane();

    public EnclosedPopup() {
        // Make contentPane and backgroundPane hide when pressed
        EnclosedPopup.hideOnClick(backgroundPane);
        EnclosedPopup.hideOnClick(contentPane);

        // Set visibility, background color, and opacity
        this.setOpacity(0);
        setVisible(false);
        backgroundPane.setBackground(NodeUtils.coloredBackground(Color.BLACK));

        // Add style classes
        this.getStyleClass().add("enclosed-popup");
        contentPane.getStyleClass().add("content");

        // Add and position children
        this.getChildren().addAll(backgroundPane, contentPane);
        NodeUtils.anchor(backgroundPane, contentPane);

        // Keep background value on backgroundPane
        backgroundProperty().addListener((observable, oldValue, newValue) -> { // This node shouldn't ever have its background set so this listener just redirects its background to backgroundPane.
                                                                               // The downside of this is that there's no (good) way to set the background of the backgroundPane to null.
                                                                               // If someone needed to do this they would just have to use Node#lookup.
            if (newValue != null) { // Prevents stackoverflow
                backgroundPane.setBackground(newValue);
                setBackground(null);
            }
        });

        // Hide when background is pressed
        setOnMousePressed(event -> {
            // Here we get the parent of the intersected node since we only want to hide if the background or content pane is *directly* pressed. This way if the content's node(s) are pressed we won't hide.
            // This could probably be improved but since this should work in any situation I will just leave as-is.
            if (event.getPickResult().getIntersectedNode().getProperties().containsKey(HIDE_MARKER)) {
                setShown(false);
            }
        });

        // Bind values
        backgroundPane.opacityProperty().bind(backgroundOpacity); // This is just to make the background-opacity value of this object apply to the backgroundPane.
    }

    public static void hideOnClick(Node node) {
        hideOnClick(node, true);
    }
    public static void hideOnClick(Node node, boolean setTo) {
        if (!setTo) {
            node.getProperties().remove(HIDE_MARKER);
        } else {
            node.getProperties().put(HIDE_MARKER, true);
        }
    }


    public StyleableObjectProperty<Duration> fadeDurationProperty() {
        return fadeDuration;
    }
    public void setFadeDuration(Duration duration) {
        fadeDurationProperty().set(duration);
    }
    public Duration getFadeDuration() {
        return fadeDurationProperty().get();
    }

    public StyleableBooleanProperty shownProperty() {
        return shown;
    }
    public void setShown(boolean shown) {
        shownProperty().set(shown);
    }
    public boolean getShown() {
        return shown.get();
    }

    public Node getContent() {
        return contentProperty().get();
    }
    public ObjectProperty<Node> contentProperty() {
        return content;
    }
    public void setContent(Node content) {
        contentProperty().set(content);
    }

    public double getBackgroundOpacity() {
        return backgroundOpacityProperty().get();
    }
    public StyleableDoubleProperty backgroundOpacityProperty() {
        return backgroundOpacity;
    }
    public void setBackgroundOpacity(double backgroundOpacity) {
        backgroundOpacityProperty().set(backgroundOpacity);
    }

    private void updateShown() {
        boolean shown = getShown();
        if (shown != shownState) {
            if (shown) {
                animateIn();
            } else {
                animateOut();
            }
        }
        shownState = shown;
    }
    private void animateIn() {
        setOpacity(0);
        setVisible(true);
        NodeUtils.transOpacity(this, 1, getFadeDuration(), finished -> {
            setOpacity(1);
        });
    }
    private void animateOut() {
        NodeUtils.transOpacity(this, 0, getFadeDuration(), finished -> {
            setOpacity(0);
            setVisible(false);
        });
    }

    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
    static {
        // Fetch CssMetaData from its ancestors
        final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(AnchorPane.getClassCssMetaData());
        // Add new CssMetaData
        styleables.addAll(CSS_META_DATA);
        STYLEABLES = Collections.unmodifiableList(styleables);
    }

    // Return all CssMetadata information
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

}
