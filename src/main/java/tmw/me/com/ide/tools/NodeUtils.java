package tmw.me.com.ide.tools;

import javafx.animation.*;
import javafx.beans.property.StringProperty;
import javafx.beans.value.WritableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import tmw.me.com.ide.settings.IdeSettings;

import java.util.function.Consumer;

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
        AnchorPane.setTopAnchor(node, top);
        AnchorPane.setBottomAnchor(node, bottom);
        AnchorPane.setRightAnchor(node, right);
        AnchorPane.setLeftAnchor(node, left);
    }

    public static void transOpacity(Node node, double to, double duration, EventHandler<ActionEvent> onFinished) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setToValue(to);
        fadeTransition.setOnFinished(onFinished);
        fadeTransition.play();
    }

    public static String colorToWeb(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static Color copyWithoutOpacity(Color color) {
        return Color.web(colorToWeb(color));
    }

    public static Node divider(double height, double horizontalPadding, double verticalPadding) {
        Pane visualLine = new Pane();
        visualLine.setMinHeight(height);
        visualLine.getStyleClass().addAll("black-background", "divider");
        HBox container = new HBox(visualLine);
        HBox.setHgrow(visualLine, Priority.ALWAYS);
        container.setPadding(new Insets(verticalPadding, horizontalPadding, verticalPadding, horizontalPadding));
        return container;
    }

    public static void bindParentToIDEStyle(Parent node, StringProperty theme) {
        if (!node.getStylesheets().contains(IdeSettings.getThemeFromName(theme.get())))
            node.getStylesheets().add(IdeSettings.getThemeFromName(theme.get()));
        theme.addListener((observable, oldValue, newValue) -> {
            node.getStylesheets().remove(IdeSettings.getThemeFromName(oldValue));
            node.getStylesheets().add(IdeSettings.getThemeFromName(newValue));
        });
    }

    public static <T> WritableValue<T> writableFromConsumer(Consumer<T> consumer) {
        return new WritableValue<>() {
            @Override
            public T getValue() {
                return null;
            }

            @Override
            public void setValue(T value) {
                consumer.accept(value);
            }
        };
    }

    public static ScrollPane getNotCachedScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            StackPane stackPane = (StackPane) scrollPane.lookup("ScrollPane .viewport");
            stackPane.setCache(false);
        });
        return scrollPane;
    }

    public static AnchorPane wrapNode(Node node) {
        AnchorPane anchorPane = new AnchorPane(node);
        NodeUtils.anchor(node);
        return anchorPane;
    }

    public static Timeline createLayoutTransition(Duration dur, double x, double y, Node move) {
        return new Timeline(
                new KeyFrame(dur, (EventHandler<ActionEvent>) null, new KeyValue(move.layoutXProperty(), x)),
                new KeyFrame(dur, (EventHandler<ActionEvent>) null, new KeyValue(move.layoutYProperty(), y)));
    }

}
