package tmw.me.com.ide.tools.tabPane;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.tools.builders.tooltip.ToolTipBuilder;

import java.io.File;
import java.util.ArrayList;

/**
 * The base for this was taken from my Transition Maker project but most of this was written for this project.
 */
public class ComponentTab<T extends Node & ComponentTabContent<T>> extends Tab {

    private static final String DEFAULT_TEXT = "Untitled";
    private static final double MIN_WIDTH = 150;
    private static final double IMAGE_OPACITY = 0.7;
    private static final double IMAGE_RATIO = 0.3;

    private final Label label = new Label();

    private Ide ide;
    private Stage stage;
    private TabPane ogTabPane;
    private Popup pictureStage;
    private ImageView imageView;
    private AnchorPane pictureAnchorPane;
    private ComponentTabPane lastTabPane;

    private final T value;

    private final SimpleObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
    private Node mainNode;

    private final SplitPane horizontal = new SplitPane();
    private final SplitPane vertical = new SplitPane(horizontal);

    private boolean dragInitialized = false;

    public ComponentTab(String s, T node) {
        super("");
        this.setContent(vertical);
        horizontal.getItems().add(node.getMainNode());
        value = node;
        label.setText(s);
        label.getStyleClass().add("tab-title");
        label.setContextMenu(makeContextMenu());
        ToolTipBuilder toolTipBuilder = new ToolTipBuilder();
        toolTipBuilder.headerProperty().bind(label.textProperty());
        fileProperty.addListener((observableValue, file, t1) -> {
            if (t1 != null) toolTipBuilder.setMainText(t1.getAbsolutePath());
            else toolTipBuilder.setMainText("");
        });
        label.setTooltip(toolTipBuilder.build());
        init();
        horizontal.getStyleClass().add("dark-split-pane");
        vertical.getStyleClass().add("dark-split-pane");
    }

