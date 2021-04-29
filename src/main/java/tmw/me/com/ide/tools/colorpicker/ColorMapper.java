package tmw.me.com.ide.tools.colorpicker;

import javafx.scene.paint.Color;
import tmw.me.com.jfxhelper.NodeUtils;

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
        } else if (isValidColorFunction(segment)) {
            if (segment.startsWith("rgb")) {
                boolean isRGBA = segment.startsWith("rgba");
                String postRGB = segment.substring(
                        isRGBA ? 5 : 4
                );
                postRGB = postRGB.substring(0, postRGB.length() - 1).replaceAll("[()]", "");
                String[] colors = postRGB.split(",\\s+");
                if (colors.length >= 3) {
                    int red = Integer.parseInt(colors[0]);
                    int green = Integer.parseInt(colors[1]);
                    int blue = Integer.parseInt(colors[2]);
                    System.out.println("red: " + red + " green: " + green + " blue: " + blue);
                    if (!isRGBA) {
                        return Color.rgb(red, green, blue);
                    } else if (colors.length > 3) {
                        double opacity = Double.parseDouble(colors[3]);
                        return Color.rgb(red, green, blue, opacity);
                    }
                }
            }
        }
        return Color.web(segment);
    }

    public static String colorToString(Color color, String findTypeFrom) {
        String trimmedType = findTypeFrom.trim();
        if (trimmedType.startsWith("rgba")) {
            return "rgba(" + (int) (color.getRed() * 255) + ", " + (int) (color.getGreen() * 255) + ", " + (int) (color.getBlue() * 255) + ", " + (((double) ((int) (color.getOpacity() * 100))) / 100) + ")";
        } else if (trimmedType.startsWith("rgb")) {
            return "rgb(" + (int) (color.getRed() * 255) + ", " + (int) (color.getGreen() * 255) + ", " + (int) (color.getBlue() * 255) + ")";
        }
        return NodeUtils.colorToWeb(color);
    }

    public static boolean isValidColorFunction(String segment) {
        return segment.startsWith("rgb(") || segment.startsWith("rgba(") || segment.startsWith("hsb(") || segment.startsWith("hsba(");
    }

}
