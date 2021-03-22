package tmw.me.com.ide.settings.visual;

import javafx.beans.value.WritableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;
import tmw.me.com.ide.settings.visual.fields.AnnotatedField;
import tmw.me.com.ide.tools.NodeUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class AnnotationVisualizer {

    public static Node deriveFromField(Field field, Object val) throws IllegalAccessException {
        List<Annotation> annotations = Arrays.asList(field.getAnnotations());
        DisplayedJSON foundAnnotation = (DisplayedJSON) annotations.stream().filter(annotation -> annotation instanceof DisplayedJSON).findFirst().orElse(null);
        if (foundAnnotation != null) {
            return AnnotatedField.fromField(field.get(val), foundAnnotation, new WritableValue<>() {
                @Override
                public Object getValue() {
                    return null;
                }

                @Override
                public void setValue(Object value) {
                    try {
                        field.set(val, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return null;
    }

    public static Parent deriveFromObject(Object from) {
        VBox layout = new VBox();
        AtomicReference<Class<?>> lastFrom = new AtomicReference<>(null);
        Arrays.stream(from.getClass().getFields())
                .filter(field -> Arrays.stream(field.getAnnotations()).anyMatch(annotation -> annotation instanceof DisplayedJSON))
                .sorted((o1, o2) -> {
                    DisplayedJSON o1Properties = o1.getAnnotation(DisplayedJSON.class);
                    DisplayedJSON o2Properties = o2.getAnnotation(DisplayedJSON.class);
                    return o1Properties.enforcePosition() - o2Properties.enforcePosition();
                })
                .forEach(field -> {
                    try {
                        if (field.getDeclaringClass() != lastFrom.get() && lastFrom.get() != null) {
                            layout.getChildren().add(NodeUtils.divider(3, 0, 5));
                        }
                        lastFrom.set(field.getDeclaringClass());
                        Node fromField = deriveFromField(field, from);
                        if (fromField != null)
                            layout.getChildren().add(fromField);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
        layout.setSpacing(4);
        return layout;
    }



}
