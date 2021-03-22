package tmw.me.com.ide.settings.visual.fields;

import javafx.collections.FXCollections;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;
import tmw.me.com.ide.tools.NodeUtils;
import tmw.me.com.ide.tools.SVG;
import tmw.me.com.ide.tools.control.SVGHoverButton;

import java.util.*;

public class AnnotatedList<T> extends AnnotatedField<Collection<T>> implements VisualField {

    private final TitledPane topPane = new TitledPane();
    private final VBox layout = new VBox();
    private final List<T> values;
    private final DisplayedJSON settings;
    private final boolean editable;

    private final List<AnnotatedField<T>> fields = FXCollections.observableArrayList();

    public AnnotatedList(String title, T[] values, DisplayedJSON settings) {
        this(title, Arrays.asList(values), settings);
    }
    public AnnotatedList(String title, List<T> values, DisplayedJSON settings) {
        super(values, settings, null);
        topPane.setText(title);
        topPane.setContent(layout);
        topPane.getStyleClass().add("json-titled-pane");

        this.values = values;
        this.settings = settings;

        boolean tempEditable = true;
        try {
            values.addAll(Collections.emptyList());
        } catch (UnsupportedOperationException exception) {
            tempEditable = false;
        } finally {
            editable = tempEditable;
        }

        layout.setSpacing(4.5);
        layout.getStyleClass().add("json-list-vbox");

        for (Object val : values) {
            AnnotatedField<T> field = getCustomInstance(val, fields.size());
            fields.add(field);
            layout.getChildren().add(field);
        }
        if (editable && settings.editMethod() != DisplayedJSON.EditMethod.FULL_OBJECT) {
            SVGHoverButton addButton = new SVGHoverButton(SVG.resizePath(SVG.PLUS, 0.8 * (settings.fontSize() / 14D)));
            addButton.getStyleClass().add("simple-button");
            addButton.setOnAction(event -> {
                AnnotatedField<T> field = getCustomInstance(null, values.size());
                layout.getChildren().add(layout.getChildren().indexOf(addButton), field);
                fields.add(field);
                values.add(null);
                saved.set(false);
            });
            layout.getChildren().add(addButton);
        }

        getChildren().remove(getTitleLabel());
        getChildren().add(topPane);
    }

    public AnnotatedField<T> getCustomInstance(Object val, int index) {
        AnnotatedField<T> field = (AnnotatedField<T>) AnnotatedField.fromField(val, settings,
                NodeUtils.writableFromConsumer(o -> values.set(index, (T) o)));
        if (editable) {
            field.makeRemovable(annotatedField -> {
                values.remove(fields.indexOf(field));
                fields.remove(field);
                layout.getChildren().remove(field);
                saved.set(false);
            });
        }
        field.savedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                saved.set(false);
            }
        });
        return field;
    }

    @Override
    public Object getValue() {
        if (fields.isEmpty()) {
            return null;
        }
        if (fields.size() == 1) {
            return fields.get(0).getValue();
        }
        ArrayList<String> asList = new ArrayList<>();
        fields.forEach(annotatedField -> asList.add(annotatedField.getValue().toString()));
        return editable ? asList : Arrays.asList(asList.toArray());
    }

    @Override
    public void saved() {
        super.saved();
        for (AnnotatedField<T> field : fields) {
            field.saved();
        }
    }
}
