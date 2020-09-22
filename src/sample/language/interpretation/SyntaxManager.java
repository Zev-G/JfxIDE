package sample.language.interpretation;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
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
import sample.betterfx.Console;
import sample.language.FXScript;
import sample.language.interpretation.parse.Parser;
import sample.language.interpretation.run.CodeChunk;
import sample.language.interpretation.run.CodePiece;
import sample.language.interpretation.run.CodeState;
import sample.language.syntaxPiece.effects.Effect;
import sample.language.syntaxPiece.effects.EffectFactory;
import sample.language.syntaxPiece.events.Function;
import sample.language.syntaxPiece.events.WhenEventFactory;
import sample.language.syntaxPiece.expressions.ExpressionFactory;
import sample.language.syntaxPiece.expressions.ExpressionPriority;
import sample.language.variable.LinkedList;
import sample.language.variable.List;
import sample.language.variable.Variable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SyntaxManager {

    public static Console PRINT_CONSOLE = null;

    public static final HashMap<String, Class<?>> SUPPORTED_TYPES = new HashMap<>();

    public static final HashMap<ExpressionPriority, HashMap<Class<?>, ArrayList<ExpressionFactory<?>>>> EXPRESSIONS = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> LOWEST = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> LOW = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> MEDIUM = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> HIGH = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> HIGHEST = new HashMap<>();

    public static final ArrayList<EffectFactory> EFFECT_FACTORIES = new ArrayList<>();
    public static final ArrayList<WhenEventFactory> EVENT_FACTORIES = new ArrayList<>();

    public static void init() {

        // All
        // Supported Types

        // Not JavaFX
        SUPPORTED_TYPES.put("file", File.class);


        //JavaFX
        SUPPORTED_TYPES.put("stage", Stage.class);
        SUPPORTED_TYPES.put("window", Window.class);
        SUPPORTED_TYPES.put("color", Color.class);
        SUPPORTED_TYPES.put("node", Node.class);
        SUPPORTED_TYPES.put("parent", Parent.class);
        SUPPORTED_TYPES.put("button", Button.class);
        SUPPORTED_TYPES.put("label", Label.class);
        SUPPORTED_TYPES.put("pane", Pane.class);
        SUPPORTED_TYPES.put("region", Region.class);
        SUPPORTED_TYPES.put("vbox", VBox.class);
        SUPPORTED_TYPES.put("hbox", HBox.class);
        SUPPORTED_TYPES.put("duration", Duration.class);
        SUPPORTED_TYPES.put("background", Background.class);
        SUPPORTED_TYPES.put("string-property", StringProperty.class);
        SUPPORTED_TYPES.put("mouse-button", MouseButton.class);
        SUPPORTED_TYPES.put("pick-result", MouseButton.class);
        SUPPORTED_TYPES.put("grid-pane", GridPane.class);
        SUPPORTED_TYPES.put("image", Image.class);
        SUPPORTED_TYPES.put("image-view", ImageView.class);

        // Java
        SUPPORTED_TYPES.put("value", Object.class);
        SUPPORTED_TYPES.put("object", Object.class);
        SUPPORTED_TYPES.put("string", String.class);
        SUPPORTED_TYPES.put("number", Number.class);
        SUPPORTED_TYPES.put("boolean", Boolean.class);

        //Special
        SUPPORTED_TYPES.put("variable", Variable.class);
        SUPPORTED_TYPES.put("list", List.class);
        SUPPORTED_TYPES.put("linked-list", LinkedList.class);

        for (Class<?> loopClass : SUPPORTED_TYPES.values()) {
            HIGHEST.put(loopClass, new ArrayList<>());
            HIGH.put(loopClass, new ArrayList<>());
            MEDIUM.put(loopClass, new ArrayList<>());
            LOW.put(loopClass, new ArrayList<>());
            LOWEST.put(loopClass, new ArrayList<>());
        }

        EXPRESSIONS.put(ExpressionPriority.LOWEST, LOWEST);
        EXPRESSIONS.put(ExpressionPriority.LOW, LOW);
        EXPRESSIONS.put(ExpressionPriority.MEDIUM, MEDIUM);
        EXPRESSIONS.put(ExpressionPriority.HIGH, HIGH);
        EXPRESSIONS.put(ExpressionPriority.HIGHEST, HIGHEST);

        effects: {

            addons: {
                EFFECT_FACTORIES.add(new EffectFactory("run code in %file%", (state, values, args) -> {
                    File file = (File) values.get(0);
                    try {
                        SyntaxManager.getCodeChunkFromCode(file).run();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }));
                EFFECT_FACTORIES.add(new EffectFactory("import %file%","$import %file%", (state, values, args) -> {
                    File file = (File) values.get(0);
                    System.out.println("Running file: " + file);
                    try {
                        SyntaxManager.getCodeChunkFromCode(file).run();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }));
            }

            reflect: {
                EFFECT_FACTORIES.add(new EffectFactory("IGNORE", "%object%\\.(.*\\))", (state, values, args) -> {
                    String connectedArgs = appendAllArgs(new StringBuilder(), args).toString();
                    Object obj = values.get(0);
                    reflectMethod(state, connectedArgs, obj);
                }));
            }
            file: {
                EFFECT_FACTORIES.add(new EffectFactory("create new file %file%", (state, values, args) -> {
                    try {
                        ((File) values.get(0)).createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
                EFFECT_FACTORIES.add(new EffectFactory("create new directory %file%", (state, values, args) -> ((File) values.get(0)).mkdir()));
                EFFECT_FACTORIES.add(new EffectFactory("delete %file%", (state, values, args) -> ((File) values.get(0)).delete()));
                EFFECT_FACTORIES.add(new EffectFactory("move %file% to %string%", (state, values, args) -> ((File) values.get(0)).renameTo(new File(values.get(1).toString()))));
                EFFECT_FACTORIES.add(new EffectFactory("write %string% to %file%", (state, values, args) -> {
                    File f = (File) values.get(1);
                    if (!f.exists()) {
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            return;
                        }
                    }
                    try {
                        FileWriter fileWriter = new FileWriter(f.getPath());
                        fileWriter.write(values.get(0).toString());
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
            }

            EFFECT_FACTORIES.add(new EffectFactory("IGNORE", "([A-z]+)\\((.*?)\\)", (state, values, args) -> {
                String connectedArgs = appendAllArgs(new StringBuilder(), args).toString();
                Function<?> function = Function.ALL_FUNCTIONS.get(connectedArgs.split("\\(")[0]);
                function.invoke();
            }));
            EFFECT_FACTORIES.add(new EffectFactory("return %object%", (state, values, args) -> {
                state.setReturnedObject(values.get(0));
                ((CodeChunk) state).setFinished(true);
            }));
            EFFECT_FACTORIES.add(new EffectFactory("set %variable% to %object%", (state, values, args) -> state.setVariableValue(
                    ((Variable<?>) values.get(0)).getName(),
                    values.get(1))));
            EFFECT_FACTORIES.add(new EffectFactory("IGNORE", "[^\\s]+(:| =) %object%", (state, values, args) -> {
                String varName = args[0].replaceAll(":", "").replaceAll("=", "").replaceAll("[{}]", "");
                state.setVariableValue(varName, values.get(0));
            }));
            EFFECT_FACTORIES.add(new EffectFactory("remove %object% from %list%", (state, values, args) -> ((List) values.get(1)).removeObject(values.get(0))));
            EFFECT_FACTORIES.add(new EffectFactory("add %object% to %list%", (state, values, args) -> ((List) values.get(1)).add(values.get(0), values.get(0).toString())));

            EFFECT_FACTORIES.add(new EffectFactory("set title of %stage% to %string%",
                    (state, values, args) -> ((Stage) values.get(0)).setTitle((String) values.get(1))));
            EFFECT_FACTORIES.add(new EffectFactory("show %stage%", (state, values, args) -> ((Stage) values.get(0)).show()));
            EFFECT_FACTORIES.add(new EffectFactory("print %object%", (state, values, args) -> System.out.println(values.get(0))));
            EFFECT_FACTORIES.add(new EffectFactory("stop program", (state, values, args) -> Platform.exit()));
            EFFECT_FACTORIES.add(new EffectFactory("set fill color of %stage% to %color%", (state, values, args) -> {
                Stage stage = (Stage) values.get(0);
                if (stage.getScene() != null) {
                    stage.getScene().setFill((Color) values.get(1));
                }
            }));
            EFFECT_FACTORIES.add(new EffectFactory("put %parent% into %stage%", (state, values, args) -> {
                Stage stage = (Stage) values.get(1);
                stage.setScene(new Scene((Parent) values.get(0)));
            }));
            EFFECT_FACTORIES.add(new EffectFactory("set text of %button% to %string%", (state, values, args) -> ((Button) values.get(0)).setText(values.get(1).toString())));
            EFFECT_FACTORIES.add(new EffectFactory("set background color of %region% to %color%", (state, values, args) -> {
                Region region = ((Region) values.get(0));
                BackgroundFill currentFill = new BackgroundFill(null, new CornerRadii(3), Insets.EMPTY);
                if (region.getBackground() != null && region.getBackground().getFills() != null && !region.getBackground().getFills().isEmpty()) {
                    currentFill = region.getBackground().getFills().get(0);
                }
                region.setBackground(new Background(new BackgroundFill((Color) values.get(1), currentFill.getRadii(), currentFill.getInsets())));
            }));
            EFFECT_FACTORIES.add(new EffectFactory("set scale x of %node% to %number%", (state, values, args) -> ((Node) values.get(0)).setScaleX((Double) values.get(1))));
            EFFECT_FACTORIES.add(new EffectFactory("set scale y of %node% to %number%", (state, values, args) -> ((Node) values.get(0)).setScaleY((Double) values.get(1))));
            EFFECT_FACTORIES.add(new EffectFactory("set scale of %node% to %number%", (state, values, args) -> {
                ((Node) values.get(0)).setScaleY((Double) values.get(1));
                ((Node) values.get(0)).setScaleX((Double) values.get(1));
            }));
            EFFECT_FACTORIES.add(new EffectFactory("set background of %region% to %background%", (state, values, args) -> ((Region) values.get(0)).setBackground((Background) values.get(1))));
            EFFECT_FACTORIES.add(new EffectFactory("push %node% to front", (state, values, args) -> ((Node) values.get(0)).toFront()));
            EFFECT_FACTORIES.add(new EffectFactory("push %node% to back", (state, values, args) -> ((Node) values.get(0)).toBack()));
            EFFECT_FACTORIES.add(new EffectFactory("set rotation of %node% to %number%", (state, values, args) -> ((Node) values.get(0)).setRotate((double) (Number) values.get(1))));
            EFFECT_FACTORIES.add(new EffectFactory("set style of %node% to %string%", (state, values, args) -> ((Node) values.get(0)).setStyle(values.get(1).toString())));
            EFFECT_FACTORIES.add(new EffectFactory("set opacity of %node% to %number%", (state, values, args) -> ((Node) values.get(0)).setOpacity((double) (Number) values.get(1))));
            EFFECT_FACTORIES.add(new EffectFactory("disable %node%", (state, values, args) -> ((Node) values.get(0)).setDisable(true)));
            EFFECT_FACTORIES.add(new EffectFactory("enable %node%", (state, values, args) -> ((Node) values.get(0)).setDisable(false)));
            EFFECT_FACTORIES.add(new EffectFactory("set value of %string-property% to %string%", (state, values, args) -> ((StringProperty) values.get(0)).set(values.get(1).toString())));
            EFFECT_FACTORIES.add(new EffectFactory("bind %string-property% to %string-property%", (state, values, args) -> ((StringProperty) values.get(0)).bind(((StringProperty) values.get(1)))));
            EFFECT_FACTORIES.add(new EffectFactory("transition scale of %node% to %number% over %number% second(s|)", (state, values, args) -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
                scaleTransition.setToX((double) (Number) values.get(1));
                scaleTransition.setToY((double) (Number) values.get(1));
                scaleTransition.play();
            }));
            EFFECT_FACTORIES.add(new EffectFactory("transition x position of %node% to %number% over %number% seconds","transition x position of %node% to %number% over %number% second(s|)", (state, values, args) -> {
                TranslateTransition scaleTransition = new TranslateTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
                scaleTransition.setToX((double) (Number) values.get(1));
                scaleTransition.play();
            }));
            EFFECT_FACTORIES.add(new EffectFactory("transition y position of %node% to %number% over %number% seconds","transition y position of %node% to %number% over %number% second(s|)", (state, values, args) -> {
                TranslateTransition scaleTransition = new TranslateTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
                scaleTransition.setToY((double) (Number) values.get(1));
                scaleTransition.play();
            }));
            EFFECT_FACTORIES.add(new EffectFactory("transition position of %node% to %number% over %number% seconds","transition position of %node% to %number% over %number% second(s|)", (state, values, args) -> {
                TranslateTransition scaleTransition = new TranslateTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
                scaleTransition.setToX((double) (Number) values.get(1));
                scaleTransition.setToY(((Number) values.get(1)).doubleValue());
                scaleTransition.play();
            }));
            EFFECT_FACTORIES.add(new EffectFactory("transition opacity of %node% to %number% over %number% seconds","transition opacity of %node% to %number% over %number% second(s|)", (state, values, args) -> {
                FadeTransition scaleTransition = new FadeTransition(Duration.seconds((double) (Number) values.get(2)), (Node) values.get(0));
                scaleTransition.setToValue((double) (Number) values.get(1));
                scaleTransition.play();
            }));
            EFFECT_FACTORIES.add(new EffectFactory("full screen %stage%", "(|set )full screen %stage%", (state, values, args) -> ((Stage) values.get(0)).setFullScreen(true)));
            EFFECT_FACTORIES.add(new EffectFactory("unfull screen %stage%", "(|set )(normal|unfull) screen %stage%", (state, values, args) -> ((Stage) values.get(0)).setFullScreen(false)));
            EFFECT_FACTORIES.add(new EffectFactory("maximize %stage%", "(|set )maximize %stage%", (state, values, args) -> ((Stage) values.get(0)).setMaximized(true)));
            EFFECT_FACTORIES.add(new EffectFactory("un maximize %stage%", "(|set )un maximize %stage%", (state, values, args) -> ((Stage) values.get(0)).setMaximized(false)));
            EFFECT_FACTORIES.add(new EffectFactory("make %stage% resizable", (state, values, args) -> ((Stage) values.get(0)).setResizable(true)));
            EFFECT_FACTORIES.add(new EffectFactory("make %stage% not resizable", (state, values, args) -> ((Stage) values.get(0)).setResizable(true)));
            EFFECT_FACTORIES.add(new EffectFactory("hide %window%", (state, values, args) -> ((Window) values.get(0)).hide()));
            EFFECT_FACTORIES.add(new EffectFactory("set icon of %stage% to %image%", (state, values, args) -> ((Stage) values.get(0)).getIcons().add((Image) values.get(1))));
            EFFECT_FACTORIES.add(new EffectFactory("make %node% visible", (state, values, args) -> ((Node) values.get(0)).setVisible(true)));
            EFFECT_FACTORIES.add(new EffectFactory("make %node% invisible", (state, values, args) -> ((Node) values.get(0)).setVisible(false)));
            EFFECT_FACTORIES.add(new EffectFactory("make %node% mouse transparent", (state, values, args) -> ((Node) values.get(0)).setMouseTransparent(true)));
            EFFECT_FACTORIES.add(new EffectFactory("make %node% not mouse transparent", (state, values, args) -> ((Node) values.get(0)).setMouseTransparent(false)));

            EFFECT_FACTORIES.add(new EffectFactory("set spacing of vbox %vbox% to %number%", (state, values, args) -> ((VBox) values.get(0)).setSpacing(((Number) values.get(1)).doubleValue())));
            EFFECT_FACTORIES.add(new EffectFactory("set spacing of hbox %hbox% to %number%", (state, values, args) -> ((HBox) values.get(0)).setSpacing(((Number) values.get(1)).doubleValue())));
        }
        events: {
            // Mouse Events
            EVENT_FACTORIES.add(new WhenEventFactory("when %node% is pressed:", "(when|on) %node% is pressed", (state, values, event, args) -> ((Node) values.get(0)).setOnMousePressed(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when %node% is clicked:","(when|on) %node% is clicked", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseClicked(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when mouse drag enters %node%:","(when|on) mouse drag enters %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseDragEntered(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when mouse drag exits %node%:","(when|on) mouse drag exits %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseDragExited(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when mouse moves over %node%:","(when|on) mouse moves over %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseMoved(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when %node% is released:","(when|on) %node% is released", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseReleased(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when drag is detected for %node%:","(when|on) drag is detected for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragDetected(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when mouse enters %node%:","(when|on) mouse enters %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseEntered(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when mouse exits %node%:", "(when|on) mouse exits %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnMouseExited(mouseEvent -> { addMouseEventExpressions(mouseEvent, event.getRunChunk()); event.run(); })));
            // Drag Events
            EVENT_FACTORIES.add(new WhenEventFactory("when drag is done for %node%:","(when|on) drag is done for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragDone(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when drag is dropped on %node%:","(when|on) drag is dropped on %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragDropped(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when drag enters %node%:","(when|on) drag enters %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragEntered(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when drag exits %node%:","(when|on) drag exits %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragExited(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when drag is over %node%:","(when|on) drag is over %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnDragOver(dragEvent -> { addDragEventExpressions(dragEvent, event.getRunChunk()); event.run(); })));
            // Key Events
            EVENT_FACTORIES.add(new WhenEventFactory("when key is pressed for %node%:","(when|on) key is pressed for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnKeyPressed(keyEvent -> { addKeyEventExpressions(keyEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when key is released for %node%:","(when|on) key is released for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnKeyReleased(keyEvent -> { addKeyEventExpressions(keyEvent, event.getRunChunk()); event.run(); })));
            EVENT_FACTORIES.add(new WhenEventFactory("when key is typed for %node%:","(when|on) key is typed for %node%", (state, values, event, args) -> ((Node) values.get(0)).setOnKeyTyped(keyEvent -> { addKeyEventExpressions(keyEvent, event.getRunChunk()); event.run(); })));

            EVENT_FACTORIES.add(new WhenEventFactory("when %string-property% changes:","(when|on) %string-property% changes", (state, values, event, args) -> ((StringProperty) values.get(0)).addListener((observableValue, s, t1) -> event.run())));

            special: {
                EVENT_FACTORIES.add(new WhenEventFactory("expression %type% -> %text%:","$expression [^\\s]+? -> (.*?)", (state, values, event, args) -> {
                    StringBuilder builder = new StringBuilder();
                    appendAllArgs(builder, args);
                    String returnType = args[1];
                    Class<?> returnClass = SUPPORTED_TYPES.get(returnType);
                    if (returnClass == null) {
                        System.err.println("Can't add expression for text: " + builder.toString() + " because type: " + returnType + " isn't a valid type. Valid types are: " + SUPPORTED_TYPES.keySet());
                    } else {
                        ExpressionFactory<Object> expressionFactory = new ExpressionFactory<>(builder.toString().replaceFirst("expression ", "").replaceFirst("(.*?) -> ", ""), (state1, values1, args1) -> {
                            event.getRunChunk().getLocalExpressions().clear();
                            int i = 0;
                            for (Object obj : values1) {
                                i++;
                                event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>(String.valueOf(i), (state2, values2, args2) -> obj, Object.class));
                            }
                            event.run();
                            return event.getRunChunk().getReturnedObject();
                        }, Object.class);
                        LOW.get(returnClass).add(expressionFactory);
                    }
                }));
                EVENT_FACTORIES.add(new WhenEventFactory("effect -> %text%:","$effect -> (.*?)", (state, values, event, args) -> {
                    StringBuilder builder = new StringBuilder();
                    appendAllArgs(builder, args);
                    EffectFactory effectFactory = new EffectFactory(builder.toString().replaceFirst("effect -> ", ""), (state1, values1, args1) -> {
                        event.getRunChunk().getLocalExpressions().clear();
                        int i = 0;
                        for (Object obj : values1) {
                            i++;
                            event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>(String.valueOf(i), (state2, values2, args2) -> obj, Object.class));
                        }
                        event.run();
                    });
                    EFFECT_FACTORIES.add(effectFactory);
                }));
                EVENT_FACTORIES.add(new WhenEventFactory("in %number% seconds:", "in %number% second(s|)", (state, values, event, args) -> new Timer().schedule(new TimerTask() {
                                         @Override
                                         public void run() {
                                             Platform.runLater(event::run);
                                         }
                                     }, ((Double) (((Double) values.get(0)) * 1000)).longValue()
                )));
                EVENT_FACTORIES.add(new WhenEventFactory("every %number% seconds:","every %number% second(s|)", (state, values, event, args) -> {
                    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
                    executorService.scheduleAtFixedRate(() -> Platform.runLater(event::run), (long) 0.1, ((Double) (((Double) values.get(0)) * 1000)).longValue(), TimeUnit.MILLISECONDS);
                }));
                EVENT_FACTORIES.add(new WhenEventFactory("chance of %number%", (state, values, event, args) -> {
                    double value = Math.random();
                    if (value < (double) values.get(0)) {
                        event.run();
                    }
                }));
                EVENT_FACTORIES.add(new WhenEventFactory("loop %number% times", (state, values, event, args) -> {
                    double times = ((Number) values.get(0)).doubleValue();
                    for (int i = 0; i < times; i++) {
                        event.getRunChunk().getLocalExpressions().clear();
                        int finalI = i;
                        event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>("num(ber|)", (state1, values1, args1) -> finalI, Number.class));
                        event.run();
                    }
                }));
                EVENT_FACTORIES.add(new WhenEventFactory("loop %list%", (state, values, event, args) -> {
                    List list = (List) values.get(0);
                    int loopTimes = 0;
                    for (Map.Entry<String, Object> entry : list.getValues().entrySet()) {
                        event.getRunChunk().getLocalExpressions().clear();
                        int finalLoopTimes = loopTimes;
                        event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>("num(ber|)", (state1, values1, args1) -> finalLoopTimes, Number.class));
                        event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>("index", (state1, values1, args1) -> entry.getKey(), String.class));
                        event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>("item", (state1, values1, args1) -> entry.getValue(), Object.class));
                        event.run();
                        loopTimes++;
                    }
                }));
            }
        }
        expressions: {
            java: {
                string: {
                    HIGHEST.get(String.class).add(new ExpressionFactory<>("IGNORE", "\"([^\\\"]*?)\"", (state, values, args) -> {
                        StringBuilder builder = new StringBuilder();
                        appendAllArgs(builder, args);
                        return builder.toString().replaceAll("\"", "");
                    }, String.class));
                    HIGHEST.get(String.class).add(new ExpressionFactory<>("%string% appended to %string%", (state, values, args) -> values.get(0).toString() + values.get(1), String.class));
                    HIGH.get(String.class).add(new ExpressionFactory<>("value of %string-property%", (state, values, args) -> ((StringProperty) values.get(0)).get(), String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("%string% without the last character", "%string% without( the|) last character", (state, values, args) -> {
                        if (values.get(0).toString().length() < 1) return values.get(0).toString();
                        return values.get(0).toString().substring(0, values.get(0).toString().length() - 1);
                    }, String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("%string% without the first character", "%string% without( the|) first character", (state, values, args) -> {
                        if (values.get(0).toString().length() < 1) return values.get(0).toString();
                        return values.get(0).toString().substring(1);
                    }, String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("%string% without character at %number%", "%string% without character (at |)%number%", (state, values, args) -> {
                        String arg1 = values.get(0).toString();
                        int arg2 = ((Double) values.get(1)).intValue();
                        return arg1.substring(0, arg2) + (arg1.length() > arg2 + 1 ? arg1.substring(0, arg2 + 1) : "");
                    }, String.class));
//                    MEDIUM.get(String.class).add(new ExpressionFactory<>("text of %button%", (state, values, args) -> ((Button) values.get(0)).getText(), String.class));
//                    MEDIUM.get(String.class).add(new ExpressionFactory<>("title of %stage%", (state, values, args) -> ((Stage) values.get(0)).getTitle(), String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("text of %button%", (state, values, args) -> ((Button) values.get(0)).getText(), String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("path of %file%", (state, values, args) -> ((File) values.get(0)).getAbsolutePath(), String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("text in %file%", (state, values, args) -> {
                        File f = (File) values.get(0);
                        if (f == null || !f.exists()) {
                            System.err.println("Can't read from file that doesn't exist, file: " + f);
                            return "INVALID FILE";
                        }
                        try {
                            Scanner sc = new Scanner(f);
                            StringBuilder text = new StringBuilder();
                            int i = 0;
                            while (sc.hasNextLine()) {
                                text.append(sc.nextLine()).append(i == 0 ? "" : "\n");
                                i++;
                            }
                            return text.toString();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        System.err.println("Couldn't read file: " + f);
                        return "COULDN'T READ FILE";
                    }, String.class));
                }
                number: {
                    HIGHEST.get(Number.class).add(new ExpressionFactory<>("IGNORE", "([0-9]+)(|\\.([0-9]+))", (state, values, args) -> {
                        StringBuilder builder = new StringBuilder();
                        appendAllArgs(builder, args);
                        return Double.parseDouble(builder.toString().replaceAll("@", ""));
                    }, Number.class));
                    HIGH.get(Number.class).add(new ExpressionFactory<>("IGNORE", "%number% \\+ %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() + ((Number) values.get(1)).doubleValue(), Number.class));
                    HIGH.get(Number.class).add(new ExpressionFactory<>("IGNORE", "%number% - %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() - ((Number) values.get(1)).doubleValue(), Number.class));
                    HIGH.get(Number.class).add(new ExpressionFactory<>("IGNORE", "%number% \\* %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() * ((Number) values.get(1)).doubleValue(), Number.class));
                    HIGH.get(Number.class).add(new ExpressionFactory<>("IGNORE", "%number% \\/ %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() / ((Number) values.get(1)).doubleValue(), Number.class));

                    MEDIUM.get(Number.class).add(new ExpressionFactory<>("random number between %number% and %number%", (state, values, args) -> {
                        double firstValue = (double) values.get(0);
                        double secondValue = (double) values.get(1);
                        if (!(firstValue < secondValue)) {
                            System.err.println("Attempted to get a random number between " + firstValue + " and " + secondValue + " but the second value is not larger than the first value.");
                            return -1;
                        }
                        return ThreadLocalRandom.current().nextDouble(firstValue, secondValue);
                    }, Number.class));
                    MEDIUM.get(Number.class).add(new ExpressionFactory<>("random integer between %number% and %number%", (state, values, args) -> {
                        int firstValue = (int) Math.round((double) values.get(0));
                        int secondValue = (int) Math.round((double) values.get(1));
                        if (!(firstValue < secondValue)) {
                            System.err.println("Attempted to get a random number between " + firstValue + " and " + secondValue + " but the second value is not larger than the first value.");
                            return -1;
                        }
                        return ThreadLocalRandom.current().nextInt(firstValue, secondValue + 1);
                    }, Number.class));
                    LOW.get(Number.class).add(new ExpressionFactory<>("length of %string%", (state, values, args) -> values.get(0).toString().length(), Number.class));
                    LOW.get(Number.class).add(new ExpressionFactory<>("space taken up by %file%", (state, values, args) -> Long.valueOf(((File) values.get(0)).getTotalSpace()).doubleValue(), Number.class));
                    LOW.get(Number.class).add(new ExpressionFactory<>("size of %list%", ((state, values, args) -> ((List) values.get(0)).getValues().values().size()), Number.class));
                }
                bool: {
                    HIGHEST.get(Boolean.class).add(new ExpressionFactory<>("true", (state, values, args) -> true, Boolean.class));
                    HIGHEST.get(Boolean.class).add(new ExpressionFactory<>("false", (state, values, args) -> false, Boolean.class));
                    HIGHEST.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%object% == %object%", (state, values, args) -> values.get(0).equals(values.get(1)), Boolean.class));
                    HIGHEST.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%object% != %object%", (state, values, args) -> !values.get(0).equals(values.get(1)), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%number% > %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() > ((Number) values.get(1)).doubleValue(), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%number% < %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() < ((Number) values.get(1)).doubleValue(), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%number% >= %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() >= ((Number) values.get(1)).doubleValue(), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%number% <= %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() <= ((Number) values.get(1)).doubleValue(), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("%number% is a multiple of %number%", (state, values, args) -> (double) ((Number) values.get(0)) % (double) ((Number) values.get(1)) == 0, Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("\\!%boolean%", (state, values, args) -> !((Boolean) values.get(0)), Boolean.class));
                    LOW.get(Boolean.class).add(new ExpressionFactory<>("computer is connected to the internet", (state, values, args) -> {
                        try {
                            final URL url = new URL("http://www.google.com");
                            final URLConnection conn = url.openConnection();
                            conn.connect();
                            conn.getInputStream().close();
                            return true;
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            return false;
                        }
                    }, Boolean.class));
                    LOW.get(Boolean.class).add(new ExpressionFactory<>("free space in %file%", (state, values, args) -> ((File) values.get(0)).exists(), Boolean.class));
                }
            }
            special: {
                variable: {
                    HIGHEST.get(Variable.class).add(new ExpressionFactory<>("IGNORE", "\\{([^\\s]*?)\\}", (state, values, args) -> {
                        StringBuilder builder = new StringBuilder();
                        appendAllArgs(builder, args);
                        String variableName = builder.toString().replaceAll("[{}]", "");
                        if (CodeChunk.printing) System.out.println("Getting Variable: " + variableName + " State: " + state + " children: " + state.getChildren());
                        if (state.getVariableByName(variableName) == null) state.setVariableValue(variableName, null);
                        return state.getVariableByName(variableName);
                    }, Variable.class));
                }
                function: {
                    HIGHEST.get(Object.class).add(new ExpressionFactory<>("IGNORE", "([A-z]+)\\((.*?)\\)", (state, values, args) -> {
                        String connectedArgs = appendAllArgs(new StringBuilder(), args).toString();
                        String[] arguments = connectedArgs.split("\\(")[1].replaceAll("\\)", "").split(",( *)");
                        ArrayList<Object> objects = new ArrayList<>();
                        for (String argument : arguments) {
                            if (argument.length() > 0) {
                                ExpressionFactory<?> expressionFactory = FXScript.PARSER.parseExpression(argument);
                                expressionFactory.setState((CodeChunk) state);
                                expressionFactory.forChildren(expressionFactory1 -> expressionFactory1.setState(expressionFactory.getState()));
                                objects.add(expressionFactory.activate());
                            }
                        }
                        Function<?> function = Function.ALL_FUNCTIONS.get(connectedArgs.split("\\(")[0]);
                        return function.invoke(objects.toArray());
                    }, Object.class));
                }
                reflect: {
                    MEDIUM.get(Object.class).add(new ExpressionFactory<>("IGNORE", "%object%\\.(.*\\))", (state, values, args) -> {
                        String connectedArgs = appendAllArgs(new StringBuilder(), args).toString();
                        Object obj = values.get(0);
                        return reflectMethod(state, connectedArgs, obj);
                    }, Object.class));
                }
                loopOrEventValue: {
                    ExpressionFactory<?> loopOrEventValue = new ExpressionFactory<>("IGNORE", "(event|loop|expression|effect|arg|value)-(([^\\s]|-)*)", (state, values, args) -> {
                        String connectedArgs = appendAllArgs(new StringBuilder(), args).toString().split("-")[1];
                        for (ExpressionFactory<?> expression : state.getLocalExpressions()) {
                            if (connectedArgs.matches(expression.getRegex())) {
                                ExpressionFactory<?> dupedExpression = expression.duplicate();
                                dupedExpression.setCode(connectedArgs);
                                return dupedExpression.activate();
                            }
                        }
                        System.err.println("Invalid event value: " + connectedArgs + ". No local expression matches. Tried for: " + state.getLocalExpressions());
                        return null;
                    }, Object.class);
                    HIGHEST.get(Object.class).add(loopOrEventValue);
                }
            }
            lists: {
                pane: {
                    MEDIUM.get(LinkedList.class).add(new ExpressionFactory<>("children of %pane%", (state, values, args) ->
                            new LinkedList<>("children of " + values.get(0).toString(), false, ((Pane) values.get(0)).getChildren(), Node.class), LinkedList.class));
                }
                file: {
                    MEDIUM.get(List.class).add(new ExpressionFactory<>("files in %file%", (state, values, args) -> List.fromList(Objects.requireNonNull(((File) values.get(0)).listFiles())), List.class));
                }
            }
            stage: {
                MEDIUM.get(Stage.class).add(new ExpressionFactory<>("new stage", "(|new )stage", (state, values, args) -> new Stage(), Stage.class));
            }
            file: {
                HIGH.get(File.class).add(new ExpressionFactory<>("load file from %string%", "(load file|file loaded) from %string%", (state, values, args) -> new File(values.get(0).toString()), File.class));
            }
            color: {
                LOW.get(Color.class).add(new ExpressionFactory<>("web color %string%", "web colo(u|)r %string%", (state, values, args) -> Color.valueOf(values.get(0).toString()), Color.class));

                LOW.get(Color.class).add(new ExpressionFactory<>("blue", ((state, values, args) -> Color.BLUE), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("red", ((state, values, args) -> Color.RED), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("green", ((state, values, args) -> Color.GREEN), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("yellow", ((state, values, args) -> Color.YELLOW), Color.class));

                LOW.get(Color.class).add(new ExpressionFactory<>("dark blue", ((state, values, args) -> Color.DARKBLUE), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("dark red", ((state, values, args) -> Color.DARKRED), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("dark green", ((state, values, args) -> Color.DARKGREEN), Color.class));

                LOW.get(Color.class).add(new ExpressionFactory<>("light blue", ((state, values, args) -> Color.LIGHTBLUE), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("light green", ((state, values, args) -> Color.LIGHTGREEN), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("light yellow", ((state, values, args) -> Color.LIGHTYELLOW), Color.class));

                LOW.get(Color.class).add(new ExpressionFactory<>("aqua", ((state, values, args) -> Color.AQUA), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("beige", ((state, values, args) -> Color.BEIGE), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("black", ((state, values, args) -> Color.BLACK), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("brown", ((state, values, args) -> Color.BROWN), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("cyan", ((state, values, args) -> Color.CYAN), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("dark grey", ((state, values, args) -> Color.DARKGREY), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("grey", ((state, values, args) -> Color.GREY), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("light grey", ((state, values, args) -> Color.LIGHTGREY), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("gold", ((state, values, args) -> Color.GOLD), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("indigo", ((state, values, args) -> Color.INDIGO), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("lime", ((state, values, args) -> Color.LIME), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("magenta", ((state, values, args) -> Color.MAGENTA), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("maroon", ((state, values, args) -> Color.MAROON), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("navy", ((state, values, args) -> Color.NAVY), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("purple", ((state, values, args) -> Color.PURPLE), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("silver", ((state, values, args) -> Color.SILVER), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("snow", ((state, values, args) -> Color.SNOW), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("white", ((state, values, args) -> Color.WHITE), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("teal", ((state, values, args) -> Color.TEAL), Color.class));
                LOW.get(Color.class).add(new ExpressionFactory<>("violet", ((state, values, args) -> Color.VIOLET), Color.class));
            }
            button: {
                MEDIUM.get(Button.class).add(new ExpressionFactory<>("new button", "(|new )button", (state, values, args) -> new Button(), Button.class));
            }
            label: {
                MEDIUM.get(Label.class).add(new ExpressionFactory<>("new label", "(|new )label", (state, values, args) -> new Label(), Label.class));
            }
            pane: {
                MEDIUM.get(Pane.class).add(new ExpressionFactory<>("new pane", "(|new )pane", (state, values, args) -> new Pane(), Pane.class));
            }
            vbox: {
                MEDIUM.get(VBox.class).add(new ExpressionFactory<>("new vbox", "(|new )vbox", (state, values, args) -> new VBox(), VBox.class));
            }
            hbox: {
                MEDIUM.get(HBox.class).add(new ExpressionFactory<>("new hbox", "(|new )hbox", (state, values, args) -> new HBox(), HBox.class));
            }
            stringProperty: {
                MEDIUM.get(StringProperty.class).add(new ExpressionFactory<>("title property of %stage%", (state, values, args) -> ((Stage) values.get(0)).titleProperty(), StringProperty.class));
                MEDIUM.get(StringProperty.class).add(new ExpressionFactory<>("text property of %button%", (state, values, args) -> ((Button) values.get(0)).textProperty(), StringProperty.class));
            }
            background: {
                MEDIUM.get(Background.class).add(new ExpressionFactory<>("new background colored %color% with corner radius %number%", "(|new )background colo(u|)red %color% with corner radius %number%", (state, values, args) -> new Background(new BackgroundFill((Color) values.get(0), new CornerRadii((Double) values.get(1)), Insets.EMPTY)), Background.class));
            }
        }


    }

    /**
     * This is in it's own method because it is easily the most complex effect/expression. And being used twice it doesn't make sense for it to be in it's respective expression and effect.
     */
    private static Object reflectMethod(CodeState state, String connectedArgs, Object obj) {
        String allMethodsInText = connectedArgs.replaceFirst("(.*?)\\.", "");
        ArrayList<String> methods = new ArrayList<>();
        StringBuilder currentPiece = new StringBuilder();
        int parenthesesDepth = 0;
        for (char c : allMethodsInText.toCharArray()) {
            if (c == '(') parenthesesDepth++;
            else if (c == ')') parenthesesDepth--;
            else if (c == '.' && parenthesesDepth <= 0) {
                methods.add(currentPiece.toString());
                currentPiece = new StringBuilder();
                continue;
            }
            currentPiece.append(c);
        }
        if (currentPiece.toString().length() > 0 && !currentPiece.toString().equals(",")) methods.add(currentPiece.toString());
        Object currentObj = obj;
        for (String methodText : methods) {
            Object nextObj = getMethodFromString(methodText, currentObj.getClass(), state, currentObj);
            if (nextObj == null) return currentObj;
            currentObj = nextObj;
        }
        return currentObj;
    }

    private static Object getMethodFromString(String methodText, Class<?> objectClass, CodeState state, Object invokeFor) {
        String methodName = methodText.split("\\(")[0];
        String params = methodText.replaceFirst("(.*?)\\(", "");
        params = params.substring(0, params.length() - 1);
        ArrayList<Object> methodArgs = new ArrayList<>();
        ArrayList<Class<?>> methodArgClasses = new ArrayList<>();
        ArrayList<String> argumentStrings = new ArrayList<>();
        int inParentheses = 0;
        StringBuilder currentPiece = new StringBuilder();
        for (char c : params.toCharArray()) {
            if (c == '(') inParentheses++;
            else if (c == ')') inParentheses--;
            else if (c == ',' && inParentheses <= 0) {
                argumentStrings.add(currentPiece.toString());
                currentPiece = new StringBuilder();
                continue;
            }
            currentPiece.append(c);
        }
        java.util.List<Method> methods = Arrays.stream(objectClass.getMethods()).filter(method -> method.getName().equals(methodName) && method.getParameters().length == argumentStrings.size() + 1).collect(Collectors.toList());
        if (currentPiece.toString().length() > 0 && !currentPiece.toString().equals(",")) argumentStrings.add(currentPiece.toString());
        int i = 0;
        for (String argument : argumentStrings) {
            argument = argument.trim();
            System.out.println("On argument: (" + argument + ")");
            if (argument.length() > 0) {
                ExpressionFactory<?> expressionFactory = FXScript.PARSER.parseExpression(argument);
                expressionFactory.setState((CodeChunk) state);
                expressionFactory.forChildren(expressionFactory1 -> expressionFactory1.setState(expressionFactory.getState()));
                Object objFromExpression = expressionFactory.activate();
                if (objFromExpression.getClass() == Double.class || objFromExpression.getClass() == Integer.class) {
                    boolean isDouble = objFromExpression.toString().matches("([0-9]+)\\.([1-9]([0-9]+|)|[0-9]+[0-9]+)");
                    if (methods.size() == 1) {
                        Parameter param = methods.get(0).getParameters()[i];
                        isDouble = param.getType() != int.class;
                    }
                    if (isDouble) {
                        methodArgs.add(((Number) objFromExpression).doubleValue());
                        methodArgClasses.add(double.class);
                    } else {
                        methodArgs.add(((Number) objFromExpression).intValue());
                        methodArgClasses.add(int.class);
                    }
                } else if (objFromExpression.getClass() == Variable.class) {
                    Object add = ((Variable<?>) expressionFactory.activate()).getValue();
                    methodArgs.add(add);
                    methodArgClasses.add(add.getClass());
                } else {
                    methodArgs.add(expressionFactory.activate());
                    methodArgClasses.add(objFromExpression.getClass());
                }
            }
            i++;
        }
        try {
            return objectClass.getMethod(methodName, methodArgClasses.toArray(new Class[0])).invoke(invokeFor, methodArgs.toArray());
        } catch (NoSuchMethodException e) {
            System.err.println("There is no method named: " + methodName + " that accepts arguments: " + methodArgClasses);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void addMouseEventExpressions(MouseEvent mouseEvent, CodeState state) {
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

    public static void addKeyEventExpressions(KeyEvent keyEvent, CodeState state) {
        state.getLocalExpressions().clear();
        state.getLocalExpressions().add(new ExpressionFactory<>("character", (state1, values, args) -> keyEvent.getCharacter(), String.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("key-code", (state1, values, args) -> keyEvent.getCode().toString(), String.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("text", (state1, values, args) -> keyEvent.getText(), String.class));
        state.getLocalExpressions().add(new ExpressionFactory<>("source", (state1, values, args) -> keyEvent.getSource(), Object.class));
    }

    public static void addDragEventExpressions(DragEvent dragEvent, CodeState state) {
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

    public static StringBuilder appendAllArgs(StringBuilder builder, String... args) {
        int i = 0;
        for (String space : args) {
            i++;
            builder.append(space);
            if (args.length != i) {
                builder.append(' ');
            }
        }
        return builder;
    }

    /**
     *
     * @param code The code from which the code piece will be generated.
     * @return A CodePiece interpreted and parsed from the inputted code. Note that this piece is not yet attached to a code chunk.
     */
    public static CodePiece genCodePieceFromCode(String code, File file, int lineNum) {
        Effect effect = FXScript.PARSER.parseLine(code, file, lineNum);
        CodePiece piece = new CodePiece(code);
        if (effect != null) {
            piece.setEffect(effect);
        }
        return piece;
    }

    public static CodeChunk getCodeChunkFromCode(File file) throws FileNotFoundException {
        StringBuilder builder = new StringBuilder();
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            builder.append(scanner.nextLine()).append('\n');
        }
        return getCodeChunkFromCode(builder.toString(), file);
    }
    public static CodeChunk getCodeChunkFromCode(String code, File file) {
        return FXScript.PARSER.parseChunk(code, file);
    }

    public static void setPrintConsole(Console printConsole) {
        PRINT_CONSOLE = printConsole;
    }

    public static boolean checkPrefix(String check, String... prefixes) {
        for (String prefix : prefixes) {
            if (check.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<ExpressionFactory<?>> getAllExpressionFactories() {
        ArrayList<ExpressionFactory<?>> arrayList = new ArrayList<>();
        EXPRESSIONS.values().forEach(classArrayListHashMap -> classArrayListHashMap.values().forEach(arrayList::addAll));
        return arrayList;
    }
    public static ArrayList<ExpressionFactory<?>> getAllExpressionFactories(Class<?> ofClass) {
        ArrayList<ExpressionFactory<?>> arrayList = new ArrayList<>();
        EXPRESSIONS.values().forEach(classArrayListHashMap -> arrayList.addAll(classArrayListHashMap.get(ofClass)));
        return arrayList;
    }
    public static ArrayList<ExpressionFactory<?>> getAllExpressionFactoriesFromClass(Class<?> ofClass) {
        ArrayList<ExpressionFactory<?>> arrayList = new ArrayList<>();
        for (HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> map : EXPRESSIONS.values()) {
            for (Map.Entry<Class<?>, ArrayList<ExpressionFactory<?>>> entry : map.entrySet()) {
                if (ofClass.isAssignableFrom(entry.getKey()) || entry.getKey() == Object.class || ofClass == String.class) {
                    arrayList.addAll(entry.getValue());
                }
            }
        }
        return arrayList;
    }

}
