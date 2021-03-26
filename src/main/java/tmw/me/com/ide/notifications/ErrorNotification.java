package tmw.me.com.ide.notifications;

import tmw.me.com.ide.tools.SVG;

public class ErrorNotification extends Notification {

    public ErrorNotification(String title, String mainText) {
        this.notificationTitle.setText(title);
        this.mainText.setText(mainText);
        icon.setContent(SVG.resizePath(SVG.WARNING, 0.045));
        getStyleClass().add("error-notification");
    }

}
