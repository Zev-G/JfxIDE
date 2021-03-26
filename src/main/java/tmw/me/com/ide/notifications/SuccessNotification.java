package tmw.me.com.ide.notifications;

import tmw.me.com.ide.tools.SVG;

public class SuccessNotification extends Notification {

    public SuccessNotification(String title) {
        this(title, null);
    }
    public SuccessNotification(String title, String mainText) {
        super(title, mainText);
        icon.setContent(SVG.resizePath(SVG.CHECK_MARK, 0.035));
        getStyleClass().add("success-notification");
    }

}
