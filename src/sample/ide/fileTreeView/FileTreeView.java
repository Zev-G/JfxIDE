package sample.ide.fileTreeView;

import com.jfoenix.controls.JFXTreeView;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import sample.ide.Ide;
import sample.ide.tools.ComponentTabPane;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class FileTreeView extends JFXTreeView<File> {

    private static final HashMap<String, Image> IMAGE_CACHE = new HashMap<>();
    static {
        try {
            IMAGE_CACHE.put("default_file", imageFromSvg(FileTreeView.class.getResourceAsStream("icons/default_file.svg")));
            IMAGE_CACHE.put("default_folder", imageFromSvg(FileTreeView.class.getResourceAsStream("icons/default_folder.svg")));
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
    }

    private final File fileRoot;
    private final Ide ide;

    private final HashMap<File, TreeItem<File>> fileMap = new HashMap<>();

    public FileTreeView(File fileRoot, Ide ide) {
        this.fileRoot = fileRoot;
        this.ide = ide;
        this.setRoot(new CustomItem(fileRoot));
        getRoot().setExpanded(true);
        setCellFactory(fileTreeView -> new CustomCell());
    }

    public File getFileRoot() {
        return fileRoot;
    }
    public Ide getIde() {
        return ide;
    }
    public HashMap<File, TreeItem<File>> getFileMap() {
        return fileMap;
    }

    private static class CustomCell extends TreeCell<File> {

//        private ComponentTabPane.ComponentTab<?> tab;
        private final ContextMenu contextMenu = defaultContextMenu();
        private MenuItem requireIsFolder;
        private final EventHandler<ContextMenuEvent> contextMenuEventEventHandler = contextMenuEvent -> {
            if (contextMenuEvent.getSource() instanceof CustomCell) {
                CustomCell customCell = (CustomCell) contextMenuEvent.getSource();
                if (customCell.getItem() == null) {
                    contextMenuEvent.consume();
                } else  {
                    requireIsFolder.setDisable(!customCell.getItem().isDirectory());
                }
            } else {
                contextMenuEvent.consume();
            }
        };

        public CustomCell() {
            setOnMousePressed(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2) {
                    if (getTreeItem() != null && ((CustomItem) getTreeItem()).getComponentTab() != null && getTreeView() != null && getTreeView() instanceof FileTreeView) {
                        File value = getTreeItem().getValue();
                        if (value != null && !value.isDirectory()) {
                            ((CustomItem) getTreeItem()).setComponentTab(Ide.getNewEditorTab(value));
                            TabPane tabPane = ((FileTreeView) getTreeView()).getIde().getTabPane();
                            if (!tabPane.getTabs().contains(((CustomItem) getTreeItem()).getComponentTab())) {
                                tabPane.getTabs().add(((CustomItem) getTreeItem()).getComponentTab());
                            }
                            tabPane.getSelectionModel().select(((CustomItem) getTreeItem()).getComponentTab());
                        }
                    } else if (getTreeItem() != null && ((CustomItem) getTreeItem()).getComponentTab() != null) {
                        ((CustomItem) getTreeItem()).getComponentTab().getTabPane().getSelectionModel().select(((CustomItem) getTreeItem()).getComponentTab());
                    }
                }
            });
            setContextMenu(contextMenu);
            setOnContextMenuRequested(contextMenuEventEventHandler);
            itemProperty().addListener((observableValue, treeItem, t1) -> {
                if (getTreeView() != null && getTreeView() instanceof FileTreeView) {
                    ((FileTreeView) getTreeView()).getFileMap().remove(treeItem);
                    ((FileTreeView) getTreeView()).getFileMap().put(t1, this.getTreeItem());
                }
            });
        }

        @Override
        protected void updateItem(File file, boolean isEmpty) {
            super.updateItem(file, isEmpty);
            if (file != null) {
                setText(file.getName());
                try {
                    setGraphic(imageViewFromFile(file));
                } catch (TranscoderException e) {
                    e.printStackTrace();
                }
                if (getTreeItem() != null && ((CustomItem) getTreeItem()).getComponentTab() != null) {
                    ((CustomItem) getTreeItem()).getComponentTab().setText(file.getName());
                }
            } else {
                setText("");
                setGraphic(null);
            }
        }

        private ContextMenu defaultContextMenu() {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            MenuItem newFile = new MenuItem("New File");
            MenuItem rename = new MenuItem("Rename");
            contextMenu.getItems().addAll(newFile, rename, deleteItem);
            requireIsFolder = newFile;
            rename.setOnAction(actionEvent -> {
                if (this.getTreeView() instanceof FileTreeView) {
                    FileTreeView fileTreeView = (FileTreeView) this.getTreeView();
                    fileTreeView.getIde().showPopupForText("", this.getItem().getName(), gotten -> {
                        if (!gotten.contains("/") && !gotten.contains("\\")) {
                            File renameTo = new File(this.getItem().getParentFile().getPath() + "\\" + gotten);
                            this.getItem().renameTo(renameTo);
                            this.setText(gotten);
                            if (getTreeItem() != null && ((CustomItem) getTreeItem()).getComponentTab() != null) {
                                ((CustomItem) getTreeItem()).getComponentTab().setText(gotten);
                            }
                        }
                    });
                }
            });
            newFile.setOnAction(actionEvent -> {
                if (this.getTreeView() instanceof FileTreeView) {
                    FileTreeView fileTreeView = (FileTreeView) this.getTreeView();
                    fileTreeView.getIde().showPopupForText("", "", gotten -> {
                        if (!gotten.contains("/") && !gotten.contains("\\")) {
                            File createdFile = new File(this.getItem().getPath() + "\\" + gotten);
                            if (!createdFile.exists()) {
                                try {
                                    createdFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            CustomItem customItem = new CustomItem(createdFile);
                            this.getTreeItem().getChildren().add(customItem);
                        }
                    });
                }
            });
            deleteItem.setOnAction(actionEvent -> {
                if (this.getTreeView() instanceof FileTreeView) {
                    FileTreeView fileTreeView = (FileTreeView) this.getTreeView();
                    fileTreeView.getIde().showConfirmation("Are you sure you want to delete this file?\nThis cannot be undone.", gotten -> {
                        this.getItem().delete();
                        if (getTreeItem() != null && ((CustomItem) getTreeItem()).getComponentTab() != null && ((CustomItem) getTreeItem()).getComponentTab().getTabPane() != null) {
                            ((CustomItem) getTreeItem()).getComponentTab().getTabPane().getTabs().remove(((CustomItem) getTreeItem()).getComponentTab());
                        }
                    });
                }
            });
            return contextMenu;
        }

    }

    private static class CustomItem extends TreeItem<File> {

        private ComponentTabPane.ComponentTab<?> componentTab;

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

        public void setComponentTab(ComponentTabPane.ComponentTab<?> componentTab) {
            this.componentTab = componentTab;
        }
        public ComponentTabPane.ComponentTab<?> getComponentTab() {
            return componentTab;
        }
    }

    private static ImageView imageViewFromFile(File f) throws TranscoderException {
        String name = "";
        if (f.isDirectory()) {
            name = f.getName();
        } else if (f.getName().contains(".")) {
            name = f.getName().split("\\.")[1];
        }
        Image image = IMAGE_CACHE.get(name);
        if (image == null) {
            InputStream resource = FileTreeView.class.getResourceAsStream("icons/" + name + ".svg");
            if (resource == null) {
                image = IMAGE_CACHE.get(f.isDirectory() ? "default_folder" : "default_file");
            } else {
                image = imageFromSvg(resource);
                IMAGE_CACHE.put(name, image);
            }
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(20);
        imageView.setPreserveRatio(true);
        System.out.println(image.getUrl());

        return imageView;
    }

    public static Image imageFromSvg(InputStream inputStream) throws TranscoderException {
        BufferedImageTranscoder trans = new BufferedImageTranscoder();

        TranscoderInput transIn = new TranscoderInput(inputStream);

        trans.transcode(transIn, null);
        return SwingFXUtils.toFXImage(trans.getBufferedImage(), null);
    }

}
