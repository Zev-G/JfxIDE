package tmw.me.com.ide.codeEditor.languages.addon.ui;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tmw.me.com.ide.codeEditor.languages.addon.StyleFactoryJSON;
import tmw.me.com.jfxhelper.ResizableTextField;
import tmw.me.com.ide.tools.SVG;
import tmw.me.com.ide.tools.control.SVGHoverButton;

import java.util.Collections;

public class StylePane extends BorderPane {

    private static final double MIN_HEIGHT = 100;
    private static final double PADDING = 7.5;

    private final StylesListPane listPane;
    private final StyleFactoryJSON json;

    private final Label idLabel = new Label("id: ");
    private final Label regexLabel = new Label("regex: ");
    private final Label styleLabel = new Label("style: ");
    private final TextField idField = new ResizableTextField();
    private final TextField regexField = new ResizableTextField();
    private final TextField styleField = new ResizableTextField();

    private final HBox idHBox = new HBox(idLabel, idField);
    private final HBox regexHBox = new HBox(regexLabel, regexField);
    private final HBox styleHBox = new HBox(styleLabel, styleField);

    private final SVGHoverButton rearrange = new SVGHoverButton(SVG.resizePath(SVG.BURGER, 0.3));
    private final SVGHoverButton trash = new SVGHoverButton(SVG.resizePath(SVG.TRASH, 0.8));
    private final HBox hoverButtons = new HBox(trash, rearrange);
    private final BorderPane rightPane = new BorderPane();

    private final VBox leftVBox = new VBox(idHBox, regexHBox, styleHBox);

    public StylePane(StylesListPane listPane, StyleFactoryJSON json) {
        this.listPane = listPane;
        this.json = json;

        idField.setText(json.id);
        regexField.setText(json.regex);
        styleField.setText(json.style);

        setMinHeight(MIN_HEIGHT);
        setPadding(new Insets(PADDING));

        getStyleClass().addAll("style-pane");
        rearrange.getStyleClass().add("simple-button");
        trash.getStyleClass().add("simple-button");
        trash.setFadeOutTo(0.4);
        trash.setFadeInTo(0.6);

        setLeft(leftVBox);
        setRight(rightPane);
        hoverButtons.setAlignment(Pos.CENTER);
        hoverButtons.setSpacing(4);
        rightPane.setCenter(hoverButtons);

        ChangeListener<String> textChangeListener = (observableValue, s, t1) -> listPane.refreshUpdatePane();

        idField.getStyleClass().addAll("editable-label", "style-editor");
        regexField.getStyleClass().addAll("editable-label", "style-editor");
        styleField.getStyleClass().addAll("editable-label", "style-editor");

        idLabel.getStyleClass().addAll("white-text", "style-label");
        regexLabel.getStyleClass().addAll("white-text", "style-label");
        styleLabel.getStyleClass().addAll("white-text", "style-label");

        idField.textProperty().addListener(textChangeListener);
        regexField.textProperty().addListener(textChangeListener);
        styleField.textProperty().addListener(textChangeListener);

        trash.setOnAction(actionEvent -> {
            listPane.getRealBox().getChildren().remove(this);
            listPane.refreshUpdatePane();
        });

        rearrange.setOnMousePressed(event -> {
            setOpacity(0.5);
            setEffect(new Glow(0.35));
        });
        rearrange.setOnMouseDragged(event -> {
            StylePane hoverPane = null;
            Node parent = event.getPickResult().getIntersectedNode();
            while (parent != null) {
                if (parent instanceof StylePane) {
                    hoverPane = (StylePane) parent;
                    break;
                } else {
                    parent = parent.getParent();
                }
            }
            if (hoverPane != null) {
                ObservableList<Node> workingCollection = FXCollections.observableArrayList(listPane.getRealBox().getChildren());
                Collections.swap(workingCollection, listPane.getRealBox().getChildren().indexOf(this), listPane.getRealBox().getChildren().indexOf(hoverPane));
                listPane.getRealBox().getChildren().setAll(workingCollection);
            }
        });
        rearrange.setOnMouseReleased(event -> {
            setOpacity(1);
            setEffect(null);
            listPane.refreshUpdatePane();
        });
    }

    public boolean fieldsMatch() {
        return idField.getText().equals(json.id) && regexField.getText().equals(json.regex) && styleField.getText().equals(json.style);
    }

    public StyleFactoryJSON getJson() {
        return json;
    }

    public void update() {
        json.id = idField.getText();
        json.regex = regexField.getText();
        json.style = styleField.getText();
    }

}
