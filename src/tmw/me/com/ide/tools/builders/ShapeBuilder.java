package tmw.me.com.ide.tools.builders;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Paint;

public abstract class ShapeBuilder<T extends ShapeBuilder<?, ?>, R> extends StyleableBuilder<T, R> {

    protected SimpleBooleanProperty pickOnBounds;
    protected ObjectProperty<Paint> fillProperty;


    public T setPickOnBounds(boolean pickOnBounds) {
        pickOnBoundsProperty().set(pickOnBounds);
        return (T) this;
    }

    public SimpleBooleanProperty pickOnBoundsProperty() {
        if (pickOnBounds == null)
            pickOnBounds = new SimpleBooleanProperty(false);
        return pickOnBounds;
    }

    public T setFill(Paint fillProperty) {
        fillProperty().set(fillProperty);
        return (T) this;
    }

    public ObjectProperty<Paint> fillProperty() {
        if (fillProperty == null)
            fillProperty = new SimpleObjectProperty<>(this, "fill");
        return fillProperty;
    }
}
