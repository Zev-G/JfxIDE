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

import java.util.ArrayList;

/**
 * Partly taken from my TransitionMaker project this is an enhanced TabPane aimed at making a more modular interface.
 */
public class ComponentTabPane extends TabPane {

    public static final ArrayList<ComponentTabPane> ALL_TAB_PANES = new ArrayList<>();
    public static final Color BG_COLOR = Color.valueOf("#18202b");
    public static final String STYLE_SHEET = ComponentTabPane.class.getResource("tab.css").toExternalForm();
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
        this.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if (this.getTabs().isEmpty()) {
                if (this.getParent() != null) {
                    lastParent = getParent();
                    if (this.getParent() instanceof Pane) {
                        if (this.getParent().getParent() instanceof SplitPane) {
                            lastLocation = ((SplitPane) this.getParent().getParent()).getItems().indexOf(getParent());
                            System.out.println(((SplitPane) this.getParent().getParent()).getItems());
                            ((SplitPane) this.getParent().getParent()).getItems().remove(this);
                        } else {
                            System.out.println("1");
                            lastLocation = ((Pane) this.getParent()).getChildren().indexOf(this);
                            ((Pane) this.getParent()).getChildren().remove(this);
                        }
                    } else if (this.getParent() instanceof SplitPane) {
                        lastLocation = ((SplitPane) this.getParent()).getItems().indexOf(this);
                        System.out.println(((SplitPane) this.getParent()).getItems());
                        ((SplitPane) this.getParent()).getItems().remove(this);
                    }
                }
            } else {
                if (lastParent instanceof Pane) {
                    ((Pane) lastParent).getChildren().add(Math.min(lastLocation, lastParent.getChildrenUnmodifiable().size()),
                            this);
                } else if (lastParent instanceof SplitPane) {
                    ((SplitPane) lastParent).getItems().add(Math.min(lastLocation, lastParent.getChildrenUnmodifiable().size()), this);
                }
                lastParent = null;
            }
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
}
