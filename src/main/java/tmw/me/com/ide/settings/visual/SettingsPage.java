package tmw.me.com.ide.settings.visual;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import tmw.me.com.ide.codeEditor.languages.addon.JSONHelper;
import tmw.me.com.ide.settings.SettingsJSON;
import tmw.me.com.ide.settings.visual.fields.ClassFields;
import tmw.me.com.jfxhelper.NodeUtils;
import tmw.me.com.ide.tools.control.HoverButton;

import java.io.File;

public class SettingsPage<T extends SettingsJSON> extends AnchorPane {

    private final File writeChangesTo;
    private final T initialValue;

    private final ScrollPane scrollParent = new ScrollPane();
    private final ClassFields<T> topLayout;

    private final HoverButton saveButton = new HoverButton("Save");

    public SettingsPage(File writeChangesTo, T initialValue) {
        this.writeChangesTo = writeChangesTo;
        this.initialValue = initialValue;

        topLayout = ClassFields.getInstanceFromObject(initialValue);
        assert topLayout != null;
        scrollParent.setContent(topLayout);
        scrollParent.setFitToWidth(true);

        getChildren().addAll(scrollParent);
        if (writeChangesTo != null) {
            getChildren().add(saveButton);
        }
        NodeUtils.anchor(scrollParent, 0);
        AnchorPane.setRightAnchor(saveButton, 5D);
        AnchorPane.setTopAnchor(saveButton, 5D);

//        getStyleClass().add("darkest-background");
        setStyle("-fx-background-color: -fx-background;");

        topLayout.savedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                saveButton.setMouseTransparent(false);
                saveButton.setText("Save");
                saveButton.setFadeOutTo(0.7);
            } else {
                saveButton.setMouseTransparent(true);
                saveButton.setText("Saved");
                saveButton.setFadeOutTo(0.3);
            }
        });
        saveButton.setMouseTransparent(true);
        saveButton.setText("Saved");
        saveButton.setFadeOutTo(0.3);

        saveButton.setOnAction(event -> {
            topLayout.saved();
            JSONHelper.toFile(writeChangesTo, initialValue);
        });

        setPadding(new Insets(10));
    }

    public File getWriteChangesTo() {
        return writeChangesTo;
    }

    public T getInitialValue() {
        return initialValue;
    }

}
