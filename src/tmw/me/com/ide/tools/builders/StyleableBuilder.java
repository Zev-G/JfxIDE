package tmw.me.com.ide.tools.builders;

import java.util.ArrayList;
import java.util.List;

public abstract class StyleableBuilder<T extends StyleableBuilder<?, ?>, R> extends Builder<R> {

    protected List<String> styleClasses;

    public T addStyleClass(String styleClass) {
        getStyleClasses().add(styleClass);
        return (T) this;
    }

    public List<String> getStyleClasses() {
        if (styleClasses == null)
            styleClasses = new ArrayList<>();
        return styleClasses;
    }

}
