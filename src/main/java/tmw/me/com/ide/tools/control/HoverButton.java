package tmw.me.com.ide.tools.control;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class HoverButton extends Button implements JFXConstructable<HoverButton> {

    private final SimpleDoubleProperty duration = new SimpleDoubleProperty(200);

    private final SimpleDoubleProperty fadeOutTo = new SimpleDoubleProperty(0.6);
    private final SimpleDoubleProperty fadeInTo = new SimpleDoubleProperty(1);

    public HoverButton() {
        init();
    }

    public HoverButton(String s) {
        super(s);
        init();
    }

    public HoverButton(String s, Node node) {
        super(s, node);
        init();
    }

    private void init() {
        this.setCursor(Cursor.HAND);
        this.setOpacity(fadeOutTo.get());

        FadeTransition fadeIn = new FadeTransition(new Duration(getDuration()), this);
        fadeIn.toValueProperty().bind(fadeInTo);
        fadeIn.setOnFinished(actionEvent -> this.setOpacity(fadeInTo.get()));
        FadeTransition fadeOut = new FadeTransition(new Duration(getDuration()), this);
        fadeOut.toValueProperty().bind(fadeOutTo);
        fadeOut.setOnFinished(actionEvent -> this.setOpacity(fadeOutTo.get()));

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEvent -> {
            fadeIn.play();
        });
        this.addEventFilter(MouseEvent.MOUSE_EXITED, mouseEvent -> {
            fadeOut.play();
        });

        duration.addListener((observableValue, number, t1) -> {
            fadeIn.setDuration(new Duration(getDuration()));
            fadeOut.setDuration(new Duration(getDuration()));
        });

        fadeOutTo.addListener((observableValue, number, t1) -> this.setOpacity(t1.doubleValue()));
    }

    public double getDuration() {
        return duration.get();
    }

    public SimpleDoubleProperty durationProperty() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration.set(duration);
    }

    public double getFadeOutTo() {
        return fadeOutTo.get();
    }

    public SimpleDoubleProperty fadeOutToProperty() {
        return fadeOutTo;
    }

    public void setFadeOutTo(double fadeOutTo) {
        this.fadeOutTo.set(fadeOutTo);
    }

    public double getFadeInTo() {
        return fadeInTo.get();
    }

    public SimpleDoubleProperty fadeInToProperty() {
        return fadeInTo;
    }

    public void setFadeInTo(double fadeInTo) {
        this.fadeInTo.set(fadeInTo);
    }

    @Override
    public HoverButton addStyleClass(String styleClass) {
        getStyleClass().add(styleClass);
        return this;
    }

    @Override
    public HoverButton addAllStyleClass(String... styleClass) {
        getStyleClass().addAll(styleClass);
        return this;
    }

    @Override
    public HoverButton removeStyleClass(String styleClass) {
        getStyleClass().remove(styleClass);
        return this;
    }

    @Override
    public HoverButton removeAllStyleClass(String... styleClass) {
        getStyleClass().removeAll(styleClass);
        return this;
    }
}
