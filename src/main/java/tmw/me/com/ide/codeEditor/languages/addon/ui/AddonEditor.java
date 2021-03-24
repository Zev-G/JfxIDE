package tmw.me.com.ide.codeEditor.languages.addon.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import tmw.me.com.ide.codeEditor.languages.addon.AddonLanguageSupport;
import tmw.me.com.ide.codeEditor.languages.addon.JSONHelper;
import tmw.me.com.ide.codeEditor.languages.addon.LanguageAddon;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;
import tmw.me.com.ide.tools.ResizableTextField;
import tmw.me.com.ide.tools.concurrent.schedulers.ChangeListenerScheduler;
import tmw.me.com.ide.tools.tabPane.ComponentTabContent;

import java.io.File;

public class AddonEditor extends AnchorPane implements ComponentTabContent<AddonEditor> {

    private static final double HORIZONTAL_PADDING = 25;
    private static final double BOTTOM_PADDING = HORIZONTAL_PADDING * 0.75;
    private static final double TOP_PADDING = HORIZONTAL_PADDING * 0.35;
    private static final double NAME_FIELD_SPACING = 15;
    private static final double VBOX_SPACING = TOP_PADDING;
    private static final double STYLES_VBOX_MIN_WIDTH = 200;

    private final LanguageAddon addon;
    private final AddonLanguageSupport languageSupport;

    private final Label nameLabel = new Label("name:");
    private final TextField nameField = new ResizableTextField();


    private final StylesListPane stylesListPane; // Gets initialized in the constructor.
    private final ScrollPane stylesScroller = new ScrollPane();

    private final Label stylesField = new Label("Styles:");

    private final IntegratedTextEditor integratedTextEditor = new IntegratedTextEditor();
    private final HBox leftTopHBox = new HBox(nameLabel, nameField);
    private final VBox leftVBox = new VBox(leftTopHBox, integratedTextEditor.getTextAreaHolder());
    private final VBox rightVBox = new VBox(stylesField, stylesScroller);
    private final SplitPane horizontalSplitter = new SplitPane(leftVBox, rightVBox);

    public AddonEditor(File file) {
        this.languageSupport = AddonLanguageSupport.fromDir(file);
        assert languageSupport != null;
        this.addon = languageSupport.getAddon();

        stylesListPane = new StylesListPane(addon, this);
        stylesScroller.setContent(stylesListPane);

        integratedTextEditor.setLanguage(languageSupport);
        integratedTextEditor.lockLanguageUI();
        integratedTextEditor.setFontSize(40);

        nameField.textProperty().addListener(new ChangeListenerScheduler<>(300, (observableValue, s, t1) -> {
            addon.getAddonJSON().name = t1;
            JSONHelper.toFile(addon.getAddonFile(), addon.getAddonJSON());
        }));

        this.getStyleClass().add("darker-background");
        horizontalSplitter.getStyleClass().addAll("darker-split-pane", "large-dividers");
        leftVBox.getStyleClass().add("darker-background");
        rightVBox.getStyleClass().add("darker-background");
        integratedTextEditor.getMainNode().getStyleClass().add("black-border");
        stylesScroller.getStyleClass().addAll("dark-background", "black-border");
        stylesListPane.getStyleClass().add("dark-background");
        stylesField.getStyleClass().addAll("xxl-title", "white-text");
        nameLabel.getStyleClass().addAll("xl-title", "white-text");
        nameField.getStyleClass().addAll("xl-title", "editable-label", "white-text");

        nameField.setText(addon.getName());
        leftTopHBox.setAlignment(Pos.CENTER);
        rightVBox.setAlignment(Pos.CENTER);

        leftTopHBox.setSpacing(NAME_FIELD_SPACING);
        leftVBox.setSpacing(VBOX_SPACING);
        rightVBox.setSpacing(VBOX_SPACING);
        rightVBox.setMinWidth(STYLES_VBOX_MIN_WIDTH);

        stylesScroller.setFitToWidth(true);
        stylesScroller.setFitToHeight(true);

        VBox.setVgrow(integratedTextEditor.getMainNode(), Priority.ALWAYS);
        VBox.setVgrow(stylesScroller, Priority.ALWAYS);

        getChildren().add(horizontalSplitter);
        AnchorPane.setTopAnchor(horizontalSplitter, TOP_PADDING);
        AnchorPane.setBottomAnchor(horizontalSplitter, BOTTOM_PADDING);
        AnchorPane.setRightAnchor(horizontalSplitter, HORIZONTAL_PADDING);
        AnchorPane.setLeftAnchor(horizontalSplitter, HORIZONTAL_PADDING);

        Platform.runLater(() -> leftTopHBox.setMinHeight(stylesField.getHeight()));
    }

    public void updated() {
        integratedTextEditor.setLanguage(addon.buildNewLanguageSupport());
    }


    @Override
    public AddonEditor getImportantNode() {
        return this;
    }

    @Override
    public Region getMainNode() {
        return this;
    }

    @Override
    public void save(File file) {
        stylesListPane.update();
    }

    @Override
    public AddonEditor createNewCopy() {
        return null;
    }

    @Override
    public boolean canSplitHorizontally() {
        return false;
    }
}
