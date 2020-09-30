package tmw.me.com.ide.tools.concurrent;

import java.util.function.Consumer;

/**
 * This implementation implements {@link Consumer} making it useful when used in cases where you would otherwise use a consumer. This is the only integration for Consumers so should you need more parameters
 * you either need to create your own implementation or make the generic parameter a class which stores the variable you need to access.
 * <p>See {@link tmw.me.com.ide.tools.concurrent.EventSchedulerBase} for more info.</p>
 * */
public class ConsumerEventScheduler<T> extends EventSchedulerBase<Consumer<T>> implements Consumer<T> {

    public ConsumerEventScheduler(long delay, Consumer<T> consumer) {
        this(delay, true, consumer);
    }
    public ConsumerEventScheduler(long delay, boolean fxThread, Consumer<T> consumer) {
        getsRan = consumer;
        waitTime = delay;
        this.runOnFx = fxThread;
    }

    @Override
    public void accept(T t) {
        ran(() -> getsRan.accept(t));
    }

}
