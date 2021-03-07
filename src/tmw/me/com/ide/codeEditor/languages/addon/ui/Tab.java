package tmw.me.com.ide.codeEditor.languages.addon.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class Tab extends BorderPane {

    private final Node node;
    private String text;

    private final Label label = new Label();

    private FinalTabPane tabPane;

    public Tab(String text, Node node) {
        this.node = node;
        this.text = text;

        setCenter(label);
        label.setText(text);

        label.getStyleClass().add("tab-title");
        getStyleClass().add("tab-header");

        setOnMousePressed(mouseEvent -> {
            if (tabPane != null) {
                tabPane.select(this);
            }
        });
    }

    public void setTabPane(FinalTabPane tabPane) {
        this.tabPane = tabPane;
    }

    public FinalTabPane getTabPane() {
        return tabPane;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        label.setText(text);
    }

    public Node getNode() {
        return node;
    }

}
