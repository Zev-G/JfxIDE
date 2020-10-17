package tmw.me.com.ide.tools.concurrent.schedulers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * This implementation implements {@link ChangeListener} so that it has seamless integration when listening to a property.
 * <p>See {@link EventSchedulerBase} for more info.</p>
 * */
public class ChangeListenerScheduler<T> extends EventSchedulerBase<ChangeListener<T>> implements ChangeListener<T> {

    public ChangeListenerScheduler(long after, ChangeListener<T> changeListener) {
        this(after, true, changeListener);
    }
    public ChangeListenerScheduler(long after, boolean fxThread, ChangeListener<T> changeListener) {
        waitTime = after;
        getsRan = changeListener;
        runOnFx = fxThread;
    }

    public static <T> ChangeListenerScheduler<T> attachListener(ObservableValue<T> observable, long after, ChangeListener<T> changeListener) {
        ChangeListenerScheduler<T> changeListenerScheduler = new ChangeListenerScheduler<>(after, changeListener);
        observable.addListener(changeListenerScheduler);
        return changeListenerScheduler;
    }

    @Override
    public void changed(ObservableValue<? extends T> observableValue, T t, T t1) {
        ran(() -> getsRan.changed(observableValue, t, t1));
    }
}
