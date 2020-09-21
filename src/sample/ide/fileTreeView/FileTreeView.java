package sample.ide.fileTreeView;

import com.jfoenix.controls.JFXTreeView;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import sample.ide.Ide;
import sample.ide.tools.ComponentTabPane;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

public class FileTreeView extends JFXTreeView<File> {

    private static final HashMap<String, Image> IMAGE_CACHE = new HashMap<>();
    private static final ArrayList<File> DELETE_ON_EXIT = new ArrayList<>();
    static {
        try {
            IMAGE_CACHE.put("default_file", imageFromSvg(FileTreeView.class.getResourceAsStream("icons/default_file.svg")));
            IMAGE_CACHE.put("default_folder", imageFromSvg(FileTreeView.class.getResourceAsStream("icons/default_folder.svg")));
            IMAGE_CACHE.put("default_folder_opened", imageFromSvg(FileTreeView.class.getResourceAsStream("icons/default_folder_opened.svg")));
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

        private ContextMenu contextMenu;
        private MenuItem requireIsFolder;
        private EventHandler<ContextMenuEvent> contextMenuEventEventHandler;
        private ChangeListener<Boolean> expandedChangeListener;

        public CustomCell() {
            itemProperty().addListener((observableValue, treeItem, t1) -> {
                if (getTreeView() != null && getTreeView() instanceof FileTreeView) {
                    ((FileTreeView) getTreeView()).getFileMap().remove(treeItem);
                    ((FileTreeView) getTreeView()).getFileMap().put(t1, this.getTreeItem());
                }
                if (t1 != null && t1.isDirectory() && getTreeItem() != null) {
                    if (expandedChangeListener != null) {
                        getTreeItem().expandedProperty().removeListener(expandedChangeListener);
                    } else {
                        expandedChangeListener = (observableValue1, aBoolean, t11) -> {
                            try {
                                setGraphic(imageViewFromFile(t1));
                            } catch (TranscoderException e) {
                                e.printStackTrace();
                            }
                        };
                    }
                }
            });
            treeItemProperty().addListener((observableValue, fileTreeItem, t1) -> {
                if (contextMenu == null) {
                    contextMenu = defaultContextMenu();
                    setContextMenu(contextMenu);
                }
                if (contextMenuEventEventHandler == null) {
                    contextMenuEventEventHandler = defaultContextMenuEvent();
                    setOnContextMenuRequested(contextMenuEventEventHandler);
                }
                if (getOnMousePressed() == null) {
                    setOnMousePressed(mouseEvent -> {
                        if (mouseEvent.getClickCount() == 2 && getTreeItem() != null && getTreeItem() instanceof CustomItem) {
                            CustomItem item = (CustomItem) getTreeItem();

                            if (getTreeItem() != null && item.getComponentTab() == null && getTreeView() != null && getTreeView() instanceof FileTreeView) {
                                File value = getTreeItem().getValue();
                                if (value != null && !value.isDirectory()) {
                                    item.setComponentTab(Ide.getNewEditorTab(value));
                                    TabPane tabPane = ((FileTreeView) getTreeView()).getIde().getTabPane();
                                    if (!tabPane.getTabs().contains(item.getComponentTab())) {
                                        tabPane.getTabs().add(item.getComponentTab());
                                    }
                                    tabPane.getSelectionModel().select(item.getComponentTab());
                                }
                            } else if (getTreeItem() != null) {
                                if (item.getComponentTab().getTabPane() != null) {
                                    item.getComponentTab().getTabPane().getSelectionModel().select(item.getComponentTab());
                                } else if (getTreeView() != null && getTreeView() instanceof FileTreeView) {
                                    TabPane tabPane = ((FileTreeView) getTreeView()).getIde().getTabPane();
                                    if (!tabPane.getTabs().contains(item.getComponentTab())) {
                                        tabPane.getTabs().add(item.getComponentTab());
                                    }
                                    tabPane.getSelectionModel().select(item.getComponentTab());
                                }
                            }
                        }
                    });
                }
                if (getOnDragDetected() == null) {
                    setOnDragDetected(mouseEvent -> {
                        if (getItem() != null) {
                            ClipboardContent clipboardContent = new ClipboardContent();
                            ArrayList<File> arrayList = new ArrayList<>();
                            arrayList.add(getItem());
                            clipboardContent.putFiles(arrayList);
                            Dragboard dragboard = this.startDragAndDrop(TransferMode.MOVE);
                            dragboard.setContent(clipboardContent);
                            SnapshotParameters snapshotParameters = new SnapshotParameters();
                            snapshotParameters.setFill(Color.TRANSPARENT);
                            dragboard.setDragView(this.snapshot(new SnapshotParameters(), null));
                        }
                    });
                }
                if (getOnDragOver() == null) {
                    setOnDragOver(dragEvent -> {
                        if (dragEvent.getDragboard().getContent(DataFormat.FILES) != null && getItem() != null && getItem().isDirectory()) {
                            if (getItem().listFiles() != null) {
                                List<File> files = (List<File>) dragEvent.getDragboard().getContent(DataFormat.FILES);
                                if (!Arrays.asList(Objects.requireNonNull(getItem().listFiles())).contains(files.get(0))) {
                                    dragEvent.acceptTransferModes(TransferMode.MOVE);
                                    if (!getStyleClass().contains("file-drag-over")) {
                                        getStyleClass().add("file-drag-over");
                                    }
                                }
                            } else {
                                if (!getStyleClass().contains("file-drag-over")) {
                                    getStyleClass().add("file-drag-over");
                                }
                            }
                        }
                    });
                }
                if (getOnDragExited() == null) {
                    setOnDragExited(dragEvent -> getStyleClass().remove("file-drag-over"));
                }
                if (getOnDragDropped() == null) {
                    setOnDragDropped(dragEvent -> {
                        if (dragEvent.getDragboard().getContent(DataFormat.FILES) != null && getItem() != null && getItem().isDirectory()) {
                            File thisFile = getItem();
                            if (dragEvent.getDragboard().getContent(DataFormat.FILES) instanceof List) {
                                List<File> files = (List<File>) dragEvent.getDragboard().getContent(DataFormat.FILES);
                                TreeItem<File> treeItem = getTreeItem();
                                dragEvent.setDropCompleted(true);
                                try {
                                    for (File file : files) {
                                        File newFile = new File(thisFile.getPath() + "\\" + file.getName());
                                        if (!newFile.exists() && file.exists()) {
                                            Files.move(file.toPath(), newFile.toPath());
                                            if (treeItem.isExpanded()) {
                                                treeItem.getChildren().add(new CustomItem(file));
                                            }
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                if (getOnDragDone() == null) {
                    setOnDragDone(dragEvent -> {
                        if (!getItem().exists()) {
                            getTreeItem().getParent().getChildren().remove(this.getTreeItem());
                        }
                    });
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
                    ((CustomItem) getTreeItem()).getComponentTab().getLabel().setText(file.getName());
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
            MenuItem openInFileExplorer = new MenuItem("Open in File Explorer");
            MenuItem run = new MenuItem("Run File");
            MenuItem copyPath = new MenuItem("Copy Path");
            contextMenu.getItems().addAll(newFile, rename, deleteItem, openInFileExplorer, run, copyPath);
            requireIsFolder = newFile;
            run.setOnAction(actionEvent -> {
                if (getItem() != null) {
                    try {
                        Desktop.getDesktop().open(getItem());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            openInFileExplorer.setOnAction(actionEvent -> {
                if (getItem() != null) {
                    try {
                        Runtime.getRuntime().exec("explorer.exe /select," + getItem().getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            rename.setOnAction(actionEvent -> {
                if (this.getTreeView() instanceof FileTreeView) {
                    FileTreeView fileTreeView = (FileTreeView) this.getTreeView();
                    fileTreeView.getIde().showPopupForText("", this.getItem().getName(), gotten -> {
                        if (!gotten.contains("/") && !gotten.contains("\\")) {
                            File renameTo = new File(this.getItem().getParentFile().getPath() + "\\" + gotten);
                            if (!renameTo.exists()) {
                                try {
                                    setGraphic(imageViewFromFile(renameTo));
                                } catch (TranscoderException e) {
                                    e.printStackTrace();
                                }
                                this.getItem().renameTo(renameTo);
                                this.setText(gotten);
                                if (getTreeItem() != null && ((CustomItem) getTreeItem()).getComponentTab() != null) {
                                    ((CustomItem) getTreeItem()).getComponentTab().setTitle(gotten);
                                }
                            }
                        }
                    });
                }
            });
            newFile.setOnAction(actionEvent -> {
                if (this.getTreeView() instanceof FileTreeView) {
                    FileTreeView fileTreeView = (FileTreeView) this.getTreeView();
                    fileTreeView.getIde().showPopupForText("Input the name of the new file.\nLeave no file type if you want to create a folder.", "", gotten -> {
                        if (!gotten.contains("/") && !gotten.contains("\\")) {
                            File createdFile = new File(this.getItem().getPath() + "\\" + gotten);
                            if (!createdFile.exists()) {
                                try {
                                    if (gotten.contains(".")) {
                                        createdFile.createNewFile();
                                    } else {
                                        createdFile.mkdir();
                                    }
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
                        if (getTreeItem().getParent() != null) {
                            getTreeItem().getParent().getChildren().remove(getTreeItem());
                        }
                        if (getTreeItem() != null && ((CustomItem) getTreeItem()).getComponentTab() != null && ((CustomItem) getTreeItem()).getComponentTab().getTabPane() != null) {
//                            ((CustomItem) getTreeItem()).getComponentTab().getFile().delete();
                            ((CustomItem) getTreeItem()).getComponentTab().getTabPane().getTabs().remove(((CustomItem) getTreeItem()).getComponentTab());
                        }
                        DELETE_ON_EXIT.add(getItem());
                        this.getItem().deleteOnExit();
                    });
                }
            });
            copyPath.setOnAction(actionEvent -> {
                if (getItem() != null) {
                    StringSelection stringSelection = new StringSelection(getItem().getAbsolutePath());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            });
            return contextMenu;
        }
        private EventHandler<ContextMenuEvent> defaultContextMenuEvent() {
            return contextMenuEvent -> {
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
        }

        private ImageView imageViewFromFile(File f) throws TranscoderException {
            String name = "";
            if (f.isDirectory()) {
                name = f.getName() + "_folder";
                if (getTreeItem() != null && getTreeItem().isExpanded()) {
                    name = name + "_opened";
                }
            } else if (f.getName().contains(".")) {
                name = f.getName().split("\\.")[1].toLowerCase();
            }
            Image image = IMAGE_CACHE.get(name);
            if (image == null) {
                InputStream resource = FileTreeView.class.getResourceAsStream("icons/" + name + ".svg");
                if (resource == null) {
                    image = IMAGE_CACHE.get(f.isDirectory() ? ("default_folder" + (getTreeItem().isExpanded() ? "_opened" : "")) : "default_file");
                } else {
                    image = imageFromSvg(resource);
                    IMAGE_CACHE.put(name, image);
                }
            }
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);

            return imageView;
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
                    File[] filesArray = file.listFiles();
                    if (filesArray != null) {
                        List<File> files = new LinkedList<>(Arrays.asList(filesArray));
                        files.removeAll(DELETE_ON_EXIT);
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


    public static Image imageFromSvg(InputStream inputStream) throws TranscoderException {
        BufferedImageTranscoder trans = new BufferedImageTranscoder();

        TranscoderInput transIn = new TranscoderInput(inputStream);

        trans.transcode(transIn, null);
        return SwingFXUtils.toFXImage(trans.getBufferedImage(), null);
    }

}
