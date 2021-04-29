package tmw.me.com.jfxhelper;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class ResizableTextField extends TextField {

    public ResizableTextField() {
        this("");
    }

    public ResizableTextField(String text) {
        textProperty().addListener((ov, prevText, currText) -> {
            // Do this in a Platform.runLater because of Textfield has no padding at first time and so on
            Platform.runLater(() -> {
                Text textNode = new Text(currText + "  ");
                textNode.setFont(getFont()); // Set the same font, so the size is the same
                double width = textNode.getLayoutBounds().getWidth() // This big is the Text in the TextField
                        + getPadding().getLeft() + getPadding().getRight() // Add the padding of the TextField
                        + 2d; // Add some spacing
                setPrefWidth(width); // Set the width
                positionCaret(getCaretPosition()); // If you remove this line, it flashes a little bit
            });
        });
        setText(text);
    }

}
