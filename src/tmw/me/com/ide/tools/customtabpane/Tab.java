package tmw.me.com.ide.tools.customtabpane;

import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class Tab extends BorderPane {

    private final BooleanProperty autoExpanding = new SimpleBooleanProperty(this, "autoExpanding", false);
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final ObjectProperty<FinalTabPane> tabPane = new SimpleObjectProperty<>(this, "tabPane");
    private final StringProperty text = new SimpleStringProperty();

    private final Label label = new Label();

    public Tab(String text, Node node, boolean bool) {
        this(text, node);
        setAutoExpanding(bool);
    }

    public Tab(String text, Node node) {
        setContent(node);
        setText(text);

        setPadding(new Insets(5));

        label.textProperty().bind(this.text);

        setCenter(label);

        label.getStyleClass().add("tab-title");
        getStyleClass().add("tab-header");

        setOnMouseReleased(mouseEvent -> {
            if (tabPane != null && isHover()) {
                getTabPane().select(this);
            }
        });

        contentProperty().addListener((observableValue, node1, t1) -> {
            if (tabPane != null && getTabPane().getSelectedTab() == this) {
                getTabPane().updateContent();
            }
        });
        autoExpanding.addListener((observableValue, aBoolean, t1) -> HBox.setHgrow(this, t1 ? Priority.ALWAYS : Priority.NEVER));
    }

    public void setTabPane(FinalTabPane tabPane) {
        this.tabPane.set(tabPane);
    }

    public FinalTabPane getTabPane() {
        return tabPane.get();
    }

    public String getText() {
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public Node getContent() {
        return content.get();
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(Node content) {
        contentProperty().set(content);
    }

    public StringProperty textProperty() {
        return text;
    }

    public boolean isAutoExpanding() {
        return autoExpanding.get();
    }

    public BooleanProperty autoExpandingProperty() {
        return autoExpanding;
    }

    public void setAutoExpanding(boolean autoExpanding) {
        this.autoExpanding.set(autoExpanding);
    }
}
