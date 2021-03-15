package tmw.me.com.ide.codeEditor.addonBuilder;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class SelectorItem<T> extends VBox {

    private final Circle icon = new Circle(32);
    private final Label title = new Label();
    private final Label subTitle = new Label();

    private boolean randomizedColors = false;

    private final T value;

    public SelectorItem(T value, String title, String subtitle) {
        this.value = value;
        this.title.setText(title);
        this.subTitle.setText(subtitle);
        this.getStyleClass().add("selector-item");
        this.icon.getStyleClass().add("selector-icon");
        this.title.getStyleClass().add("selector-title");
        this.subTitle.getStyleClass().add("selector-sub-title");
        this.setPrefSize(150, 200);
        this.setSpacing(20);
        this.setAlignment(Pos.TOP_CENTER);
        this.setPadding(new Insets(30, 10, 10, 10));
        VBox textHolder = new VBox(this.title, this.subTitle);
        textHolder.setAlignment(Pos.CENTER);
//        textHolder.setSpacing(5);
        this.getChildren().addAll(this.icon, textHolder);
    }

    public void randomizeColor(int times) {
        icon.setStyle("-fx-fill: linear-gradient(rgb(" +
                ((Math.random() + 1) * times) + "," + ((Math.random() + 1) * times) + "," + ((Math.random() + 1) * times) + "), rgb(" +
                ((Math.random() + 1) * times) + "," + ((Math.random() + 1) * times) + "," + ((Math.random() + 1) * times) +
                "))");
        randomizedColors = true;
    }

    public void addHoverAnimation() {


        this.setOnMouseEntered(mouseEvent -> {
            ScaleTransition scaleTransition = new ScaleTransition(new Duration(250), this);
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
            scaleTransition.play();
        });
        this.setOnMouseExited(mouseEvent -> {
            ScaleTransition scaleTransition = new ScaleTransition(new Duration(250), this);
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);
            scaleTransition.play();
        });

    }


    public T getValue() {
        return value;
    }
}
