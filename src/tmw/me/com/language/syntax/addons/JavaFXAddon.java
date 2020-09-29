package tmw.me.com.language.syntax.addons;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.syntaxPiece.effects.EffectFactory;
import tmw.me.com.language.syntaxPiece.events.WhenEventFactory;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionPriority;
import tmw.me.com.language.variable.LinkedList;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JavaFXAddon extends AddonBase {

    @Override
    public HashMap<String, Class<?>> addTypes() {

        HashMap<String, Class<?>> supportedTypes = new HashMap<>();

        supportedTypes.put("stage", Stage.class);
        supportedTypes.put("window", Window.class);
        supportedTypes.put("color", Color.class);
        supportedTypes.put("node", Node.class);
        supportedTypes.put("parent", Parent.class);
        supportedTypes.put("button", Button.class);
        supportedTypes.put("label", Label.class);
        supportedTypes.put("pane", Pane.class);
        supportedTypes.put("region", Region.class);
        supportedTypes.put("vbox", VBox.class);
        supportedTypes.put("hbox", HBox.class);
        supportedTypes.put("duration", Duration.class);
        supportedTypes.put("background", Background.class);
        supportedTypes.put("string-property", StringProperty.class);
        supportedTypes.put("mouse-button", MouseButton.class);
        supportedTypes.put("pick-result", MouseButton.class);
        supportedTypes.put("grid-pane", GridPane.class);
        supportedTypes.put("image", Image.class);
        supportedTypes.put("image-view", ImageView.class);

        return supportedTypes;
    }

    @Override
    public ArrayList<EffectFactory> addEffects() {

        ArrayList<EffectFactory> effectFactories = new ArrayList<>();

        effectFactories.add(new EffectFactory("set title of %stage% to %string%",
                (state, values, args) -> ((Stage) values.get(0)).setTitle((String) values.get(1))));
        effectFactories.add(new EffectFactory("show %stage%", (state, values, args) -> ((Stage) values.get(0)).show()));
        effectFactories.add(new EffectFactory("stop program", (state, values, args) -> Platform.exit()));
        effectFactories.add(new EffectFactory("set fill color of %stage% to %color%", (state, values, args) -> {
            Stage stage = (Stage) values.get(0);
            if (stage.getScene() != null) {
                stage.getScene().setFill((Color) values.get(1));
            }
        }));
        effectFactories.add(new EffectFactory("put %parent% into %stage%", (state, values, args) -> {
            Stage stage = (Stage) values.get(1);
            stage.setScene(new Scene((Parent) values.get(0)));
        }));
        effectFactories.add(new EffectFactory("set text of %button% to %string%", (state, values, args) -> ((Button) values.get(0)).setText(values.get(1).toString())));
        effectFactories.add(new EffectFactory("set background color of %region% to %color%", (state, values, args) -> {
            Region region = ((Region) values.get(0));
            BackgroundFill currentFill = new BackgroundFill(null, new CornerRadii(3), Insets.EMPTY);
            if (region.getBackground() != null && region.getBackground().getFills() != null && !region.getBackground().getFills().isEmpty()) {
                currentFill = region.getBackground().getFills().get(0);
            }
            region.setBackground(new Background(new BackgroundFill((Color) values.get(1), currentFill.getRadii(), currentFill.getInsets())));
        }));
        effectFactories.add(new EffectFactory("set scale x of %node% to %number%", (state, values, args) -> ((Node) values.get(0)).setScaleX((Double) values.get(1))));
        effectFactories.add(new EffectFactory("set scale y of %node% to %number%", (state, values, args) -> ((Node) values.get(0)).setScaleY((Double) values.get(1))));
        effectFactories.add(new EffectFactory("set scale of %node% to %number%", (state, values, args) -> {
            ((Node) values.get(0)).setScaleY((Double) values.get(1));
            ((Node) values.get(0)).setScaleX((Double) values.get(1));
        }));
        effectFactories.add(new EffectFactory("set background of %region% to %background%", (state, values, args) -> ((Region) values.get(0)).setBackground((Background) values.get(1))));
        effectFactories.add(new EffectFactory("push %node% to front", (state, values, args) -> ((Node) values.get(0)).toFront()));
        effectFactories.add(new EffectFactory("push %node% to back", (state, values, args) -> ((Node) values.get(0)).toBack()));
        effectFactories.add(new EffectFactory("set rotation of %node% to %number%", (state, values, args) -> ((Node) values.get(0)).setRotate((double) (Number) values.get(1))));
        effectFactories.add(new EffectFactory("set style of %node% to %string%", (state, values, args) -> ((Node) values.get(0)).setStyle(values.get(1).toString())));
        effectFactories.add(new EffectFactory("set opacity of %node% to %number%", (state, values, args) -> ((Node) values.get(0)).setOpacity((double) (Number) values.get(1))));
        effectFactories.add(new EffectFactory("disable %node%", (state, values, args) -> ((Node) values.get(0)).setDisable(true)));
        effectFactories.add(new EffectFactory("enable %node%", (state, values, args) -> ((Node) values.get(0)).setDisable(false)));
        effectFactories.add(new EffectFactory("set value of %string-property% to %string%", (state, values, args) -> ((StringProperty) values.get(0)).set(values.get(1).toString())));
        effectFactories.add(new EffectFactory("bind %string-property% to %string-property%", (state, values, args) -> ((StringProperty) values.get(0)).bind(((StringProperty) values.get(1)))));
        effectFactories.add(new EffectFactory("transition scale of %node% to %number% over %number% second(s|)", (state, values, args) -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
            scaleTransition.setToX((double) (Number) values.get(1));
            scaleTransition.setToY((double) (Number) values.get(1));
            scaleTransition.play();
        }));
        effectFactories.add(new EffectFactory("transition x position of %node% to %number% over %number% seconds","transition x position of %node% to %number% over %number% second(s|)", (state, values, args) -> {
            TranslateTransition scaleTransition = new TranslateTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
            scaleTransition.setToX((double) (Number) values.get(1));
            scaleTransition.play();
        }));
        effectFactories.add(new EffectFactory("transition y position of %node% to %number% over %number% seconds","transition y position of %node% to %number% over %number% second(s|)", (state, values, args) -> {
            TranslateTransition scaleTransition = new TranslateTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
            scaleTransition.setToY((double) (Number) values.get(1));
            scaleTransition.play();
        }));
        effectFactories.add(new EffectFactory("transition position of %node% to %number% over %number% seconds","transition position of %node% to %number% over %number% second(s|)", (state, values, args) -> {
            TranslateTransition scaleTransition = new TranslateTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
            scaleTransition.setToX((double) (Number) values.get(1));
            scaleTransition.setToY(((Number) values.get(1)).doubleValue());
            scaleTransition.play();
        }));
        effectFactories.add(new EffectFactory("transition opacity of %node% to %number% over %number% seconds","transition opacity of %node% to %number% over %number% second(s|)", (state, values, args) -> {
            FadeTransition scaleTransition = new FadeTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
            scaleTransition.setToValue((double) (Number) values.get(1));
            scaleTransition.play();
        }));
        effectFactories.add(new EffectFactory("full screen %stage%", "(|set )full screen %stage%", (state, values, args) -> ((Stage) values.get(0)).setFullScreen(true)));
        effectFactories.add(new EffectFactory("unfull screen %stage%", "(|set )(normal|unfull) screen %stage%", (state, values, args) -> ((Stage) values.get(0)).setFullScreen(false)));
        effectFactories.add(new EffectFactory("maximize %stage%", "(|set )maximize %stage%", (state, values, args) -> ((Stage) values.get(0)).setMaximized(true)));
        effectFactories.add(new EffectFactory("un maximize %stage%", "(|set )un maximize %stage%", (state, values, args) -> ((Stage) values.get(0)).setMaximized(false)));
        effectFactories.add(new EffectFactory("make %stage% resizable", (state, values, args) -> ((Stage) values.get(0)).setResizable(true)));
        effectFactories.add(new EffectFactory("make %stage% not resizable", (state, values, args) -> ((Stage) values.get(0)).setResizable(true)));
        effectFactories.add(new EffectFactory("hide %window%", (state, values, args) -> ((Window) values.get(0)).hide()));
        effectFactories.add(new EffectFactory("set icon of %stage% to %image%", (state, values, args) -> ((Stage) values.get(0)).getIcons().add((Image) values.get(1))));
        effectFactories.add(new EffectFactory("make %node% visible", (state, values, args) -> ((Node) values.get(0)).setVisible(true)));
        effectFactories.add(new EffectFactory("make %node% invisible", (state, values, args) -> ((Node) values.get(0)).setVisible(false)));
        effectFactories.add(new EffectFactory("make %node% mouse transparent", (state, values, args) -> ((Node) values.get(0)).setMouseTransparent(true)));
        effectFactories.add(new EffectFactory("make %node% not mouse transparent", (state, values, args) -> ((Node) values.get(0)).setMouseTransparent(false)));

        effectFactories.add(new EffectFactory("set spacing of vbox %vbox% to %number%", (state, values, args) -> ((VBox) values.get(0)).setSpacing(((Number) values.get(1)).doubleValue())));
        effectFactories.add(new EffectFactory("set spacing of hbox %hbox% to %number%", (state, values, args) -> ((HBox) values.get(0)).setSpacing(((Number) values.get(1)).doubleValue())));
        
        return effectFactories;
    }

    @Override
    public ArrayList<WhenEventFactory> addEvents() {
        
        ArrayList<WhenEventFactory> eventFactories = new ArrayList<>();

        eventFactories.add(new WhenEventFactory("javafx:", "(|run on )javafx( thread|)", (state, values, event, args) -> Platform.runLater(event::run)));
        
        // Mouse Events
        eventFactories.add(new WhenEventFactory("when %node% is pressed:", "(when|on) %node% is pressed", (state, values, event, args) -> ((Node) values.get(0)).setOnMousePressed(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when %node% is clicked:","(when|on) %node% is clicked", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseClicked(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when mouse drag enters %node%:","(when|on) mouse drag enters %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseDragEntered(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when mouse drag exits %node%:","(when|on) mouse drag exits %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseDragExited(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when mouse moves over %node%:","(when|on) mouse moves over %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseMoved(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when %node% is released:","(when|on) %node% is released", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseReleased(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when drag is detected for %node%:","(when|on) drag is detected for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragDetected(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when mouse enters %node%:","(when|on) mouse enters %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseEntered(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when mouse exits %node%:", "(when|on) mouse exits %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseExited(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
        // Drag Events
        eventFactories.add(new WhenEventFactory("when drag is done for %node%:","(when|on) drag is done for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragDone(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when drag is dropped on %node%:","(when|on) drag is dropped on %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragDropped(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when drag enters %node%:","(when|on) drag enters %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragEntered(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when drag exits %node%:","(when|on) drag exits %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragExited(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when drag is over %node%:","(when|on) drag is over %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragOver(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
        // Key Events
        eventFactories.add(new WhenEventFactory("when key is pressed for %node%:","(when|on) key is pressed for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnKeyPressed(keyEvent -> { addKeyEventExpressions(keyEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when key is released for %node%:","(when|on) key is released for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnKeyReleased(keyEvent -> { addKeyEventExpressions(keyEvent, event.getRunChunk()); event.run(); })));
        eventFactories.add(new WhenEventFactory("when key is typed for %node%:","(when|on) key is typed for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnKeyTyped(keyEvent -> { addKeyEventExpressions(keyEvent, event.getRunChunk()); event.run(); })));

        eventFactories.add(new WhenEventFactory("when %string-property% changes:","(when|on) %string-property% changes", (state, values, event, args) -> ((StringProperty) values.get(0)).addListener((observableValue, s, t1) -> event.run())));
        eventFactories.add(new WhenEventFactory("in %number% seconds:", "in %number% second(s|)", (state, values, event, args) -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(event::run); }
            }, ((Double) (((Double) values.get(0)) * 1000)).longValue()
        )));
        eventFactories.add(new WhenEventFactory("every %number% seconds:","every %number% second(s|)", (state, values, event, args) -> {
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(() -> Platform.runLater(event::run), (long) 0.1, ((Double) (((Double) values.get(0)) * 1000)).longValue(), TimeUnit.MILLISECONDS);
        }));

        eventFactories.add(new WhenEventFactory("when %stage% is closed", (state, values, event, args) -> ((Stage) values.get(0)).setOnHidden(windowEvent -> {
            addEventExpressions(windowEvent, event.getRunChunk());
            event.run();
        })));
        eventFactories.add(new WhenEventFactory("when %stage% is shown", (state, values, event, args) -> ((Stage) values.get(0)).setOnShown(windowEvent -> {
            addEventExpressions(windowEvent, event.getRunChunk());
            event.run();
        })));
        
        return eventFactories;
    }

    @Override
    public EnumMap<ExpressionPriority, HashMap<Class<?>, ArrayList<ExpressionFactory<?>>>> addExpressions(Collection<Class<?>> classes) {

        HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> lowest = new HashMap<>();
        HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> low = new HashMap<>();
        HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> medium = new HashMap<>();
        HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> high = new HashMap<>();
        HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> highest = new HashMap<>();
        EnumMap<ExpressionPriority, HashMap<Class<?>, ArrayList<ExpressionFactory<?>>>> end = new EnumMap<>(ExpressionPriority.class);
        end.put(ExpressionPriority.LOWEST, lowest);
        end.put(ExpressionPriority.LOW, low);
        end.put(ExpressionPriority.MEDIUM, medium);
        end.put(ExpressionPriority.HIGH, high);
        end.put(ExpressionPriority.HIGHEST, highest);
        for (Class<?> loopClass : classes) {
            lowest.put(loopClass, new ArrayList<>());
            low.put(loopClass, new ArrayList<>());
            medium.put(loopClass, new ArrayList<>());
            high.put(loopClass, new ArrayList<>());
            highest.put(loopClass, new ArrayList<>());
        }
        
        
        string: {
            high.get(String.class).add(new ExpressionFactory<>("value of %string-property%", (state, values, args) -> ((StringProperty) values.get(0)).get(), String.class));
            low.get(String.class).add(new ExpressionFactory<>("text of %button%", (state, values, args) -> ((Button) values.get(0)).getText(), String.class));
        }
        lists: {
            pane: {
                medium.get(LinkedList.class).add(new ExpressionFactory<>("children of %pane%", (state, values, args) ->
                        new LinkedList<>("children of " + values.get(0).toString(), false, ((Pane) values.get(0)).getChildren(), Node.class), LinkedList.class));
            }
        }
        stage: {
            medium.get(Stage.class).add(new ExpressionFactory<>("new stage", "(|new )stage", (state, values, args) -> new Stage(), Stage.class));
        }
        file: {
            high.get(File.class).add(new ExpressionFactory<>("load file from %string%", "(load file|file loaded) from %string%", (state, values, args) -> new File(values.get(0).toString()), File.class));
        }
        color: {
            low.get(Color.class).add(new ExpressionFactory<>("web color %string%", "web colo(u|)r %string%", (state, values, args) -> Color.valueOf(values.get(0).toString()), Color.class));

            low.get(Color.class).add(new ExpressionFactory<>("blue", ((state, values, args) -> Color.BLUE), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("red", ((state, values, args) -> Color.RED), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("green", ((state, values, args) -> Color.GREEN), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("yellow", ((state, values, args) -> Color.YELLOW), Color.class));

            low.get(Color.class).add(new ExpressionFactory<>("dark blue", ((state, values, args) -> Color.DARKBLUE), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("dark red", ((state, values, args) -> Color.DARKRED), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("dark green", ((state, values, args) -> Color.DARKGREEN), Color.class));

            low.get(Color.class).add(new ExpressionFactory<>("light blue", ((state, values, args) -> Color.LIGHTBLUE), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("light green", ((state, values, args) -> Color.LIGHTGREEN), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("light yellow", ((state, values, args) -> Color.LIGHTYELLOW), Color.class));

            low.get(Color.class).add(new ExpressionFactory<>("aqua", ((state, values, args) -> Color.AQUA), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("beige", ((state, values, args) -> Color.BEIGE), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("black", ((state, values, args) -> Color.BLACK), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("brown", ((state, values, args) -> Color.BROWN), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("cyan", ((state, values, args) -> Color.CYAN), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("dark grey", ((state, values, args) -> Color.DARKGREY), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("grey", ((state, values, args) -> Color.GREY), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("light grey", ((state, values, args) -> Color.LIGHTGREY), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("gold", ((state, values, args) -> Color.GOLD), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("indigo", ((state, values, args) -> Color.INDIGO), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("lime", ((state, values, args) -> Color.LIME), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("magenta", ((state, values, args) -> Color.MAGENTA), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("maroon", ((state, values, args) -> Color.MAROON), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("navy", ((state, values, args) -> Color.NAVY), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("purple", ((state, values, args) -> Color.PURPLE), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("silver", ((state, values, args) -> Color.SILVER), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("snow", ((state, values, args) -> Color.SNOW), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("white", ((state, values, args) -> Color.WHITE), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("teal", ((state, values, args) -> Color.TEAL), Color.class));
            low.get(Color.class).add(new ExpressionFactory<>("violet", ((state, values, args) -> Color.VIOLET), Color.class));
        }
        button: {
            medium.get(Button.class).add(new ExpressionFactory<>("new button", "(|new )button", (state, values, args) -> new Button(), Button.class));
        }
        label: {
            medium.get(Label.class).add(new ExpressionFactory<>("new label", "(|new )label", (state, values, args) -> new Label(), Label.class));
        }
        pane: {
            medium.get(Pane.class).add(new ExpressionFactory<>("new pane", "(|new )pane", (state, values, args) -> new Pane(), Pane.class));
        }
        vbox: {
            medium.get(VBox.class).add(new ExpressionFactory<>("new vbox", "(|new )vbox", (state, values, args) -> new VBox(), VBox.class));
        }
        hbox: {
            medium.get(HBox.class).add(new ExpressionFactory<>("new hbox", "(|new )hbox", (state, values, args) -> new HBox(), HBox.class));
        }
        stringProperty: {
            medium.get(StringProperty.class).add(new ExpressionFactory<>("title property of %stage%", (state, values, args) -> ((Stage) values.get(0)).titleProperty(), StringProperty.class));
            medium.get(StringProperty.class).add(new ExpressionFactory<>("text property of %button%", (state, values, args) -> ((Button) values.get(0)).textProperty(), StringProperty.class));
        }
        background: {
            medium.get(Background.class).add(new ExpressionFactory<>("new background colored %color% with corner radius %number%", "(|new )background colo(u|)red %color% with corner radius %number%", (state, values, args) -> new Background(new BackgroundFill((Color) values.get(0), new CornerRadii((Double) values.get(1)), Insets.EMPTY)), Background.class));
        }

        return end;
    }

    private static void addMouseEventExpressions(MouseEvent mouseEvent, CodeChunk state) {
        state.getLocalExpressions().clear();
        state.getLocalExpressions().add(new ExpressionFactory<>("mouse-button", (state1, values, args) -> mouseEvent.getButton(), MouseButton.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("click-count", (state1, values, args) -> mouseEvent.getClickCount(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("pick-result", (state1, values, args) -> mouseEvent.getPickResult(), PickResult.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("intersected-node", (state1, values, args) -> mouseEvent.getPickResult().getIntersectedNode(), Node.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("scene-x", (state1, values, args) -> mouseEvent.getSceneX(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("scene-y", (state1, values, args) -> mouseEvent.getSceneY(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("screen-x", (state1, values, args) -> mouseEvent.getScreenX(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("screen-y", (state1, values, args) -> mouseEvent.getScreenY(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("x", (state1, values, args) -> mouseEvent.getX(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("y", (state1, values, args) -> mouseEvent.getY(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("source", (state1, values, args) -> mouseEvent.getSource(), Object.class));
    }

    public static void addKeyEventExpressions(KeyEvent keyEvent, CodeChunk state) {
        state.getLocalExpressions().clear();
        state.getLocalExpressions().add(new ExpressionFactory<>("character", (state1, values, args) -> keyEvent.getCharacter(), String.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("key-code", (state1, values, args) -> keyEvent.getCode().toString(), String.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("text", (state1, values, args) -> keyEvent.getText(), String.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("source", (state1, values, args) -> keyEvent.getSource(), Object.class));
    }

    public static void addDragEventExpressions(DragEvent dragEvent, CodeChunk state) {
        state.getLocalExpressions().clear();
        // Not 100% complete
        state.getLocalExpressions().add(new ExpressionFactory<>("pick-result", (state1, values, args) -> dragEvent.getPickResult(), PickResult.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("intersected-node", (state1, values, args) -> dragEvent.getPickResult().getIntersectedNode(), Node.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("scene-y", (state1, values, args) -> dragEvent.getSceneY(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("scene-x", (state1, values, args) -> dragEvent.getSceneX(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("screen-y", (state1, values, args) -> dragEvent.getScreenY(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("screen-x", (state1, values, args) -> dragEvent.getScreenX(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("y", (state1, values, args) -> dragEvent.getY(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("x", (state1, values, args) -> dragEvent.getX(), Number.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("source", (state1, values, args) -> dragEvent.getSource(), Object.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("gesture-target", (state1, values, args) -> dragEvent.getGestureTarget(), Object.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("gesture-source", (state1, values, args) -> dragEvent.getGestureSource(), Object.class));
    }

    public static void addEventExpressions(Event event, CodeChunk state) {
        state.getLocalExpressions().clear();
        state.getLocalExpressions().add(new ExpressionFactory<>("source", (state1, values, args) -> event.getSource(), Object.class));
    }

}
