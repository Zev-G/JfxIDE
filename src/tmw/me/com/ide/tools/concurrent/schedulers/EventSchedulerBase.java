package tmw.me.com.ide.tools.concurrent.schedulers;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * <p>This is the base class for all other EventScheduler classes.
 *  These classes will cause a FunctionalInterface of some kind to get called after a certain amount of time.
 *  <p>If the functional interface is called again before the previous call finishes (the delay is over) the first call won't run.
 *  So in effect only the last call between the specified interval will be called, this is very useful for limiting the number of times a segment of code has to be ran.
 *  If for example you only want to run a segment of code after someone finishes typing you could listen to the textProperty of the text component with the {@link ChangeListenerScheduler}
 *  if you give it a delay of 600 ms the ChangeListener will only be called after the user hasn't typed for 600ms, therefore the calculation won't be made too often.</p>
 * */
public abstract class EventSchedulerBase<T> {


    protected long waitTime;
    protected int timesActivated = 0;
    protected T getsRan;
    protected boolean runOnFx = true;
    protected TimerTask lastTimerTask;

    protected final Timer timer = new Timer(true);

    protected void ran(Runnable runOnFxThread) {
        if (lastTimerTask != null) {
            lastTimerTask.cancel();
        }
        timer.purge();
        timesActivated++;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (runOnFx)
                    Platform.runLater(() -> {
                        runOnFxThread.run();
                        timesActivated = 0;
                    });
                else {
                    runOnFxThread.run();
                    timesActivated = 0;
                }
            }
        };
        lastTimerTask = timerTask;
        timer.schedule(timerTask, waitTime);
    }

    public long getWaitTime() {
        return waitTime;
    }
    public T getGetsRan() {
        return getsRan;
    }
    public boolean isRunOnFx() {
        return runOnFx;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }
    public void setGetsRan(T getsRan) {
        this.getsRan = getsRan;
    }
    public void setRunOnFx(boolean runOnFx) {
        this.runOnFx = runOnFx;
    }

    public int getTimesActivated() {
        return timesActivated;
    }
}
