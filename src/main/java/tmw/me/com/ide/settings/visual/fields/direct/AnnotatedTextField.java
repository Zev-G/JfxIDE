package tmw.me.com.ide.settings.visual.fields.direct;

import javafx.beans.value.WritableValue;
import javafx.scene.Cursor;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;
import tmw.me.com.ide.settings.visual.fields.AnnotatedField;
import tmw.me.com.ide.tools.ResizableTextField;

public abstract class AnnotatedTextField<T> extends AnnotatedField<T> {

    public static AnnotatedTextField<String> getSimpleInstance(String value, DisplayedJSON annotation, WritableValue<String> writeTo) {
        return new AnnotatedTextField<>(value, annotation, writeTo) {
            @Override
            public Object getValue() {
                return getText();
            }

            @Override
            protected void textChanged(String newText) {
                writeTo.setValue(newText);
            }
        };
    }

    protected final ResizableTextField field = new ResizableTextField();

    protected AnnotatedTextField(T value, DisplayedJSON annotation, WritableValue<T> writeTo) {
        super(value, annotation, writeTo);

        Font font = Font.font(Font.getDefault().getFamily(), bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize);

        field.setText(value.toString());
        field.setEditable(editable);
        field.setFont(font);
        field.getStyleClass().addAll(additionalStyles);
        field.getStyleClass().addAll("editable-label", "json-node");
        if (editable) {
            field.getStyleClass().add("editable-json-node");
        }

        field.textProperty().addListener((observable, oldValue, newValue) -> textChanged(newValue));

        field.setCursor(editable ? Cursor.TEXT : Cursor.DEFAULT);

        getChildren().add(field);
    }

    protected void textChanged(String newText) {
        saved.set(false);
    }

    public String getText() {
        return field.getText();
    }
    public void setText(String text) {
        field.setText(text);
    }

}
