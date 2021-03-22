package tmw.me.com.ide.settings.visual.fields.direct;

import javafx.beans.value.WritableValue;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import tmw.me.com.ide.fileTreeView.FileTreeView;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;

import java.io.File;

public class FileField extends AnnotatedTextField<String> {

    private final ImageView fileChooserButton = new FileChooser(getEditMethod());

    public FileField(String text, DisplayedJSON annotation, WritableValue<String> writeTo) {
        super(text, annotation, writeTo);

        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (new File(newValue).exists()) {
                field.getStyleClass().remove("fr-error");
            } else if (!field.getStyleClass().contains("fr-error")) {
                field.getStyleClass().add("fr-error");
            }
        });
        if (!new File(text).exists()) {
            field.getStyleClass().add("fr-error");
        }

        getChildren().add(0, fileChooserButton);
    }

    @Override
    public Object getValue() {
        return getText();
    }

    @Override
    protected void textChanged(String newText) {
        super.textChanged(newText);
        writeTo.setValue(newText);
    }

    private class FileChooser extends ImageView {

        private final DisplayedJSON.EditMethod editMethod;

        private FileChooser(DisplayedJSON.EditMethod editMethod) {
            super(FileTreeView.getFolderImage());
            this.editMethod = editMethod;

            setFitHeight(fontSize * 1.6);
            setPreserveRatio(true);

            setOnMousePressed(this::onPressed);

            if (editable) {
                setCursor(Cursor.HAND);
            }
        }

        private void onPressed(MouseEvent event) {
            if (!editable)
                return;
            File result;
            File previousPath = new File(getText());
            if (editMethod == DisplayedJSON.EditMethod.FOLDER) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                if (previousPath.exists())
                    directoryChooser.setInitialDirectory(previousPath);
                result = directoryChooser.showDialog(getScene().getWindow());
            } else if (editMethod == DisplayedJSON.EditMethod.FILE) {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                if (previousPath.exists())
                    fileChooser.setInitialDirectory(previousPath);
                result = fileChooser.showOpenDialog(getScene().getWindow());
            } else {
                return;
            }
            if (result != null)
                setText(result.getAbsolutePath());
        }

    }

}
