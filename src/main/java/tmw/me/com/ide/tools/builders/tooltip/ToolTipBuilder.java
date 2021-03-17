package tmw.me.com.ide.tools.builders.tooltip;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import tmw.me.com.Resources;
import tmw.me.com.ide.tools.builders.Builder;

/**
 * All ToolTips in the IDE are built with this, this is simply a class for quickly creating powerful ToolTips which follow a common structure.
 */
public class ToolTipBuilder extends Builder<Tooltip> {

    private StringProperty header = new SimpleStringProperty();
    private StringProperty mainText = new SimpleStringProperty();
    private StringProperty headerIcon = new SimpleStringProperty();
    private StringProperty mainTextIcon = new SimpleStringProperty();

    public static ToolTipBuilder create() {
        return new ToolTipBuilder();
    }

    public ToolTipBuilder setHeader(String s) {
        this.header.set(s);
        return this;
    }

    public ToolTipBuilder setMainText(String s) {
        this.mainText.set(s);
        return this;
    }

    public ToolTipBuilder setHeaderIcon(String s) {
        this.headerIcon.set(s);
        return this;
    }

    public ToolTipBuilder setMainTextIcon(String s) {
        this.mainTextIcon.set(s);
        return this;
    }

    public ToolTipBuilder setHeaderProperty(StringProperty stringProperty) {
        this.header = stringProperty;
        return this;
    }

    public ToolTipBuilder setMainTextProperty(StringProperty stringProperty) {
        this.mainText = stringProperty;
        return this;
    }

    public ToolTipBuilder setHeaderIconProperty(StringProperty stringProperty) {
        this.headerIcon = stringProperty;
        return this;
    }

    public ToolTipBuilder setMainTextIconProperty(StringProperty stringProperty) {
        this.mainTextIcon = stringProperty;
        return this;
    }

    public String getHeader() {
        return header.get();
    }

    public StringProperty headerProperty() {
        return header;
    }

    public String getMainText() {
        return mainText.get();
    }

    public StringProperty mainTextProperty() {
        return mainText;
    }

    public String getHeaderIcon() {
        return headerIcon.get();
    }

    public StringProperty headerIconProperty() {
        return headerIcon;
    }

    public String getMainTextIcon() {
        return mainTextIcon.get();
    }

    public StringProperty mainTextIconProperty() {
        return mainTextIcon;
    }

    public Tooltip build() {
        Tooltip tooltip = new Tooltip();
        VBox topVbox = new VBox();
        topVbox.getStylesheets().addAll(Resources.getExternalForm(Resources.EDITOR_STYLES + "tooltip.css"));
        tooltip.setGraphic(topVbox);
        Label headerLabel = new Label();
        headerLabel.textProperty().bind(header);
        headerLabel.getStyleClass().add("header");
        headerLabel.setTextFill(Paint.valueOf("#b94646"));
        HBox titleBox = new HBox(headerLabel);
        if (headerIcon.get() != null) {
            SVGPath svgPath = new SVGPath();
            svgPath.contentProperty().bind(headerIcon);
            svgPath.getStyleClass().add("header-icon");
            svgPath.setPickOnBounds(true);
            titleBox.getChildren().add(svgPath);
        }
        if (header.get() != null) {
            topVbox.getChildren().add(titleBox);
        } else {
            header.addListener((observableValue, s, t1) -> {
                if (t1 != null) topVbox.getChildren().add(titleBox);
            });
        }
        Label mainTextLabel = new Label();
        mainTextLabel.textProperty().bind(mainText);
        mainTextLabel.getStyleClass().add("main-text");
        mainTextLabel.setTextFill(Paint.valueOf("#bcc5d2"));
        HBox textBox = new HBox(mainTextLabel);
        if (mainTextIcon.get() != null) {
            SVGPath svgPath = new SVGPath();
            svgPath.contentProperty().bind(mainTextIcon);
            svgPath.getStyleClass().add("main-text-icon");
            svgPath.setPickOnBounds(true);
            textBox.getChildren().add(svgPath);
        }
        if (mainText.get() != null) {
            topVbox.getChildren().add(textBox);
        } else {
            mainText.addListener((observableValue, s, t1) -> {
                if (t1 != null) topVbox.getChildren().add(textBox);
            });
        }
        return tooltip;
    }


}
