package tmw.me.com.ide.tools.customtabpane;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import tmw.me.com.jfxhelper.NodeUtils;

import java.util.*;

public class FinalTabPane extends AnchorPane {

    private static final double MIN_TABS_HEIGHT = 50;

    private final Pane tabsBox;
    private final AnchorPane contentPane = new AnchorPane();
    private final Pane allContainer;

    private final ObjectProperty<Insets> tabPadding = new SimpleObjectProperty<>(new Insets(5));

    private List<Tab> lastTabs = Collections.emptyList();
    private final ObservableList<Tab> tabs = FXCollections.observableArrayList();
    private final ObjectProperty<Tab> selectedTab = new SimpleObjectProperty<>();

    public FinalTabPane(Tab... tabs) {
        this(true, tabs);
    }
    public FinalTabPane(boolean horizontal, Tab... tabs) {
        tabsBox = horizontal ? new HBox() : new VBox();

        this.selectedTab.addListener((observable, oldValue, newValue) -> select(newValue, oldValue));
        this.tabs.addListener((ListChangeListener<Tab>) c -> {
            int selectedTabPos = lastTabs.indexOf(getSelectedTab());
            while (c.next()) {
                List<? extends Tab> newList = c.getList();
                for (int i = 0; i < newList.size(); i++) {
                    System.out.println("Inside 2nd loop ( at least )");
                    if ((i >= lastTabs.size() || newList.get(i) != lastTabs.get(i)) && newList.get(i) != null) {
                        setTab(i, newList.get(i));
                    }
                }
            }
            lastTabs = new ArrayList<>(this.tabs);
            if (!this.tabs.contains(selectedTab.get())) {
                int select = selectedTabPos + 1;
                if (this.tabs.size() > select) {
                    setSelectedTab(this.tabs.get(select));
                }
            }
        });
        this.tabs.addAll(tabs);

        allContainer = horizontal ? new VBox() : new HBox();

        tabsBox.setMinHeight(MIN_TABS_HEIGHT);

        tabsBox.getStyleClass().add("tabs-holder");

        allContainer.getChildren().addAll(tabsBox, contentPane);
        getChildren().addAll(allContainer);
        NodeUtils.anchor(allContainer);
        if (horizontal) {
            VBox.setVgrow(contentPane, Priority.SOMETIMES);
        } else {
            HBox.setHgrow(contentPane, Priority.SOMETIMES);
        }

        if (tabs.length > 0) {
            setSelectedTab(tabs[0]);
        }
    }

    private void addTabs(Tab... tabs) {
        addTabs(Arrays.asList(tabs));
    }
    private void addTabs(int pos, Tab... tabs) {
        addTabs(pos, Arrays.asList(tabs));
    }
    private void addTabs(Collection<Tab> tabs) {
        for (Tab tab : tabs) {
            addTab(tab);
        }
    }
    private void addTabs(int pos, List<Tab> tabs) {
        for (int i = 0; i < tabs.size(); i++) {
            addTab(pos + i, tabs.get(i));
        }
    }
    private void addTab(Tab tab) {
        addTab(tabs.size(), tab);
    }
    private void addTab(int pos, Tab tab) {
        tabsBox.getChildren().add(pos, tab);
        tab.setTabPane(this);
    }

    private void removeTabs(Tab... tabs) {
        removeTabs(Arrays.asList(tabs));
    }
    private void removeTabs(Collection<Tab> tabs) {
        for (Tab tab : tabs) {
            removeTabs(tab);
        }
    }
    private void removeTab(Tab tab) {
        tabsBox.getChildren().remove(tab);
        tab.setTabPane(null);
    }

    private void setTab(int pos, Tab tab) {
        if (pos < lastTabs.size() && lastTabs.get(pos) != null) {
            removeTab(lastTabs.get(pos));
        }
        addTab(pos, tab);
    }

    private void select(Tab tab, Tab old) {
        if (tab != old) {
            if (old != null) {
                old.setSelected(false);
                old.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
            }
            tab.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
            tab.setSelected(true);
        }
        contentPane.getChildren().setAll(tab.getContent());
        NodeUtils.anchor(tab.getContent());
    }

    public ObservableList<Tab> getTabs() {
        return tabs;
    }

    public Tab getSelectedTab() {
        return selectedTab.get();
    }
    public void setSelectedTab(Tab tab) {
        selectedTab.set(tab);
    }
    public final ObjectProperty<Tab> selectedTabProperty() {
        return selectedTab;
    }

    public void updateContent() {
        setSelectedTab(getSelectedTab());
    }

    public Pane getTabsBox() {
        return tabsBox;
    }

    public void setTabPadding(Insets insets) {
        tabPaddingProperty().set(insets);
    }
    public Insets getTabPadding() {
        return tabPaddingProperty().get();
    }
    public ObjectProperty<Insets> tabPaddingProperty() {
        return tabPadding;
    }

}
