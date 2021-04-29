package tmw.me.com.ide.notifications;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import tmw.me.com.Resources;
import tmw.me.com.jfxhelper.NodeUtils;
import tmw.me.com.ide.tools.SVG;
import tmw.me.com.ide.tools.concurrent.schedulers.ConsumerEventScheduler;
import tmw.me.com.ide.tools.control.SVGHoverButton;

public class Notification extends HBox {

    private static final String STYLE_SHEET = Resources.getExternalForm("ide/styles/components/notification.css");

    private boolean smoothRelocate = false;
    private final ConsumerEventScheduler<Point2D> relocateScheduler = new ConsumerEventScheduler<>(30, point2D -> {
        double v = point2D.getX();
        double v1 = point2D.getY();

        NodeUtils.createLayoutTransition(new Duration(125), v, v1, this).play();
    });

    protected final SVGPath icon = new SVGPath();
    protected final VBox iconBox = new VBox(icon);

    protected final Label notificationTitle = new Label();
    protected final BorderPane topPane = new BorderPane();
    private final Node divider = NodeUtils.divider(2, 0, 0);
    protected final Label mainText = new Label();
    protected final VBox mainBox = new VBox(topPane, divider, mainText);

    public Notification() {
        this(null, null);
    }

    public Notification(String title, String mainText) {
        this.notificationTitle.setText(title);
        this.mainText.setText(mainText);

        topPane.setLeft(notificationTitle);

        this.getStylesheets().add(STYLE_SHEET);

        this.getStyleClass().add("notification");
        iconBox.getStyleClass().add("icon-box");
        notificationTitle.getStyleClass().addAll("ms-title", "notification-label");
        this.mainText.getStyleClass().addAll("notification-text", "notification-label");
        icon.getStyleClass().add("notification-icon");
        mainBox.getStyleClass().add("text-box");

        HBox.setHgrow(mainBox, Priority.ALWAYS);
        getChildren().addAll(iconBox, mainBox);

        if (mainText == null || mainText.trim().isEmpty()) {
            mainBox.getChildren().removeAll(divider, this.mainText);
        }

        setOpacity(0);
        mainBox.setSpacing(3.5);
        mainBox.setPadding(new Insets(7));
        this.mainText.setWrapText(true);
        iconBox.setPadding(new Insets(7.5, 2, 2, 0));
    }

    public Transition animateIn() {
        FadeTransition fade = new FadeTransition(new Duration(200), this);
        fade.setToValue(1);
        TranslateTransition translate = new TranslateTransition(new Duration(200), this);
        translate.setFromY(-50);
        translate.setToY(0);
        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
        parallel.setOnFinished(event -> smoothRelocate = true);
        return parallel;
    }
    public Transition animateOut() {
        FadeTransition fade = new FadeTransition(new Duration(200), this);
        fade.setToValue(0);
        TranslateTransition translate = new TranslateTransition(new Duration(200), this);
        translate.setFromY(0);
        translate.setByY(-50);
        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
        return parallel;
    }

    @Override
    public void relocate(double x, double y) {
        if (!smoothRelocate) {
            super.relocate(x, y);
            return;
        }
        relocateScheduler.accept(new Point2D(x, y));
    }

    public void makeRemovable(EventHandler<ActionEvent> eventEventHandler) {
        SVGHoverButton closeButton = new SVGHoverButton(SVG.resizePath(SVG.X, 0.75));
        closeButton.getSvgPath().getStyleClass().addAll("notification-icon", "close-icon");
        closeButton.getSvgPath().setOpacity(0.4);
        closeButton.setFadeInTo(1);
        closeButton.setFadeOutTo(0.6);
        closeButton.getStyleClass().add("simple-button");
        topPane.setRight(new BorderPane(closeButton));
        closeButton.setOnMousePressed(event -> animateOut().setOnFinished(eventEventHandler));
    }

}
