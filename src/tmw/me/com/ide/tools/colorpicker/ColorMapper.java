package tmw.me.com.ide.tools.colorpicker;

import javafx.scene.paint.Color;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;

public final class ColorMapper {

    private static final HashMap<String, Color> COLOR_MAP = new HashMap<>();

    public static HashMap<String, Color> getColorMap() {
        if (COLOR_MAP.isEmpty()) {
            Arrays.stream(Color.class.getFields()).filter(field -> Modifier.isStatic(field.getModifiers())).filter(field -> field.getType() == Color.class).
                    forEach(field -> {
                        try {
                            COLOR_MAP.put(field.getName(), (Color) field.get(null));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
        }
        return COLOR_MAP;
    }

    public static Color fromString(String segment) {
        segment = segment.toLowerCase().trim();
        if (COLOR_MAP.containsKey(segment)) {
            return COLOR_MAP.get(segment);
        } else {
            return Color.web(segment);
        }
    }
}
