package tmw.me.com.ide.tools.concurrent.schedulers;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;

/**
 * This implementation implements {@link EventHandler} so that it has integration with the EventHandler of a node.
 * <p>See {@link EventSchedulerBase} for more info.</p>
 */
public class JFXEventScheduler<E extends Event> extends EventSchedulerBase<EventHandler<E>> implements EventHandler<E> {

    public JFXEventScheduler(long delay, EventHandler<E> eventHandler) {
        waitTime = delay;
        getsRan = eventHandler;
    }

    public static <T extends EventType<E>, E extends Event> JFXEventScheduler<E> attach(Node node, T eventType, long delay, EventHandler<E> eventHandler) {
        JFXEventScheduler<E> jfxEventScheduler = new JFXEventScheduler<>(delay, eventHandler);
        node.addEventFilter(eventType, jfxEventScheduler);
        return jfxEventScheduler;
    }

    @Override
    public void handle(E e) {
        ran(() -> getsRan.handle(e));
    }
}
