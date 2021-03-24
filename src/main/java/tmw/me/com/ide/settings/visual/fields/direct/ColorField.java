package tmw.me.com.ide.settings.visual.fields.direct;

import javafx.beans.value.WritableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;
import tmw.me.com.ide.settings.visual.fields.AnnotatedField;
import tmw.me.com.ide.tools.colorpicker.MyCustomColorPicker;

public class ColorField extends AnnotatedField<Color> {

    private final MyCustomColorPicker colorPicker = new MyCustomColorPicker(false);
    private final Popup popup = new Popup();
    private final Pane currentColor = new Pane();

    public ColorField(Color val, DisplayedJSON annotation, WritableValue<Color> writeTo) {
        super(val, annotation, writeTo);
        popup.getContent().add(colorPicker);
        popup.setAutoHide(true);
        currentColor.setMinSize(annotation.fontSize() * 3, annotation.fontSize());
        currentColor.setBackground(new Background(new BackgroundFill(val, CornerRadii.EMPTY, Insets.EMPTY)));
        currentColor.setOnMousePressed(event -> popup.show(getScene().getWindow(), event.getScreenX(), event.getScreenY()));
        colorPicker.customColorProperty().addListener((observable, oldValue, newValue) -> {
            currentColor.setBackground(new Background(new BackgroundFill(val, CornerRadii.EMPTY, Insets.EMPTY)));
            writeTo.setValue(newValue);
        });
        getChildren().add(currentColor);
    }

    @Override
    public Object getValue() {
        return colorPicker.getCurrentColor();
    }

}
