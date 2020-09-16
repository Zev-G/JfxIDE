package sample.ide.fileTreeView;

import com.jfoenix.controls.JFXTreeView;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import sample.ide.Ide;
import sample.ide.tools.ComponentTabPane;

import java.io.File;
import java.util.ArrayList;

public class FileTreeView extends JFXTreeView<File> {

    private File fileRoot;
    private final Ide ide;

    public FileTreeView(File fileRoot, Ide ide) {
        this.fileRoot = fileRoot;
        this.ide = ide;
        this.setRoot(new CustomItem(fileRoot));
        setCellFactory(fileTreeView -> new CustomCell());
    }

    public File getFileRoot() {
        return fileRoot;
    }
    public Ide getIde() {
        return ide;
    }

    private static class CustomCell extends TreeCell<File> {

        private ComponentTabPane.ComponentTab<?> tab;

        public CustomCell() {
            setOnMousePressed(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2) {
                    if (tab == null && getTreeItem() != null && getTreeView() != null && getTreeView() instanceof FileTreeView) {
                        File value = getTreeItem().getValue();
                        if (value != null && !value.isDirectory()) {
                            tab = Ide.getNewEditorTab(value);
                            TabPane tabPane = ((FileTreeView) getTreeView()).getIde().getTabPane();
                            if (!tabPane.getTabs().contains(tab)) {
                                tabPane.getTabs().add(tab);
                            }
                            tabPane.getSelectionModel().select(tab);
                        }
                    } else if (tab != null) {
                        tab.getTabPane().getSelectionModel().select(tab);
                    }
                }
            });
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            Menu newMenu = new Menu("New");
//            setContextMenu();
        }

        @Override
        protected void updateItem(File file, boolean isEmpty) {
            super.updateItem(file, isEmpty);
            if (file != null) {
                setText(file.getName());
                setGraphic(imageViewFromFile(file));
            } else {
                setText("");
                setGraphic(null);
            }
        }
    }

    private static class CustomItem extends TreeItem<File> {

        public CustomItem() {
            super();
        }
        public CustomItem(File file) {
            super(file);
            File[] listFiles = file.listFiles();
            if (file.isDirectory() && listFiles != null) {
                this.getChildren().add(new CustomItem());
            }
            this.expandedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        ArrayList<TreeItem<File>> newTreeItems = new ArrayList<>();
                        for (File loopFile : files) {
                            newTreeItems.add(new CustomItem(loopFile));
                        }
                        this.getChildren().setAll(newTreeItems);
                    }
                }
            });
        }



    }

    private static ImageView imageViewFromFile(File f) {
        return new ImageView();
    }

}
