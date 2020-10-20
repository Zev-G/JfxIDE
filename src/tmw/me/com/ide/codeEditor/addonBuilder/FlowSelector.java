package tmw.me.com.ide.codeEditor.addonBuilder;

import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import org.reactfx.util.TriFunction;

public class FlowSelector<T> extends FlowPane {


    private TriFunction<String, String, T, ? extends SelectorItem<T>> selectorItemFactory = (s, s2, t) -> {
        SelectorItem<T> selectorItem = new SelectorItem<>(t, s, s2);
        selectorItem.addHoverAnimation();
        selectorItem.randomizeColor(127);
        return selectorItem;
    };

    @SafeVarargs
    public FlowSelector(SelectorItem<T>... items) {
        super(items);
        this.setPadding(new Insets(30, 30, 30, 30));
        this.setVgap(10);
        this.setHgap(10);
    }

    public void add(T value, String title, String subTitle) {
        this.getChildren().add(selectorItemFactory.apply(title, subTitle, value));
    }

    public TriFunction<String, String, T, ? extends SelectorItem<T>> getSelectorItemFactory() {
        return selectorItemFactory;
    }

    public void setSelectorItemFactory(TriFunction<String, String, T, ? extends SelectorItem<T>> selectorItemFactory) {
        this.selectorItemFactory = selectorItemFactory;
    }
}
