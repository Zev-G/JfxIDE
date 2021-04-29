package tmw.me.com.jfxhelper;

import javafx.beans.value.ObservableNumberValue;
import javafx.scene.Node;
import tmw.me.com.ide.tools.concurrent.schedulers.ChangeListenerScheduler;

import java.util.function.Consumer;

public final class BindingUtils {

    public static void bindFontSize(Node node, ObservableNumberValue listenTo) {
        Consumer<Number> update = aDouble -> NodeUtils.setFontSize(node, aDouble);
        update.accept(listenTo.getValue());
        listenTo.addListener((observable, oldValue, newValue) -> update.accept(newValue));
    }

    public static void delayedBindFontSize(Node node, ObservableNumberValue listenTo, long wait) {
        Consumer<Number> update = aDouble -> NodeUtils.setFontSize(node, aDouble);
        update.accept(listenTo.getValue());
        listenTo.addListener(new ChangeListenerScheduler<>(wait, (observable, oldValue, newValue) -> update.accept(newValue)));
    }
}
