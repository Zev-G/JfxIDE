package tmw.me.com.ide.settings.visual.fields.direct;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;
import tmw.me.com.ide.settings.visual.fields.AnnotatedField;

public class TextDisplayField extends AnnotatedField<String> {

    private final TextFlow textFlow = new TextFlow();

    public TextDisplayField(String val, DisplayedJSON annotation) {
        super(val, annotation, null);
        Font font = Font.font(Font.getDefault().getFamily(), bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize);

        Text text = new Text(val);
        text.setFont(font);
        text.getStyleClass().add("json-node");
        text.getStyleClass().addAll(annotation.additionalStyleClasses());
        textFlow.getChildren().add(text);
        getChildren().add(textFlow);
    }

    @Override
    public Object getValue() {
        return val;
    }

}
