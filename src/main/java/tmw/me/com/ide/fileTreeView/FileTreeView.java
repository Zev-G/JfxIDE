package tmw.me.com.ide.fileTreeView;

import com.jfoenix.controls.JFXTreeCell;
import com.jfoenix.controls.JFXTreeView;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import tmw.me.com.Resources;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.languages.addon.LanguageAddon;
import tmw.me.com.ide.codeEditor.languages.addon.ui.AddonEditor;
import tmw.me.com.ide.images.Images;
import tmw.me.com.ide.settings.IdeSettings;
import tmw.me.com.ide.tools.tabPane.ComponentTab;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is the File Tree View used to navigate and edit files in the IDE.
 */
public class FileTreeView extends JFXTreeView<File> {

    private static final String ICONS_PATH = Resources.EDITOR + "svgs/icons/";

    private static final Image ADDON_IMAGE = new Image(Images.get("addon.png"));

    public static final HashMap<String, Image> IMAGE_CACHE = new HashMap<>();
    private static final ArrayList<File> DELETE_ON_EXIT = new ArrayList<>();

    static {
        try {
            IMAGE_CACHE.put("default_file", imageFromSvg(Resources.getAsStream(ICONS_PATH + "default_file.svg")));
            IMAGE_CACHE.put("default_folder", imageFromSvg(Resources.getAsStream(ICONS_PATH + "default_folder.svg")));
            IMAGE_CACHE.put("default_folder_opened", imageFromSvg(Resources.getAsStream(ICONS_PATH + "default_folder_opened.svg")));
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
    }

    public static Image getFolderImage() {
        return getFolderImage(false);
    }
    public static Image getFolderImage(boolean opened) {
        return IMAGE_CACHE.get(opened ? "default_folder_opened" : "default_folder");
    }
    public static Image getFileImage() {
        return IMAGE_CACHE.get("default_file");
    }
    public static Optional<Image> getImage(String name) {
        return Optional.ofNullable(IMAGE_CACHE.get(name));
    }

    private final File fileRoot;
    private final Ide ide;

    private final HashMap<File, CustomItem> fileItemMap = new HashMap<>();
    private final HashMap<File, CustomCell> fileCellMap = new HashMap<>();

    private final ArrayList<Function<File, Consumer<CustomItem>>> customFunctions = new ArrayList<>();
    private final ArrayList<Function<File, ImageView>> customImages = new ArrayList<>();
    private final BiFunction<File, Boolean, ImageView> imageFactory = (file, bool) -> {
        for (Function<File, ImageView> val : customImages) {
            ImageView result = val.apply(file);
            if (result != null) {
                result.setFitHeight(20);
                result.setPreserveRatio(true);
                return result;
            }
        }
        try {
            ImageView result = CustomCell.imageViewFromFile(file, bool);
            result.setFitHeight(20);
            result.setPreserveRatio(true);
            return result;
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        return null;
    };

    /**
     * @param fileRoot The root file, should be a folder.
     * @param ide      The IDE that owns this File Tree View.
     */
    public FileTreeView(File fileRoot, Ide ide) {
        customImages.add(file -> {
            if (LanguageAddon.verifyDir(file))
                return new ImageView(ADDON_IMAGE);
            return null;
        });
        customFunctions.add(file -> {
            if (LanguageAddon.verifyDir(file))
                return item -> {
                    if (ide != null) {
                        AddonEditor editor = new AddonEditor(file);
                        ComponentTab<AddonEditor> tab = new ComponentTab<>(file.getName(), editor);
                        ide.getTabPane().getTabs().add(tab);
                        ide.getTabPane().getSelectionModel().select(tab);
                        item.setComponentTab(tab);
                        if (!IdeSettings.getAddonJSON().addonPaths.contains(file.getAbsolutePath())) {
                            ide.showConfirmation("Would you like to add this addon to the Ide?", aBoolean -> {
                                if (aBoolean) {
                                    IdeSettings.ADDON_PATHS.add(file.getAbsolutePath());
                                }
                            });
                        }
                    }
                };
            return null;
        });

        this.fileRoot = fileRoot;
        this.ide = ide;
        this.setRoot(new CustomItem(fileRoot, this));
        getRoot().setExpanded(true);
        setCellFactory(fileTreeView -> new CustomCell());
    }

    public File getFileRoot() {
        return fileRoot;
    }

    public Ide getIde() {
        return ide;
    }

    public HashMap<File, CustomItem> getFileItemMap() {
        return fileItemMap;
    }

    public HashMap<File, CustomCell> getFileCellMap() {
        return fileCellMap;
    }

    public ArrayList<Function<File, ImageView>> getCustomImagesList() {
        return customImages;
    }

    public ArrayList<Function<File, Consumer<CustomItem>>> getCustomFunctions() {
        return customFunctions;
    }

    /**
     * This is the cell used by {@link FileTreeView}, it implements the ContextMenu, Graphic, Opening, and Dragging functionalities.
     */
    public static class CustomCell extends JFXTreeCell<File> {

        private ContextMenu contextMenu;
        private MenuItem requireIsFolder;
        private EventHandler<ContextMenuEvent> contextMenuEventEventHandler;
        private ChangeListener<Boolean> expandedChangeListener;

        public void pressed(CustomItem item) {
            if (item.getComponentTab() != null) {
                if (item.getComponentTab().getTabPane() != null) {
                    item.getComponentTab().getTabPane().getSelectionModel().select(item.getComponentTab());
                } else if (getTreeView() != null && getTreeView() instanceof FileTreeView) {
                    TabPane tabPane = ((FileTreeView) getTreeView()).getIde().getTabPane();
                    if (!tabPane.getTabs().contains(item.getComponentTab())) {
                        tabPane.getTabs().add(item.getComponentTab());
                    }
                    tabPane.getSelectionModel().select(item.getComponentTab());
                }
            } else if (getTreeView() instanceof FileTreeView && item.getValue() != null) {
                for (Function<File, Consumer<CustomItem>> customFunction : ((FileTreeView) getTreeView()).getCustomFunctions()) {
                    Consumer<CustomItem> result = customFunction.apply(item.getValue());
                    if (result != null) {
                        result.accept(item);
                        return;
                    }
                }
                // Didn't return
                File value = getTreeItem().getValue();
                if (value != null && !value.isDirectory()) {
                    item.setComponentTab(Ide.getNewEditorTab(value));
                    TabPane tabPane = ((FileTreeView) getTreeView()).getIde().getTabPane();
                    if (!tabPane.getTabs().contains(item.getComponentTab())) {
                        tabPane.getTabs().add(item.getComponentTab());
                    }
                    tabPane.getSelectionModel().select(item.getComponentTab());
                }
            }
        }

        public CustomCell() {
            itemProperty().addListener((observableValue, treeItem, t1) -> {
                if (getTreeView() != null && getTreeView() instanceof FileTreeView) {
                    ((FileTreeView) getTreeView()).getFileItemMap().remove(treeItem);
                    ((FileTreeView) getTreeView()).getFileItemMap().put(t1, (CustomItem) this.getTreeItem());
                    ((FileTreeView) getTreeView()).getFileCellMap().put(t1, this);
                }
                if (t1 != null && t1.isDirectory() && getTreeItem() != null) {
                    if (expandedChangeListener != null) {
                        getTreeItem().expandedProperty().removeListener(expandedChangeListener);
                    } else {
                        expandedChangeListener = (observableValue1, aBoolean, t11) -> setGraphic(((FileTreeView) getTreeView()).imageFactory.apply(t1, getTreeItem() == null || !getTreeItem().isExpanded()));
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
                            if (getTreeItem() != null)
                                pressed(item);
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
                                                treeItem.getChildren().add(new CustomItem(file, (FileTreeView) getTreeView()));
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
                setGraphic(((FileTreeView) getTreeView()).imageFactory.apply(file, getTreeItem() == null || getTreeItem().isExpanded()));
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
                    fileTreeView.getIde().showPopupForText("Rename the file to:", this.getItem().getName(), gotten -> {
                        if (!gotten.contains("/") && !gotten.contains("\\")) {
                            File renameTo = new File(this.getItem().getParentFile().getPath() + "\\" + gotten);
                            if (!renameTo.exists()) {
                                setGraphic(((FileTreeView) getTreeView()).imageFactory.apply(renameTo, getTreeItem() == null || getTreeItem().isExpanded()));
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
                            CustomItem customItem = new CustomItem(createdFile, (FileTreeView) getTreeView());
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
                    } else {
                        requireIsFolder.setDisable(!customCell.getItem().isDirectory());
                    }
                } else {
                    contextMenuEvent.consume();
                }
            };
        }

        /**
         * Gets an ImageView from a file, this also caches the image so that it doesn't need to be processed more than once.
         */
        private static ImageView imageViewFromFile(File f, boolean expanded) throws TranscoderException {
            String name = "";
            if (f.isDirectory()) {
                name = f.getName() + "_folder";
                if (expanded) {
                    name = name + "_opened";
                }
            } else if (f.getName().contains(".")) {
                name = f.getName().split("\\.")[1].toLowerCase();
            }
            Image image = IMAGE_CACHE.get(name);
            if (image == null) {
                InputStream resource = Resources.getAsStream(ICONS_PATH + name + ".svg");
                if (resource == null) {
                    image = IMAGE_CACHE.get(f.isDirectory() ? ("default_folder" + (expanded ? "_opened" : "")) : "default_file");
                } else {
                    image = imageFromSvg(resource);
                    IMAGE_CACHE.put(name, image);
                }
            }

            return new ImageView(image);
        }
    }

    /**
     * This is the item class used by {@link FileTreeView}, this item implements the file listing functionality.
     */
    public static class CustomItem extends TreeItem<File> {

        private ComponentTab<?> componentTab;

        public CustomItem() {
            super();
        }

        public CustomItem(File file, FileTreeView treeView) {
            super(file);
            File[] listFiles = file.listFiles();
            boolean customFunction = false;
            for (Function<File, Consumer<CustomItem>> function : treeView.getCustomFunctions()) {
                if (function.apply(file) != null) {
                    customFunction = true;
                    break;
                }
            }
            if (!customFunction && file.isDirectory() && listFiles != null) {
                this.getChildren().add(new CustomItem());
            }
            boolean finalCustomFunction = customFunction;
            this.expandedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1 && !finalCustomFunction) {
                    File[] filesArray = file.listFiles();
                    if (filesArray != null) {
                        List<File> files = new LinkedList<>(Arrays.asList(filesArray));
                        files.removeAll(DELETE_ON_EXIT);
                        files.sort((o1, o2) -> {
                            int value = o1.compareTo(o2);
                            if (o1.isDirectory()) {
                                value -= 10000;
                            }
                            if (o2.isDirectory()) {
                                value += 10000;
                            }
                            return value;
                        });
                        ArrayList<TreeItem<File>> newTreeItems = new ArrayList<>();
                        for (File loopFile : files) {
                            newTreeItems.add(new CustomItem(loopFile, treeView));
                        }
                        this.getChildren().setAll(newTreeItems);
                        if (newTreeItems.size() == 1 && !newTreeItems.get(0).getChildren().isEmpty()) {
                            newTreeItems.get(0).setExpanded(true);
                        }
                    }
                }
            });
        }

        public void setComponentTab(ComponentTab<?> componentTab) {
            this.componentTab = componentTab;
        }

        public ComponentTab<?> getComponentTab() {
            return componentTab;
        }

    }


    /**
     * @param inputStream The SVG's input stream.
     * @return A javafx image from the svg.
     * @throws TranscoderException Throws from {@link BufferedImageTranscoder#transcode(TranscoderInput, TranscoderOutput)}
     */
    public static Image imageFromSvg(InputStream inputStream) throws TranscoderException {
        BufferedImageTranscoder trans = new BufferedImageTranscoder();

        TranscoderInput transIn = new TranscoderInput(inputStream);

        trans.transcode(transIn, null);
        return SwingFXUtils.toFXImage(trans.getBufferedImage(), null);
    }

}
