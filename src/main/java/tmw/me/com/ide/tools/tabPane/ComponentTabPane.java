package tmw.me.com.ide.tools.tabPane;


import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import tmw.me.com.Resources;

import java.util.ArrayList;

/**
 * Partly taken from my TransitionMaker project this is an enhanced TabPane aimed at making a more modular interface.
 */
public class ComponentTabPane extends TabPane {

    public static final ArrayList<ComponentTabPane> ALL_TAB_PANES = new ArrayList<>();
    public static final Color BG_COLOR = Color.valueOf("#18202b");
    public static final String STYLE_SHEET = Resources.getExternalForm(Resources.EDITOR_STYLES + "tab.css");
    public static final Color DEFAULT_LABEL_COLOR = Color.WHITE;

    private Parent lastParent;
    private int lastLocation;

    private SplitPane horizontal;
    private SplitPane vertical;

    private EventHandler<Event> eventEventHandler;

    public ComponentTabPane(ComponentTab<?>... componentTabs) {
        super(componentTabs);
        init();
    }

    private void init() {
        this.getStyleClass().add("component-tab-pane");
        ALL_TAB_PANES.add(this);
        this.setTabDragPolicy(TabDragPolicy.REORDER);

        this.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        this.setBackground(new Background(new BackgroundFill(BG_COLOR.darker(), CornerRadii.EMPTY, Insets.EMPTY)));

        this.getStylesheets().add(STYLE_SHEET);

        setOnDragOver(dragEvent -> {
            System.out.println('a');
        });
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

    public SplitPane getHorizontal() {
        return horizontal;
    }

    public SplitPane getVertical() {
        return vertical;
    }

    public void setHorizontal(SplitPane horizontal) {
        this.horizontal = horizontal;
    }

    public void setVertical(SplitPane vertical) {
        this.vertical = vertical;
    }

    public static void disappearWithoutChildren(ComponentTabPane tabPane) {
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if (tabPane.getTabs().isEmpty()) {
                if (tabPane.getParent() != null) {
                    tabPane.lastParent = tabPane.getParent();
                    if (tabPane.getParent() instanceof Pane) {
                        if (tabPane.getParent().getParent() instanceof SplitPane) {
                            tabPane.lastLocation = ((SplitPane) tabPane.getParent().getParent()).getItems().indexOf(tabPane.getParent());
                            ((SplitPane) tabPane.getParent().getParent()).getItems().remove(tabPane);
                        } else {
                            tabPane.lastLocation = ((Pane) tabPane.getParent()).getChildren().indexOf(tabPane);
                            ((Pane) tabPane.getParent()).getChildren().remove(tabPane);
                        }
                    } else if (tabPane.getParent() instanceof SplitPane) {
                        tabPane.lastLocation = ((SplitPane) tabPane.getParent()).getItems().indexOf(tabPane);
                        ((SplitPane) tabPane.getParent()).getItems().remove(tabPane);
                    }
                }
            } else {
                if (tabPane.lastParent instanceof Pane) {
                    // TODO fix possible IndexOutOfBoundsException here
                    ((Pane) tabPane.lastParent).getChildren().add(Math.min(tabPane.lastLocation, tabPane.lastParent.getChildrenUnmodifiable().size()),
                            tabPane);
                } else if (tabPane.lastParent instanceof SplitPane) {
                    ((SplitPane) tabPane.lastParent).getItems().add(Math.min(tabPane.lastLocation, tabPane.lastParent.getChildrenUnmodifiable().size()), tabPane);
                }
                tabPane.lastParent = null;
            }
        });
    }

}
