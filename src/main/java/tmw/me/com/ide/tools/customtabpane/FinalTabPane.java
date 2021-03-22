package tmw.me.com.ide.tools.customtabpane;

import javafx.css.PseudoClass;
import javafx.scene.layout.*;
import tmw.me.com.ide.tools.NodeUtils;

public class FinalTabPane extends AnchorPane {

    private static final double MIN_TABS_HEIGHT = 50;

    private final Pane tabsBox;
    private final AnchorPane contentPane = new AnchorPane();
    private final Pane allContainer;

    private final Tab[] tabs;
    private Tab selectedTab;

    public FinalTabPane(Tab... tabs) {
        this(true, tabs);
    }
    public FinalTabPane(boolean horizontal, Tab... tabs) {
        this.tabs = tabs;

        tabsBox = horizontal ? new HBox() : new VBox();
        allContainer = horizontal ? new VBox() : new HBox();

        tabsBox.getChildren().addAll(tabs);
        for (Tab tab : tabs) {
            tab.setMinHeight(MIN_TABS_HEIGHT);
            tab.setTabPane(this);
        }
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
            select(tabs[0]);
        }
    }

    public void select(Tab tab) {
        if (tab != selectedTab) {
            if (selectedTab != null) {
                selectedTab.setSelected(false);
                selectedTab.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
            }
            tab.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
            tab.setSelected(true);
        }
        selectedTab = tab;
        contentPane.getChildren().setAll(tab.getContent());
        NodeUtils.anchor(tab.getContent());
    }

    public Tab[] getTabs() {
        return tabs;
    }

    public Tab getSelectedTab() {
        return selectedTab;
    }

    public void updateContent() {
        select(getSelectedTab());
    }

    public Pane getTabsBox() {
        return tabsBox;
    }
}
