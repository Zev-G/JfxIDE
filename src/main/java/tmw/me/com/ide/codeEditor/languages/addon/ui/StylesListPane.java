package tmw.me.com.ide.codeEditor.languages.addon.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.languages.CssLanguage;
import tmw.me.com.ide.codeEditor.languages.addon.JSONHelper;
import tmw.me.com.ide.codeEditor.languages.addon.LanguageAddon;
import tmw.me.com.ide.codeEditor.languages.addon.StyleFactoryJSON;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;
import tmw.me.com.jfxhelper.NodeUtils;
import tmw.me.com.ide.tools.SVG;
import tmw.me.com.ide.tools.control.SVGHoverButton;
import tmw.me.com.ide.tools.customtabpane.FinalTabPane;
import tmw.me.com.ide.tools.customtabpane.Tab;

import java.util.ArrayList;

public class StylesListPane extends AnchorPane {

    private static final double BUTTON_OPACITY = 0.75;

    private String originalCSS;

    private final VBox realBox = new VBox();
    private final ScrollPane itemsScroller = new ScrollPane(realBox);
    private final IntegratedTextEditor cssEditor = new IntegratedTextEditor(new CssLanguage());

    private final Button updateButton = new Button("Update");
    private final BorderPane updatePane = new BorderPane();

    private final SVGHoverButton plusButton = new SVGHoverButton(SVG.PLUS);
    private final BorderPane buttonHolder = new BorderPane(plusButton);

    private final LanguageAddon addon;
    private final AddonEditor addonEditor;

    private final FinalTabPane tabPane = new FinalTabPane(new Tab("Json View", itemsScroller, true), new Tab("Css View", cssEditor.getMainNode(), true));

    public StylesListPane(LanguageAddon addon, AddonEditor editor) {
        this.addon = addon;
        this.addonEditor = editor;
        getChildren().addAll(tabPane, updatePane);
        NodeUtils.anchor(tabPane);

        originalCSS = Ide.readFile(addon.getStyleFile());
        cssEditor.replaceText(originalCSS);
        originalCSS = cssEditor.getTabbedText();
        cssEditor.lockLanguageUI();
        cssEditor.textProperty().addListener((observableValue, s, t1) -> refreshUpdatePane());

        updatePane.setCenter(updateButton);
        updateButton.getStyleClass().addAll("xl-title", "update-button", "white-text");
        realBox.getStyleClass().add("dark-background");

        updatePane.setVisible(false);
        updatePane.setMouseTransparent(true);

        itemsScroller.setPannable(true);

        AnchorPane.setLeftAnchor(updatePane, 0D);
        AnchorPane.setRightAnchor(updatePane, 0D);
        AnchorPane.setBottomAnchor(updatePane, 50D);

        for (StyleFactoryJSON factoryJSON : addon.getHighlighterJSON().styles) {
            realBox.getChildren().add(new StylePane(this, factoryJSON));
        }
        buttonHolder.setPadding(new Insets(15, 0, 0, 0));
        buttonHolder.setCenter(plusButton);
        realBox.getChildren().add(buttonHolder);
        realBox.setAlignment(Pos.TOP_CENTER);

        itemsScroller.setFitToWidth(true);
        itemsScroller.setFitToHeight(true);

        plusButton.setOnAction(actionEvent -> {
            StyleFactoryJSON factoryJSON = new StyleFactoryJSON();
            factoryJSON.style = "";
            factoryJSON.regex = "";
            factoryJSON.id = "";
            addon.getHighlighterJSON().styles.add(factoryJSON);
            realBox.getChildren().add(new StylePane(this, factoryJSON));
            buttonHolder.toFront();
            update();
        });

        updateButton.setOnAction(actionEvent -> {
            update();
            hideUpdatePane();
        });
    }

    public VBox getRealBox() {
        return realBox;
    }

    public void showUpdatePane() {
        updatePane.setOpacity(0);
        updatePane.setMouseTransparent(false);
        updatePane.setVisible(true);
        NodeUtils.transOpacity(updatePane, BUTTON_OPACITY, 300, null);
    }

    public void hideUpdatePane() {
        NodeUtils.transOpacity(updatePane, 0, 150, actionEvent -> {
            updatePane.setMouseTransparent(true);
            updatePane.setVisible(false);
        });
    }

    public void refreshUpdatePane() {
        boolean updatePaneIsShowing = updatePane.isVisible();
        if (realBox.getChildren().size() - 1 != addon.getHighlighterJSON().styles.size()) {
            if (!updatePaneIsShowing) {
                showUpdatePane();
            }
            return;
        }
        if (!cssEditor.getTabbedText().equals(originalCSS)) {
            if (!updatePaneIsShowing) {
                showUpdatePane();
            }
            return;
        }
        int i = 0;
        for (Node node : realBox.getChildren()) {
            if (node instanceof StylePane) {
                StylePane pane = (StylePane) node;
                if (pane.getJson() != addon.getHighlighterJSON().styles.get(i) || !pane.fieldsMatch()) {
                    if (!updatePaneIsShowing) {
                        showUpdatePane();
                    }
                    return;
                }
                i++;
            }
        }
        if (updatePaneIsShowing) {
            hideUpdatePane();
        }
    }

    public void update() {
        ArrayList<StyleFactoryJSON> jsonOrder = new ArrayList<>();
        for (Node node : realBox.getChildren()) {
            if (node instanceof StylePane) {
                StylePane pane = (StylePane) node;
                pane.update();
                jsonOrder.add(pane.getJson());
            }
        }
        addon.getHighlighterJSON().styles = jsonOrder;
        cssEditor.save(addon.getStyleFile());
        originalCSS = cssEditor.getTabbedText();
        JSONHelper.toFile(addon.getHighlighterFile(), addon.getHighlighterJSON());
        addonEditor.updated();
    }

}
