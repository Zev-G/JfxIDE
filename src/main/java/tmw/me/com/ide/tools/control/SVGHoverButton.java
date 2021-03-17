package tmw.me.com.ide.tools.control;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class SVGHoverButton extends HoverButton {

    public static final Color NEAR_WHITE = Color.valueOf("#d3dae6");

    private final SVGPath svgPath = new SVGPath();

    public SVGHoverButton(String path) {
        this.setGraphic(svgPath);
        this.getStyleClass().add("transparent-bg");
        this.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                setOpacity(fadeInToProperty().get());
            } else {
                setOpacity(fadeOutToProperty().get());
            }
        });
        svgPath.setContent(path);
        svgPath.setFill(NEAR_WHITE);
    }

    public SVGPath getSvgPath() {
        return svgPath;
    }

}