    private void init() {
        this.getStyleClass().add("component-tab");
        this.setGraphic(label);
        label.setTextFill(ComponentTabPane.DEFAULT_LABEL_COLOR);
        makeDraggable();
        this.tabPaneProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends TabPane> observableValue, TabPane tabPane, TabPane t1) {
                ogTabPane = t1;
                if (t1 instanceof ComponentTabPane) {
                    setOnCloseRequest(((ComponentTabPane) t1).getOnTabCloseRequested());
                }
                tabPaneProperty().removeListener(this);
            }
        });
    }

    private ContextMenu makeContextMenu() {
        MenuItem save = new MenuItem("Save");
        MenuItem close = new MenuItem("Close");
        MenuItem openInNewWindow = new MenuItem("Open in New Window");
        MenuItem splitHorizontally = new MenuItem("Split Horizontally");

        if (value.canSplitHorizontally()) {
            splitHorizontally.setOnAction(actionEvent -> {
                ObservableList<Node> items = getTabPaneCTP().getHorizontal().getItems();
                int ourIndex = items.indexOf(getTabPaneCTP());
                ComponentTabPane componentTabPane = new ComponentTabPane(new ComponentTab<>(label.getText(), value.createNewCopy()));
                ComponentTabPane.disappearWithoutChildren(componentTabPane);
                componentTabPane.setVertical(getTabPaneCTP().getVertical());
                componentTabPane.setHorizontal(getTabPaneCTP().getHorizontal());
                items.add(ourIndex + 1, componentTabPane);
            });
        }

        save.setOnAction(actionEvent -> {
            if (fileProperty.get() != null && getValue() != null) {
                value.save(fileProperty.get());
            }
        });
        close.setOnAction(actionEvent -> {
            if (getTabPane() != null) {
                getTabPane().getTabs().remove(this);
            }
        });
        openInNewWindow.setOnAction(actionEvent -> {
            if (getTabPane() != null) {
                Ide ide = new Ide();
                Stage stage = new Stage();
                stage.setScene(new Scene(ide));
                stage.show();
                stage.setHeight(500);
                stage.setWidth(800);
                if (fileProperty.get() != null) {
                    ide.loadFile(getFile());
                } else {
                    T newNode = value.createNewCopy();
                    ComponentTab<T> dupedTab = new ComponentTab<>(label.getText(), newNode);
                    ide.getTabPane().getTabs().add(dupedTab);
                }
            }
        });
        SeparatorMenuItem separator1 = new SeparatorMenuItem();
        separator1.getStyleClass().add("darkish-separator-item");
        ContextMenu contextMenu = new ContextMenu(save, close, openInNewWindow, separator1);
        if (value.canSplitHorizontally()) {
            contextMenu.getItems().add(splitHorizontally);
        }
        contextMenu.getItems().addAll(value.addContext());
        return contextMenu;
    }

    public void makeDraggable() {

        // Event Listeners
        this.label.setOnDragDetected(mouseEvent -> {
            if (!dragInitialized) {
                stage = new Stage();
                pictureStage = new Popup();
                imageView = new ImageView();
                pictureAnchorPane = new AnchorPane(imageView);
                pictureStage.setAutoFix(false);
                Scene s = new Scene(getIde());
                s.setFill(Color.TRANSPARENT);
                stage.setScene(s);
                pictureStage.getContent().add(pictureAnchorPane);
                imageView.setPreserveRatio(true);
                pictureAnchorPane.setMaxHeight(100);
                pictureAnchorPane.setMaxWidth(300);
                pictureAnchorPane.setOpacity(0.5);
                pictureAnchorPane.setMouseTransparent(true);
                AnchorPane.setTopAnchor(imageView, 0D);
                AnchorPane.setBottomAnchor(imageView, 0D);
                AnchorPane.setRightAnchor(imageView, 0D);
                AnchorPane.setLeftAnchor(imageView, 0D);

                // Buttons
                SVGPath closeShape = new SVGPath();
                closeShape.setContent("M 4 2 L 2 4 L 21 24 L 23 22 L 4 2 M 23 4 L 21 2 L 2 22 L 4 24 L 23 4");
                closeShape.setFill(Color.GRAY);
                Button closeButton = new Button("", closeShape);
                closeButton.setCursor(Cursor.HAND);
                Background blankBg = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
                Background redBg = new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
                closeButton.setBackground(blankBg);
                closeButton.setOnMouseEntered(mouseEvent1 -> {
                    closeButton.setBackground(redBg);
                    closeShape.setFill(Color.WHITE);
                });
                closeButton.setOnMouseExited(mouseEvent1 -> {
                    closeButton.setBackground(blankBg);
                    closeShape.setFill(Color.GRAY);
                });
                closeButton.setOnAction(actionEvent -> this.stage.hide());
                AnchorPane.setRightAnchor(closeButton, 3D);
                AnchorPane.setTopAnchor(closeButton, 3D);
                dragInitialized = true;
            }
        });
        this.label.setOnMouseDragged(mouseEvent -> {
            if (dragInitialized) {
                Bounds bounds = label.localToScene(label.getBoundsInLocal());
                if (mouseEvent.getSceneY() > bounds.getMaxY() || mouseEvent.getSceneY() < bounds.getMinY() || pictureStage.isShowing()) {
                    SnapshotParameters snapshotParameters = new SnapshotParameters();
                    snapshotParameters.setFill(Color.TRANSPARENT);
                    if (!pictureStage.isShowing()) {
                        Image image = this.getContent().snapshot(snapshotParameters, null);
                        this.imageView.setImage(image);
                        imageView.setFitWidth(image.getWidth() / 2.3);
                        imageView.setFitHeight(image.getHeight() / 2.3);
                        pictureStage.show(this.getContent().getScene().getWindow());
                        pictureStage.setWidth(image.getWidth());
                        pictureStage.setHeight(image.getHeight());
                        this.getContent().setOpacity(0.1);
                        label.getParent().getParent().getParent().setOpacity(0.1);
                    }
                    pictureStage.setX(mouseEvent.getX() + this.getContent().getScene().getWindow().getX() + bounds.getMinX() - 30);
                    pictureStage.setY(mouseEvent.getY() + this.getContent().getScene().getWindow().getY() + bounds.getMinY() + 30);
                    ComponentTabPane pane = getTopTabPane(mouseEvent);
                    if (pane != null) {
                        pane.setEffect(new Glow(0.8));
                        lastTabPane = pane;
                    }
                    if (lastTabPane != null && lastTabPane != pane) {
                        lastTabPane.setEffect(null);
                    }
                }
            }
        });
        this.label.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.MIDDLE) {
                if (pictureStage != null)
                    pictureStage.hide();
                this.getContent().setOpacity(1);
                label.getParent().getParent().getParent().setOpacity(1);
                ComponentTabPane pane = getTopTabPane(mouseEvent);
                if (pane != this.getTabPane()) {
                    if (pane == null) {
                        attachToStage();
                        this.stage.setX(pictureStage.getX());
                        this.stage.setY(pictureStage.getY());
                    } else {
                        pane.setEffect(null);
                        if (this.getTabPane() != null) {
                            this.getTabPane().setEffect(null);
                            this.getTabPane().getTabs().remove(this);
                        }
                        stage.setOnHidden(null);
                        stage.hide();
                        pane.getTabs().add(this);
                        pane.getSelectionModel().select(this);
                    }
                }
                if (lastTabPane != null) {
                    lastTabPane.setEffect(null);
                }
            }
        });
    }

    public void setMainNode(Node mainNode) {
        this.mainNode = mainNode;
    }

    public Node getMainNode() {
        return mainNode;
    }

    private ComponentTabPane getTabPaneCTP() {
        return (ComponentTabPane) getTabPane();
    }

    private static ComponentTabPane getTopTabPane(MouseEvent mouseEvent) {
        ComponentTabPane pane = null;
        int size = ComponentTabPane.ALL_TAB_PANES.size();
        for (int i = 0; i < size; i++) {
            if (ComponentTabPane.ALL_TAB_PANES.size() - 1 >= i) {
                ComponentTabPane tabPane = ComponentTabPane.ALL_TAB_PANES.get(i);
                if (tabPane != null && tabPane.getParent() != null && tabPane.getScene() != null && tabPane.getScene().getWindow() != null && tabPane.getScene().getWindow().isShowing()) {
                    Bounds tabPaneBounds = tabPane.localToScene(tabPane.getBoundsInLocal());
                    Window window = tabPane.getScene().getWindow();
                    if (mouseEvent.getScreenX() > tabPaneBounds.getMinX() + window.getX() && mouseEvent.getScreenX() < tabPaneBounds.getMaxX() + window.getX()) {
                        if (mouseEvent.getScreenY() > tabPaneBounds.getMinY() + window.getY() && mouseEvent.getScreenY() < tabPaneBounds.getMaxY() + window.getY()) {
                            pane = tabPane;
                            break;
                        }
                    }
                }
            }
        }
        return pane;
    }

    public void attachToStage() {
        stage.setHeight(this.getContent().getScene().getWindow().getHeight());
        stage.setWidth(this.getContent().getScene().getWindow().getWidth());
        if (this.getTabPane() != null) {
            this.getTabPane().getTabs().remove(this);
        }
        stage.show();
        stage.setOnHidden(windowEvent -> {
            ArrayList<Tab> tabs = new ArrayList<>(this.getTabPane().getTabs());
            for (Tab tab : tabs) {
                if (tab instanceof ComponentTab) {
                    ((ComponentTab<?>) tab).stageHidden();
                }
            }
        });
        getIde().getTabPane().getTabs().add(this);
    }

    public Label getLabel() {
        return label;
    }

    public void setTitle(String text) {
        this.label.setText(text);
    }

    public void stageHidden() {
        if (this.getTabPane() != null) {
            this.getTabPane().getTabs().remove(this);
        }
        if (ogTabPane != null) {
            ogTabPane.getTabs().add(this);
            ogTabPane.getSelectionModel().select(this);
        }
    }

    public T getValue() {
        return value;
    }

    public File getFile() {
        return fileProperty.get();
    }

    public void setFile(File file) {
        fileProperty.set(file);
    }

    public ObjectProperty<File> fileProperty() {
        return fileProperty;
    }

    public Ide getIde() {
        if (ide == null) {
            ide = new Ide();
        }
        return ide;
    }
}