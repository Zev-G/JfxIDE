package tmw.me.com.ide.settings.visual.fields;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;
import tmw.me.com.ide.tools.NodeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ClassFields<T> extends AnnotatedField<T> {

    private final VBox layoutVBox = new VBox();
    private final List<AnnotatedField<?>> childrenFields = new ArrayList<>();

    public static ScrollPane getWrappedInScrollpane(Object obj) {
        ScrollPane scrollPane = new ScrollPane(new Pane(getInstanceFromObject(obj)));
        scrollPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            StackPane stackPane = (StackPane) scrollPane.lookup("ScrollPane .viewport");
            stackPane.setCache(false);
        });
        return scrollPane;
    }

    public static <T> ClassFields<T> getInstanceFromObject(T object) {
        DisplayedJSON foundAnnotation = object.getClass().getAnnotation(DisplayedJSON.class);
        if (foundAnnotation != null) {
            return new ClassFields<>(object, foundAnnotation);
        }
        return null;
    }

    public ClassFields(T from, DisplayedJSON annotation) {
        super(from, annotation, null);

        AtomicReference<Class<?>> lastFrom = new AtomicReference<>(null);
        Arrays.stream(from.getClass().getFields())
                .filter(field -> Arrays.stream(field.getAnnotations()).anyMatch(annotation1 -> annotation1 instanceof DisplayedJSON))
                .sorted((o1, o2) -> {
                    DisplayedJSON o1Properties = o1.getAnnotation(DisplayedJSON.class);
                    DisplayedJSON o2Properties = o2.getAnnotation(DisplayedJSON.class);
                    return o1Properties.enforcePosition() - o2Properties.enforcePosition();
                })
                .forEach(field -> {
                    DisplayedJSON fieldAnnotation = field.getAnnotation(DisplayedJSON.class);
                    if (field.getDeclaringClass() != lastFrom.get() && lastFrom.get() != null) {
                        layoutVBox.getChildren().add(NodeUtils.divider(3, 0, 5));
                    }
                    lastFrom.set(field.getDeclaringClass());
                    if (fieldAnnotation != null) {
                        try {
                            Object val = field.get(from);
                            AnnotatedField<?> annotatedField = AnnotatedField.fromField(val, fieldAnnotation, NodeUtils.writableFromConsumer(o -> {
                                try {
                                    field.set(from, o);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }));
                            annotatedField.savedProperty().addListener((a, b, c) -> {
                                if (!c) {
                                    saved.set(false);
                                }
                            });
                            childrenFields.add(annotatedField);
                            layoutVBox.getChildren().add(annotatedField);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });

        HBox.setHgrow(layoutVBox, Priority.ALWAYS);

        getChildren().add(layoutVBox);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void saved() {
        super.saved();
        childrenFields.forEach(AnnotatedField::saved);
    }

}
