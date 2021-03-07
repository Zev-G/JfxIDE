package tmw.me.com.ide.codeEditor.languages.addon.ui;

import javafx.css.PseudoClass;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import tmw.me.com.ide.tools.NodeUtils;

public class FinalTabPane extends AnchorPane {

    private static final double MIN_TABS_HEIGHT = 50;

    private final HBox tabsHBox = new HBox();
    private final AnchorPane contentPane = new AnchorPane();

    private final Tab[] tabs;
    private Tab selectedTab;

    public FinalTabPane(Tab... tabs) {
        this.tabs = tabs;

        tabsHBox.getChildren().addAll(tabs);
        for (Tab tab : tabs) {
            HBox.setHgrow(tab, Priority.ALWAYS);
            tab.setMinHeight(MIN_TABS_HEIGHT);
            tab.setTabPane(this);
        }
        tabsHBox.setMinHeight(MIN_TABS_HEIGHT);

        tabsHBox.getStyleClass().add("tabs-holder");

        getChildren().addAll(contentPane, tabsHBox);
        AnchorPane.setTopAnchor(tabsHBox, 0D); AnchorPane.setRightAnchor(tabsHBox, 0D);
        AnchorPane.setLeftAnchor(tabsHBox, 0D);

        AnchorPane.setTopAnchor(contentPane, MIN_TABS_HEIGHT); AnchorPane.setBottomAnchor(contentPane, 0D);
        AnchorPane.setRightAnchor(contentPane, 0D); AnchorPane.setLeftAnchor(contentPane, 0D);

        if (tabs.length > 0) {
            select(tabs[0]);
        }
    }

    public void select(Tab tab) {
        if (selectedTab != null) {
            selectedTab.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
        }
        selectedTab = tab;
        tab.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
        contentPane.getChildren().setAll(tab.getNode());
        NodeUtils.anchor(tab.getNode());
    }

    public Tab[] getTabs() {
        return tabs;
    }
}
