package tmw.me.com.ide.settings.visual.fields;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.WritableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;
import tmw.me.com.ide.settings.annotations.DisplayedJSON.EditMethod;
import tmw.me.com.ide.settings.visual.fields.direct.*;
import tmw.me.com.ide.tools.SVG;
import tmw.me.com.ide.tools.control.SVGHoverButton;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AnnotatedField<T> extends HBox implements VisualField {

    public static <T> AnnotatedField<?> fromField(T value, DisplayedJSON annotation, WritableValue<T> writeTo) {
        if (value != null && Arrays.stream(value.getClass().getAnnotations()).anyMatch(testAnnotation -> testAnnotation instanceof DisplayedJSON)) {
            return new ClassFields<>(value, annotation);
        }
        if (value instanceof List || value instanceof Object[]) {
            List<?> valAsList = value instanceof Object[] ? Arrays.asList((Object[]) value) : (List<?>) value;
            return new AnnotatedList<>(annotation.title(), valAsList, annotation);
        }
        if (annotation.editMethod().isTextMethod()) {
            if (annotation.editMethod().isFileMethod()) {
                return new FileField(Objects.toString(value, ""), annotation, (WritableValue<String>) writeTo);
            } else if (annotation.editMethod().isNumberMethod()) {
                assert value instanceof Number;
                return new NumberField((Number) value, annotation, (WritableValue<Number>) writeTo);
            } else if (!annotation.editable()) {
                return new TextDisplayField(Objects.toString(value, ""), annotation);
            }
            return AnnotatedTextField.getSimpleInstance(Objects.toString(value, ""), annotation, (WritableValue<String>) writeTo);
        } else if (annotation.editMethod() == EditMethod.BOOLEAN) {
            return new BooleanField(((boolean) value), annotation, (WritableValue<Boolean>) writeTo);
        }
        return AnnotatedTextField.getSimpleInstance(Objects.toString(value, ""), annotation, (WritableValue<String>) writeTo);
    }

    protected Label title;

    protected final String[] additionalStyles;
    protected final boolean editable;
    protected final int fontSize;
    protected final boolean useTitle;
    protected final boolean bold;
    protected final EditMethod method;

    protected final WritableValue<T> writeTo;
    protected final T val;

    protected final BooleanProperty saved = new SimpleBooleanProperty(true);

    protected AnnotatedField(T val, DisplayedJSON annotation, WritableValue<T> writeTo) {
        this.additionalStyles = annotation.additionalStyleClasses();
        this.editable = annotation.editable();
        this.fontSize = annotation.fontSize();
        this.useTitle = annotation.useTitle();
        this.method = annotation.editMethod();
        this.bold = annotation.bold();
        this.val = val;
        this.writeTo = writeTo;

        if (useTitle) {
            Font font = Font.font(Font.getDefault().getFamily(), bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize);
            this.title = new Label(annotation.title());
            this.title.getStyleClass().add("json-value-title");
            this.title.setFont(font);
            getChildren().add(this.title);
        }

        setSpacing(4);
        setAlignment(Pos.CENTER_LEFT);
    }

    public void makeRemovable(Consumer<AnnotatedField<T>> removed) {
        SVGHoverButton removeButton = new SVGHoverButton(SVG.resizePath(SVG.TRASH, 0.8 * (fontSize / 14D)));
        removeButton.getStyleClass().add("simple-button");
        getChildren().add(removeButton);
        removeButton.setOnAction(event -> removed.accept(this));
    }

    public DisplayedJSON.EditMethod getEditMethod() {
        return method;
    }

    public abstract Object getValue();

    public T getInitialVal() {
        return val;
    }

    public Label getTitleLabel() {
        return title;
    }

    public boolean isSaved() {
        return saved.get();
    }

    public BooleanProperty savedProperty() {
        return saved;
    }

    public void saved() {
        savedProperty().set(true);
    }

}
