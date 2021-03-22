package tmw.me.com.ide.settings.visual.fields.direct;

import javafx.beans.value.WritableValue;
import javafx.scene.control.CheckBox;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;
import tmw.me.com.ide.settings.visual.fields.AnnotatedField;

public class BooleanField extends AnnotatedField<Boolean> {

    private final CheckBox booleanBox = new CheckBox();

    public BooleanField(Boolean value, DisplayedJSON annotation, WritableValue<Boolean> writeTo) {
        super(value, annotation, writeTo);
        booleanBox.setSelected(value);
        booleanBox.setMouseTransparent(!editable);
        booleanBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            writeTo.setValue(newValue);
            saved.set(false);
        });
        getChildren().add(booleanBox);
    }

    @Override
    public Object getValue() {
        return booleanBox.isSelected();
    }
}
