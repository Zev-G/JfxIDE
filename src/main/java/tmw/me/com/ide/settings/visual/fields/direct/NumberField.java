package tmw.me.com.ide.settings.visual.fields.direct;

import javafx.beans.value.WritableValue;
import javafx.scene.Cursor;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class NumberField extends AnnotatedTextField<Number> {

    public NumberField(Number value, DisplayedJSON annotation, WritableValue<Number> writeTo) {
        super(value, annotation, writeTo);
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (getEditMethod() == DisplayedJSON.EditMethod.INTEGER) {
                    Integer.parseInt(newValue);
                } else {
                    Double.parseDouble(newValue);
                }
            } catch (NumberFormatException exception) {
                field.setText(oldValue);
            }
        });

        this.title.setCursor(Cursor.H_RESIZE);
        AtomicReference<Double> start = new AtomicReference<>();
        AtomicLong startVal = new AtomicLong();
        this.title.setOnMousePressed(event -> {
            start.set(event.getScreenX());
            Number result = (Number) getValue();
            startVal.set(result.longValue());
        });
        this.title.setOnMouseDragged(event -> {
            double dif = event.getScreenX() - start.get();
            if (getEditMethod() == DisplayedJSON.EditMethod.INTEGER) {
                int newValue = (int) (((int) (dif / 10)) + startVal.get());
                setText(String.valueOf(newValue));
            } else {
                double newValue = dif / 10 + startVal.get();
                setText(String.valueOf(newValue));
            }
        });

    }

    @Override
    protected void textChanged(String newText) {
        super.textChanged(newText);
        if (getEditMethod() == DisplayedJSON.EditMethod.INTEGER) {
            writeTo.setValue(Integer.parseInt(newText));
        } else {
            writeTo.setValue(Double.parseDouble(newText));
        }
    }

    @Override
    public Object getValue() {
        if (getEditMethod() == DisplayedJSON.EditMethod.INTEGER) {
            return Integer.parseInt(getText());
        } else {
            return Double.parseDouble(getText());
        }
    }

}
