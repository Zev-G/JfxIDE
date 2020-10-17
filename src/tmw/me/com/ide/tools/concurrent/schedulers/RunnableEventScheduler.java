package tmw.me.com.ide.tools.concurrent.schedulers;

/**
 * This implementation implements {@link Runnable} making it the most simple implementation. It is recommended you use this implementation when no function parameters are needed.
 * <p>See {@link EventSchedulerBase} for more info.</p>
 * */
public class RunnableEventScheduler extends EventSchedulerBase<Runnable> implements Runnable {

    public RunnableEventScheduler(long delay, Runnable runnable) {
        waitTime = delay;
        getsRan = runnable;
    }

    @Override
    public void run() {
        ran(getsRan);
    }
}
