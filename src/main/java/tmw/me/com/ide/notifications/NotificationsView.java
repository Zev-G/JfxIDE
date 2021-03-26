package tmw.me.com.ide.notifications;

import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tmw.me.com.Resources;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationsView extends VBox {

    private static final String STYLE_SHEET = Resources.getExternalForm("ide/styles/components/notifications.css");

    public NotificationsView() {
        getStylesheets().add(STYLE_SHEET);
        getStyleClass().add("notifications-box");
    }

    public void showNotification(Duration length, Notification show) {
        showNotification(new Duration(0), length, show);
    }
    public void showNotification(Duration delay, Duration length, Notification show) {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    getChildren().add(show);
                    show.animateIn();
                    if (length != null) {
                        new Timer(true).schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> show.animateOut().setOnFinished(event -> getChildren().remove(show)));
                            }
                        }, (long) length.toMillis());
                    } else {
                        show.makeRemovable(event -> getChildren().remove(show));
                    }
                });
            }
        }, (long) delay.toMillis());
    }

}
