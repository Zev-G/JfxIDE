package tmw.me.com.ide.settings;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import tmw.me.com.ide.settings.visual.SettingsPage;
import tmw.me.com.ide.tools.NodeUtils;
import tmw.me.com.ide.tools.customtabpane.FinalTabPane;
import tmw.me.com.ide.tools.customtabpane.Tab;
import tmw.me.com.ide.tools.tabPane.ComponentTabContent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

public class SettingsView extends AnchorPane implements ComponentTabContent<SettingsView> {

    private final FinalTabPane tabPane;

    public SettingsView(Tab... firstTabs) {
        ArrayList<Tab> tabs = new ArrayList<>(Arrays.asList(firstTabs));
        for (IdeSettings.FileAndInstance<SettingsJSON> fileAndSettingsJSON : IdeSettings.SETTING_PAGES) {
            tabs.add(UnloadedTab.getInstance(fileAndSettingsJSON.getInstance().pageName, () -> new SettingsPage<>(fileAndSettingsJSON.getFile(), fileAndSettingsJSON.getInstance())));
        }
        tabPane = new FinalTabPane(false, tabs.toArray(new Tab[0]));

        getChildren().add(tabPane);
        NodeUtils.anchor(tabPane);
    }

    @Override
    public SettingsView getImportantNode() {
        return this;
    }

    @Override
    public Region getMainNode() {
        return this;
    }

    @Override
    public void save(File file) {

    }

    @Override
    public SettingsView createNewCopy() {
        return null;
    }

    @Override
    public boolean canSplitHorizontally() {
        return false;
    }

    @Override
    public void onSelected() {
        if (tabPane.getSelectedTab() instanceof UnloadedTab) {
            ((UnloadedTab) tabPane.getSelectedTab()).loadFromSupplier();
        }
    }

    public FinalTabPane getTabPane() {
        return tabPane;
    }

    private static class UnloadedTab extends Tab {

        private static UnloadedTab getInstance(String text, Supplier<? extends Node> nodeSupplier) {
            return new UnloadedTab(text, new AnchorPane(), nodeSupplier);
        }

        private final AnchorPane holder;
        private final Supplier<? extends Node> nodeSupplier;

        private UnloadedTab(String text, AnchorPane holder, Supplier<? extends Node> nodeSupplier) {
            super(text, holder, false);
            this.holder = holder;
            this.nodeSupplier = nodeSupplier;

            selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    Node generated = nodeSupplier.get();
                    holder.getChildren().setAll(generated);
                    NodeUtils.anchor(generated);
                }
            });

            setPadding(new Insets(10));
        }

        public void loadFromSupplier() {
            Node generated = nodeSupplier.get();
            holder.getChildren().setAll(generated);
            NodeUtils.anchor(generated);
        }
    }

}
