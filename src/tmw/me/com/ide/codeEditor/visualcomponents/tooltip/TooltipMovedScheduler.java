package tmw.me.com.ide.codeEditor.visualcomponents.tooltip;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.Timer;
import java.util.TimerTask;

public class TooltipMovedScheduler implements EventHandler<MouseEvent> {

    private final EditorTooltip tooltip;

    private TimerTask lastTimerTask;
    private final Timer timer = new Timer(true);

    private Node currentHover;

    public TooltipMovedScheduler(EditorTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void handle(MouseEvent event) {
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        if (currentHover == null) {
            if (lastTimerTask != null) {
                lastTimerTask.cancel();
            }
            timer.purge();
            lastTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> currentHover = intersectedNode);
                }
            };
            timer.schedule(lastTimerTask, EditorTooltip.MOUSE_OVER_DELAY);
        } else if (intersectedNode != currentHover) {
            currentHover = null;
            if (tooltip.isShowing()) {
                tooltip.hide();
            }
        }
    }

    public void setCurrentHover(Node currentHover) {
        this.currentHover = currentHover;
    }

    public Node getCurrentHover() {
        return currentHover;
    }
}
