package tmw.me.com.ide.tools.colorpicker;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import tmw.me.com.ide.images.Images;

public class MyCustomColorPicker extends VBox {

    private final ObjectProperty<Color> currentColorProperty = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> customColorProperty = new SimpleObjectProperty<>(Color.TRANSPARENT);

    private Pane opacityBar = null;
    private Region opacityBarIndicator = null;

    private final Pane colorRect;
    private final Pane colorBar;
    private final Pane colorRectOverlayOne;
    private final Pane colorRectOverlayTwo;
    private final Region colorRectIndicator;
    private final Region colorBarIndicator;
    private final Pane newColorRect;

    private final DoubleProperty hue = new SimpleDoubleProperty(-1);
    private final DoubleProperty sat = new SimpleDoubleProperty(-1);
    private final DoubleProperty bright = new SimpleDoubleProperty(-1);

    private final DoubleProperty alpha = new SimpleDoubleProperty(100) {
        @Override
        protected void invalidated() {
            setCustomColor(new Color(getCustomColor().getRed(), getCustomColor().getGreen(),
                    getCustomColor().getBlue(), clamp(alpha.get() / 100)));
        }
    };

    public MyCustomColorPicker() {
        this(true);
    }

    public MyCustomColorPicker(boolean includeOpacity) {

        System.out.println(includeOpacity);

        getStyleClass().add("my-custom-color");

        VBox box = new VBox();

        box.getStyleClass().add("color-rect-pane");
        customColorProperty().addListener((ov, t, t1) -> colorChanged());

        colorRectIndicator = new Region();
        colorRectIndicator.setId("color-rect-indicator");
        colorRectIndicator.setManaged(false);
        colorRectIndicator.setMouseTransparent(true);
        colorRectIndicator.setCache(true);

        final Pane colorRectOpacityContainer = new StackPane();

        colorRect = new StackPane();
        colorRect.getStyleClass().addAll("color-rect", "transparent-pattern");

        Pane colorRectHue = new Pane();
        colorRectHue.backgroundProperty().bind(new ObjectBinding<>() {
            {
                bind(hue);
            }

            @Override
            protected Background computeValue() {
                return new Background(new BackgroundFill(
                        Color.hsb(hue.getValue(), 1.0, 1.0),
                        CornerRadii.EMPTY, Insets.EMPTY));

            }
        });

        colorRectOverlayOne = new Pane();
        colorRectOverlayOne.getStyleClass().add("color-rect");
        colorRectOverlayOne.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(255, 255, 255, 1)),
                        new Stop(1, Color.rgb(255, 255, 255, 0))),
                CornerRadii.EMPTY, Insets.EMPTY)));

        EventHandler<MouseEvent> rectMouseHandler = event -> {
            final double x = event.getX();
            final double y = event.getY();
            sat.set(clamp(x / colorRect.getWidth()) * 100);
            bright.set(100 - (clamp(y / colorRect.getHeight()) * 100));
            updateHSBColor();
        };

        colorRectOverlayTwo = new Pane();
        colorRectOverlayTwo.getStyleClass().addAll("color-rect");
        colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(0, 0, 0, 0)), new Stop(1, Color.rgb(0, 0, 0, 1))),
                CornerRadii.EMPTY, Insets.EMPTY)));
        colorRectOverlayTwo.setOnMouseDragged(rectMouseHandler);
        colorRectOverlayTwo.setOnMousePressed(rectMouseHandler);

        Pane colorRectBlackBorder = new Pane();
        colorRectBlackBorder.setMouseTransparent(true);
        colorRectBlackBorder.getStyleClass().addAll("color-rect", "color-rect-border");

        colorBar = new Pane();
        colorBar.getStyleClass().add("color-bar");
        colorBar.setBackground(new Background(new BackgroundFill(createHueGradient(),
                CornerRadii.EMPTY, Insets.EMPTY)));

        StackPane opacityBarSecondaryBG = null;
        if (includeOpacity) {

            opacityBar = new Pane();

            opacityBarSecondaryBG = new StackPane(opacityBar);
            opacityBarSecondaryBG.setBackground(new Background(new BackgroundFill(new ImagePattern(new Image(Images.get("opacity.png")), 0, 0, 10, 10, false), CornerRadii.EMPTY, Insets.EMPTY)));
            opacityBarSecondaryBG.getStyleClass().add("color-bar");

            opacityBarIndicator = new Region();
            opacityBarIndicator.setId("opacity-bar-indicator");
            opacityBarIndicator.setMouseTransparent(true);
            opacityBarIndicator.setCache(true);

            opacityBarIndicator.layoutXProperty().bind(
                    alpha.divide(100).multiply(opacityBar.widthProperty()));


            StackPane finalOpacityBarSecondaryBG = opacityBarSecondaryBG;
            EventHandler<MouseEvent> opacityMouseHandler = event -> {
                final double x = event.getX();
                alpha.set(clamp(x / finalOpacityBarSecondaryBG.getWidth()) * 100);
                updateHSBColor();
            };

            opacityBar.setOnMouseDragged(opacityMouseHandler);
            opacityBar.setOnMousePressed(opacityMouseHandler);

            opacityBar.backgroundProperty().bind(new ObjectBinding<>() {
                {
                    bind(customColorProperty);
                }

                @Override
                protected Background computeValue() {
                    return new Background(new BackgroundFill(createOpacityGradient(customColorProperty.get()), CornerRadii.EMPTY, Insets.EMPTY));
                }
            });
            opacityBar.getChildren().setAll(opacityBarIndicator);
        }

        colorBarIndicator = new Region();
        colorBarIndicator.setId("color-bar-indicator");
        colorBarIndicator.setMouseTransparent(true);
        colorBarIndicator.setCache(true);

        colorRectIndicator.layoutXProperty().bind(
                sat.divide(100).multiply(colorRect.widthProperty()));
        colorRectIndicator.layoutYProperty().bind(
                Bindings.subtract(1, bright.divide(100)).multiply(colorRect.heightProperty()));
        colorBarIndicator.layoutXProperty().bind(
                hue.divide(360).multiply(colorBar.widthProperty()));
        colorRectOpacityContainer.opacityProperty().bind(alpha.divide(100));

        EventHandler<MouseEvent> barMouseHandler = event -> {
            final double x = event.getX();
            hue.set(clamp(x / colorRect.getWidth()) * 360);
            updateHSBColor();
        };

        colorBar.setOnMouseDragged(barMouseHandler);
        colorBar.setOnMousePressed(barMouseHandler);

        newColorRect = new Pane();
        newColorRect.getStyleClass().add("color-new-rect");
        newColorRect.setId("new-color");

        colorBar.getChildren().setAll(colorBarIndicator);
        colorRectOpacityContainer.getChildren().setAll(colorRectHue, colorRectOverlayOne, colorRectOverlayTwo);
        colorRect.getChildren().setAll(colorRectOpacityContainer, colorRectBlackBorder, colorRectIndicator);
        VBox.setVgrow(colorRect, Priority.SOMETIMES);
        box.getChildren().addAll(colorBar, colorRect);
        if (includeOpacity) {
            box.getChildren().add(opacityBarSecondaryBG);
        }

        getChildren().add(box);

        if (currentColorProperty.get() == null) {
            currentColorProperty.set(Color.TRANSPARENT);
        }
        updateValues();

        getStylesheets().add(MyCustomColorPicker.class.getResource("color.css").toExternalForm());

    }

    private void updateValues() {
        hue.set(getCurrentColor().getHue());
        sat.set(getCurrentColor().getSaturation() * 100);
        bright.set(getCurrentColor().getBrightness() * 100);
        alpha.set(getCurrentColor().getOpacity() * 100);
        setCustomColor(Color.hsb(hue.get(), clamp(sat.get() / 100),
                clamp(bright.get() / 100), clamp(alpha.get() / 100)));
    }

    private void colorChanged() {
        hue.set(getCustomColor().getHue());
        sat.set(getCustomColor().getSaturation() * 100);
        bright.set(getCustomColor().getBrightness() * 100);
    }

    private void updateHSBColor() {
        Color newColor = Color.hsb(hue.get(), clamp(sat.get() / 100),
                clamp(bright.get() / 100), clamp(alpha.get() / 100));
        setCustomColor(newColor);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        colorRectIndicator.autosize();
    }

    static double clamp(double value) {
        return value < 0 ? 0 : value > 1 ? 1 : value;
    }

    private static LinearGradient createHueGradient() {
        double offset;
        Stop[] stops = new Stop[255];
        for (int x = 0; x < 255; x++) {
            offset = (1.0 / 255) * x;
            int h = (int) ((x / 255.0) * 360);
            stops[x] = new Stop(offset, Color.hsb(h, 1.0, 1.0));
        }
        return new LinearGradient(0f, 0f, 1f, 0f, true, CycleMethod.NO_CYCLE, stops);
    }

    private static LinearGradient createOpacityGradient(Color paint) {
        Color opacity0 = Color.rgb((int) (paint.getRed() * 255), (int) (paint.getGreen() * 255), (int) (paint.getBlue() * 255), 0);
        Color opacity1 = Color.rgb((int) (paint.getRed() * 255), (int) (paint.getGreen() * 255), (int) (paint.getBlue() * 255), 1);
        return new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, opacity0), new Stop(1, opacity1));
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColorProperty.set(currentColor);
        updateValues();
    }

    public Color getCurrentColor() {
        return currentColorProperty.get();
    }

    public final ObjectProperty<Color> customColorProperty() {
        return customColorProperty;
    }

    public void setCustomColor(Color color) {
        customColorProperty.set(color);
    }

    public Color getCustomColor() {
        return customColorProperty.get();
    }

    public double getAlpha() {
        return alpha.get();
    }

    public void setAlpha(int alpha) {
        this.alpha.set(alpha);
    }

}
