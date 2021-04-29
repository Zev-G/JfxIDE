package tmw.me.com.jfxhelper;

import javafx.beans.property.Property;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.util.function.Function;

public final class CssUtils {

    public static <T extends Styleable> CssMetaData<T, Number> simpleMetaData(String name, Number defaultValue, Function<T, StyleableProperty<Number>> fact) {
        return simpleMetaData(name, defaultValue, fact, StyleConverter.getSizeConverter());
    }
    public static <T extends Styleable> CssMetaData<T, String> simpleMetaData(String name, String defaultValue, Function<T, StyleableProperty<String>> fact) {
        return simpleMetaData(name, defaultValue, fact, StyleConverter.getStringConverter());
    }
    public static <T extends Styleable> CssMetaData<T, Color> simpleMetaData(String name, Color defaultValue, Function<T, StyleableProperty<Color>> fact) {
        return simpleMetaData(name, defaultValue, fact, StyleConverter.getColorConverter());
    }
    public static <T extends Styleable> CssMetaData<T, Boolean> simpleMetaData(String name, Boolean defaultValue, Function<T, StyleableProperty<Boolean>> fact) {
        return simpleMetaData(name, defaultValue, fact, StyleConverter.getBooleanConverter());
    }
    public static <T extends Styleable, E extends Enum<E>> CssMetaData<T, E> simpleMetaData(Class<E> enumInput, String name, E defaultValue, Function<T, StyleableProperty<E>> fact) {
        return simpleMetaData(name, defaultValue, fact, StyleConverter.getEnumConverter(enumInput));
    }
    public static <T extends Styleable> CssMetaData<T, Insets> simpleMetaData(String name, Insets defaultValue, Function<T, StyleableProperty<Insets>> fact) {
        return simpleMetaData(name, defaultValue, fact, StyleConverter.getInsetsConverter());
    }
    public static <T extends Styleable> CssMetaData<T, Paint> simpleMetaData(String name, Paint defaultValue, Function<T, StyleableProperty<Paint>> fact) {
        return simpleMetaData(name, defaultValue, fact, StyleConverter.getPaintConverter());
    }
    public static <T extends Styleable> CssMetaData<T, Duration> simpleMetaData(String name, Duration defaultValue, Function<T, StyleableProperty<Duration>> fact) {
        return simpleMetaData(name, defaultValue, fact, StyleConverter.getDurationConverter());
    }
    public static <T extends Styleable, V> CssMetaData<T, V> simpleMetaData(String name, V defaultValue, Function<T, StyleableProperty<V>> fact, StyleConverter<?, V> converter) {
        return new CssMetaData<>(name, converter, defaultValue) {

            @Override
            public boolean isSettable(T styleable) {
                StyleableProperty<V> property = getStyleableProperty(styleable);
                if (property == null) return true;
                if (property instanceof Property) return ((Property<?>) property).isBound();
                return false;
            }

            @Override
            public StyleableProperty<V> getStyleableProperty(T styleable) {
                return fact.apply(styleable);
            }

        };
    }

    public static String append(String style, String appendTo) {
        if (!appendTo.trim().endsWith(";")) appendTo = appendTo.trim() + ";";
        if (style.trim().endsWith(";")) {
            return style + " " + appendTo;
        } else {
            return style.trim() + "; " + appendTo;
        }
    }

    public static String editProperty(String style, String property, Object newValue) {
        property = property.trim();
        String newVal = newValue.toString();
        if (!property.endsWith(":")) property = property + ":";
        return style.replaceFirst(property + "[^;]*?;", property + newVal + ";");
    }
}
