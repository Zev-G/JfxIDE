package tmw.me.com.ide.tools;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ComponentTabPane extends TabPane {

    private static final ArrayList<ComponentTabPane> ALL_TAB_PANES = new ArrayList<>();

    public static final Color BG_COLOR = Color.valueOf("#18202b");
    public static final String STYLE_SHEET = ComponentTabPane.class.getResource("tab.css").toExternalForm();

    public static final Color DEFAULT_LABEL_COLOR = Color.WHITE;

    private EventHandler<Event> eventEventHandler;

    public ComponentTabPane() {
        super();
        init();
    }
//    public ComponentTabPane(Tab... tabs) {
//        super(tabs);
//        init();
//    }

    private void init() {
        this.getStyleClass().add("component-tab-pane");
        ALL_TAB_PANES.add(this);
        this.setTabDragPolicy(TabDragPolicy.REORDER);

        this.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        this.setBackground(new Background(new BackgroundFill(BG_COLOR.darker(), CornerRadii.EMPTY, Insets.EMPTY)));

        this.getStylesheets().add(STYLE_SHEET);

    }

    public EventHandler<Event> getOnTabCloseRequested() {
        return eventEventHandler;
    }

    public void setOnTabCloseRequested(EventHandler<Event> eventHandler) {
        this.eventEventHandler = eventHandler;
    }

    public ComponentTab<?> getSelectedTab() {
        return (ComponentTab<?>) getSelectionModel().getSelectedItem();
    }

    public static class ComponentTab<T extends Node> extends Tab {

        private final Label label = new Label();
        private final ComponentTabPane individualTabPane = new ComponentTabPane();
        private final Stage stage = new Stage(StageStyle.UNDECORATED);

        private TabPane ogTabPane;

        private double individualTabPaneStartDragX = 0;
        private double individualTabPaneStartDragY = 0;
        private final Popup pictureStage = new Popup();
        private final ImageView imageView = new ImageView();
        private final AnchorPane pictureAnchorPane = new AnchorPane(imageView);
        private ComponentTabPane lastTabPane;
        private final T value;

        private File file;
        private Node mainNode;

        public ComponentTab(String s, T node) {
            super("");
            if (node instanceof IntegratedTextEditor) {
                this.setContent(new VirtualizedScrollPane<>((IntegratedTextEditor) node));
            }
            value = node;
            label.setText(s);
            label.setContextMenu(makeContextMenu());
            init();
        }

        private void init() {
            this.getStyleClass().add("component-tab");
            this.setGraphic(label);
            label.setTextFill(DEFAULT_LABEL_COLOR);
            makeDraggable();
            this.setOnClosed(event -> {
                if (stage.isShowing()) stage.hide();
                ALL_TAB_PANES.remove(individualTabPane);
            });
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
            this.contentProperty().addListener((observableValue, node, t1) -> {
                if (mainNode == null && t1 != null) {
                    mainNode = t1;
                }
            });
        }

        private ContextMenu makeContextMenu() {
            MenuItem save = new MenuItem("Save");
            MenuItem close = new MenuItem("Close");
            MenuItem openInNewWindow = new MenuItem("Open in New Window");

            save.setOnAction(actionEvent -> {
                if (file != null && getValue() != null && getValue() instanceof IntegratedTextEditor) {
                    String text = ((IntegratedTextEditor) getValue()).getText();
                    try {
                        FileWriter fileWriter = new FileWriter(file);
                        fileWriter.write(text);
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            close.setOnAction(actionEvent -> {
                if (getTabPane() != null) {
                    getTabPane().getTabs().remove(this);
                }
            });
            openInNewWindow.setOnAction(actionEvent -> {
                if (getValue() instanceof IntegratedTextEditor && getTabPane() != null) {
                    Ide ide = new Ide();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(ide));
                    stage.show();
                    stage.setHeight(500);
                    stage.setWidth(800);
                    if (file != null) {
                        ide.loadFile(getFile());
                    } else {
                        String text = ((IntegratedTextEditor) getValue()).getText();
                        IntegratedTextEditor newIntegratedTextEditor = new IntegratedTextEditor();
                        newIntegratedTextEditor.replaceText(text);
                        ComponentTab<IntegratedTextEditor> dupedTab = new ComponentTab<>(label.getText(), newIntegratedTextEditor);
                        ide.getTabPane().getTabs().add(dupedTab);
                    }
                }
            });

            return new ContextMenu(save, close, openInNewWindow);
        }

        public void makeDraggable() {
            // Initialisation
            AnchorPane padding = new AnchorPane(individualTabPane);
            double pad = 5;
            padding.setBackground(new Background(new BackgroundFill(BG_COLOR.darker(), CornerRadii.EMPTY, Insets.EMPTY)));
            AnchorPane.setLeftAnchor(individualTabPane, pad); AnchorPane.setRightAnchor(individualTabPane, pad);
            AnchorPane.setTopAnchor(individualTabPane, pad); AnchorPane.setBottomAnchor(individualTabPane, pad);
            Scene s = new Scene(padding);
            s.setFill(Color.TRANSPARENT);
            stage.setScene(s);
            pictureStage.getContent().add(pictureAnchorPane);
            imageView.setPreserveRatio(true);
            pictureAnchorPane.setMaxHeight(100);
            pictureAnchorPane.setMaxWidth(300);
            pictureAnchorPane.setOpacity(0.5);
            pictureAnchorPane.setMouseTransparent(true);
            AnchorPane.setTopAnchor(imageView, 0D); AnchorPane.setBottomAnchor(imageView, 0D);
            AnchorPane.setRightAnchor(imageView, 0D); AnchorPane.setLeftAnchor(imageView, 0D);

            // Buttons
            SVGPath closeShape = new SVGPath(); closeShape.setContent("M 4 2 L 2 4 L 21 24 L 23 22 L 4 2 M 23 4 L 21 2 L 2 22 L 4 24 L 23 4");
            closeShape.setFill(Color.GRAY);
            Button closeButton = new Button("", closeShape);
            closeButton.setCursor(Cursor.HAND);
            Background blankBg = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
            Background redBg = new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
            closeButton.setBackground(blankBg);
            closeButton.setOnMouseEntered(mouseEvent -> { closeButton.setBackground(redBg); closeShape.setFill(Color.WHITE); });
            closeButton.setOnMouseExited(mouseEvent -> { closeButton.setBackground(blankBg); closeShape.setFill(Color.GRAY); });
            closeButton.setOnAction(actionEvent -> this.stage.hide());
            padding.getChildren().add(closeButton);
            AnchorPane.setRightAnchor(closeButton, 3D); AnchorPane.setTopAnchor(closeButton, 3D);

            // Event Listeners
            FXResizeHelper.addResizeListener(this.stage);
            individualTabPane.setOnMousePressed(mouseEvent -> {
                if (mouseEvent.getPickResult().getIntersectedNode().getStyleClass().contains("tab-header-background")) {
                    individualTabPaneStartDragX = mouseEvent.getScreenX() - stage.getX();
                    individualTabPaneStartDragY = mouseEvent.getScreenY() - stage.getY();
                    if (mouseEvent.getClickCount() > 1) {
                        stage.setMaximized(!stage.isMaximized());
                    }
                } else {
                    individualTabPaneStartDragX = Integer.MAX_VALUE;
                    individualTabPaneStartDragY = Integer.MAX_VALUE;
                }
            });
            individualTabPane.setOnMouseDragged(mouseEvent -> {
                if (individualTabPaneStartDragY != Integer.MAX_VALUE && !stage.isMaximized()) {
                    stage.setX(mouseEvent.getScreenX() - individualTabPaneStartDragX);
                    stage.setY(mouseEvent.getScreenY() - individualTabPaneStartDragY);
                }
            });
            this.label.setOnMousePressed(mouseEvent -> {
            });
            this.label.setOnMouseDragged(mouseEvent -> {
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
            });
            this.label.setOnMouseReleased(mouseEvent -> {
                if (mouseEvent.getButton() != MouseButton.MIDDLE) {
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

        private ComponentTabPane getTopTabPane(MouseEvent mouseEvent) {
            ComponentTabPane pane = null;
            int size = ALL_TAB_PANES.size();
            for (int i = 0; i < size; i++) {
                if (ALL_TAB_PANES.size() - 1 >= i) {
                    ComponentTabPane tabPane = ALL_TAB_PANES.get(i);
                    if (tabPane.getScene() != null && tabPane.getScene().getWindow().isShowing()) {
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
            stage.setHeight(this.getContent().getBoundsInLocal().getHeight());
            stage.setWidth(this.getContent().getBoundsInLocal().getWidth());
            individualTabPane.getTabs().add(this);
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
            return file;
        }
        public void setFile(File file) {
            this.file = file;
        }
    }

}
