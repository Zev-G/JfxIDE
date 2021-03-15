package tmw.me.com.ide.codeEditor;

import javafx.stage.Popup;
import javafx.stage.Stage;
import org.reactfx.EventStream;
import org.reactfx.value.Var;

public class BoundsPopup extends Popup {

    public BoundsPopup() {
        super();
    }

    private final Var<Boolean> showWhenItemOutsideViewport = Var.newSimpleVar(true);

    public final EventStream<Boolean> outsideViewportValues() {
        return showWhenItemOutsideViewport.values();
    }

    public final void invertViewportOption() {
        showWhenItemOutsideViewport.setValue(!showWhenItemOutsideViewport.getValue());
    }

    /**
     * Indicates whether popup has been hidden since its item (caret/selection) is outside viewport
     * and should be shown when that item becomes visible again
     */
    private final Var<Boolean> hideTemporarily = Var.newSimpleVar(false);

    public final boolean isHiddenTemporarily() {
        return hideTemporarily.getValue();
    }

    public final void setHideTemporarily(boolean value) {
        hideTemporarily.setValue(value);
    }

    public final void invertVisibility(Stage stage) {
        if (isShowing()) {
            hide();
        } else {
            show(stage);
        }
    }

}
