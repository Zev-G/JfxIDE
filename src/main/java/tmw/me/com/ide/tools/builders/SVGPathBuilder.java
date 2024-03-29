package tmw.me.com.ide.tools.builders;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.SVGPath;

public final class SVGPathBuilder extends ShapeBuilder<SVGPathBuilder, SVGPath> {

    private StringProperty contentProperty;
    private boolean selectable = false;
    private boolean selected = false;

    public static SVGPathBuilder create() {
        return new SVGPathBuilder();
    }

    public SVGPathBuilder makeSelectable() {
        selectable = true;
        return this;
    }

    public SVGPathBuilder setContent(String contentProperty) {
        contentProperty().set(contentProperty);
        return this;
    }

    public StringProperty contentProperty() {
        if (contentProperty == null)
            contentProperty = new SimpleStringProperty(this, "content");
        return contentProperty;
    }

    public SVGPath build() {
        SVGPath svgPath = new SVGPath();
        if (contentProperty != null)
            svgPath.contentProperty().bind(contentProperty);
        if (fillProperty != null)
            svgPath.fillProperty().bind(fillProperty);
        if (styleClasses != null)
            svgPath.getStyleClass().addAll(styleClasses);
        if (pickOnBounds != null)
            svgPath.pickOnBoundsProperty().bind(pickOnBounds);
        if (selectable) {
            svgPath.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
                selected = !selected;
                svgPath.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), selected);
            });
        }
        return svgPath;
    }

}
