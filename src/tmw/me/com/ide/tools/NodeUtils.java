package tmw.me.com.ide.tools;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class NodeUtils {

    public static void anchor(Node node) {
        anchor(node, 0, 0, 0, 0);
    }
    public static void anchor(Node node, double amount) {
        anchor(node, amount, amount, amount, amount);
    }
    public static void anchor(Node node, double horizontal, double vertical) {
        anchor(node, horizontal, horizontal, vertical, vertical);
    }
    public static void anchor(Node node, double top, double bottom, double right, double left) {
        AnchorPane.setTopAnchor(node, top); AnchorPane.setBottomAnchor(node, bottom);
        AnchorPane.setRightAnchor(node, right); AnchorPane.setLeftAnchor(node, left);
    }

    public static void transOpacity(Node node, double to, double duration, EventHandler<ActionEvent> onFinished) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setToValue(to);
        fadeTransition.setOnFinished(onFinished);
        fadeTransition.play();
    }

    public static String colorToWeb(Color color) {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

}
