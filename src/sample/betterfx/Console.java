package sample.betterfx;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Console extends HBox {

    private final VBox topBox = new VBox();

    private final TextFlow consoleText = new TextFlow();
    private final TextField consoleInput = new TextField();

    private final SVGPath arrow = new SVGPath();

    private final HBox inputBox = new HBox(arrow, consoleInput);
    private final HBox wrapConsoleText = new HBox(consoleText);
    private final ScrollPane consoleTextScrollPane = new ScrollPane(wrapConsoleText);

    protected final ArrayList<ConsoleEvent> protectedOnUserInput = new ArrayList<>();
    protected final ArrayList<ConsoleEvent> protectedOnPrint = new ArrayList<>();
    protected final HashMap<PrintWriter, ConsoleEvent> printWriters = new HashMap<>();
    private final ArrayList<ConsoleEvent> onUserInput = new ArrayList<>();
    private final ArrayList<ConsoleEvent> onPrint = new ArrayList<>();

    private Background mainBg = new Background(new BackgroundFill(Paint.valueOf("#333333"), CornerRadii.EMPTY, Insets.EMPTY));
    private String textFill = "rgb(245, 245, 245)";
    private Color defaultConsoleColor = Color.rgb(245, 245, 245);
    private Font font = new Font("Terminal", 20);
    private static final Font INPUT_FONT = new Font("Terminal", 24);

    private final ArrayList<String> sent = new ArrayList<>();
    private int currentText = 0;

    private PrintStream printStream;


    public Console() {
        init();
    }

    public static Console generateForJava() {

        Console console = new Console();
        console.setOnUserInput();
        System.setErr(console.generateNewPrintStream(Color.RED));
        System.setOut(console.getStream());
        try {
            PipedInputStream inputStream = new PipedInputStream();
            System.setIn(inputStream);
            PrintWriter inWriter = new PrintWriter(new PipedOutputStream(inputStream), true);
            console.addPrintWriter(inWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return console;
    }

    private void init() {
        topBox.getChildren().addAll(consoleTextScrollPane, inputBox);
        consoleTextScrollPane.getStylesheets().add(Console.class.getResource("scrollpane.css").toExternalForm());
        VBox.setVgrow(consoleTextScrollPane, Priority.ALWAYS);
        HBox.setHgrow(topBox, Priority.ALWAYS);
        this.getChildren().addAll(topBox);

        consoleTextScrollPane.setFitToWidth(true);
        consoleTextScrollPane.setFitToHeight(true);
        wrapConsoleText.setFillHeight(true);
        wrapConsoleText.setPadding(new Insets(0, 5, 5, 5));
        topBox.setFillWidth(true);
        this.setFillHeight(true);
        this.setBackground(mainBg);

        inputBox.setFillHeight(true);

        consoleText.heightProperty().addListener((observableValue, number, t1) -> Platform.runLater(() -> consoleTextScrollPane.setVvalue(1)));


        arrow.setContent("M0 0l0 -34.3l19.6 17.15zM0 -9.8L0 -9.8M4.9 -14.7L4.9 -14.7L4.9 -19.6L0 -24.5L0 -9.8");
        setArrowColor(Color.valueOf("#06c906"));
        DropShadow shadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.valueOf("#6b6b6b"), 10, 0.17, 0, 0);
        setArrowEffect(shadow);
        this.inputBox.setBorder(new Border(new BorderStroke(Color.valueOf("#292929"), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(5, 0, 0, 0), new Insets(-3))));
        inputBox.setPadding(new Insets(0, 2, 2, 3));
        consoleInput.setPadding(new Insets(0, 0, 13, 3));
        inputBox.setBackground(mainBg); consoleTextScrollPane.setBackground(mainBg); consoleText.setBackground(mainBg); consoleInput.setBackground(mainBg); wrapConsoleText.setBackground(mainBg);
        consoleInput.setFont(INPUT_FONT);
        consoleInput.setStyle("-fx-text-fill: " + textFill);

        setOnUserInput((input, source) -> {
            DateTimeFormatter dft = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            addText(getDefaultText("[" + dft.format(now) + "]", Color.BLACK), true);
            sendCurrentText(false);
            if (input.equals("/help")) {
                Text header = getDefaultText("---Help---");
                header.setFill(Color.ROYALBLUE);
                Text footer = getDefaultText("---Help---");
                footer.setFill(Color.ROYALBLUE);
                Text help = getDefaultText("/help > Displays This\nDeveloper note: This is the default onUserInput. Can be configured with setOnUserInput.");
                help.setFill(Color.LIGHTSEAGREEN);
                addText(header, true);
                addText(help, true);
                addText(footer, true);
            } else {
                Text secondText = getDefaultText("<- Invalid command, run /help for a list of valid commands.");
                secondText.setFill(Color.RED);
                addText(secondText, false);
            }
        });

        consoleInput.setOnAction(actionEvent -> {
            String consoleValue = consoleInput.getText();
            for (ConsoleEvent event : protectedOnUserInput) {
                event.onInput(consoleValue, this);
            }
            for (ConsoleEvent event : onUserInput) {
                event.onInput(consoleValue, this);
            }
        });
        consoleInput.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.DOWN) {
                if (currentText - 1 > 0) {
                    currentText = currentText - 1;
                    consoleInput.setText(sent.get(sent.size() - currentText));
                }
            } else if (keyEvent.getCode() == KeyCode.UP) {
                if (sent.size() > currentText) {
                    currentText++;
                    consoleInput.setText(sent.get(sent.size() - currentText));
                }
            }
        });
        inputBox.widthProperty().addListener((observableValue, number, t1) -> consoleInput.setPrefWidth(inputBox.getWidth() - 30));
    }

    public void setArrowColor(Color color) {
        arrow.setFill(color);
    }
    public void setArrowEffect(Effect effect) {
        arrow.setEffect(effect);
    }

    public ArrayList<ConsoleEvent> getOnUserInput() {
        return onUserInput;
    }
    public void setOnUserInput() { onUserInput.clear(); }
    public void setOnUserInput(ConsoleEvent event) {
        onUserInput.clear();
        onUserInput.add(event);
    }

    public ArrayList<ConsoleEvent> getOnPrint() {
        return onPrint;
    }
    public void setOnPrint(ConsoleEvent event) {
        onPrint.clear();
        onPrint.add(event);
    }

    public void addText(String string, boolean newLine) {
        Text text = getDefaultText();
        text.setText(string + " ");
        addText(text, newLine);
        for (ConsoleEvent event : protectedOnPrint) {
            event.onInput(string, this);
        }
        for (ConsoleEvent event : onPrint) {
            event.onInput(string, this);
        }
    }
    public void addText(Text text, boolean newLine) {
        if (newLine && !consoleText.getChildren().isEmpty()) text.setText("\n" + text.getText());
        consoleText.getChildren().add(text);
    }

    public void addLines(Text... texts) { addTexts(true, texts); }
    public void addTexts(Text... texts) { addTexts(false, texts); }
    public void addTexts(boolean newLine, Text... texts) {
        for (Text text : texts) {
            addText(text, newLine);
        }
    }

    public Text getDefaultText() { return getDefaultText("", defaultConsoleColor); }
    public Text getDefaultText(String s) { return getDefaultText(s, defaultConsoleColor); }
    public Text getDefaultText(Color color) { return getDefaultText("", color); }
    public Text getDefaultText(String s, Color color) {
        Text text = new Text(s + " ");
        text.setFont(font);
        text.setFill(color);
        return text;
    }

    public void sendCurrentText() { sendCurrentText(defaultConsoleColor, true); }
    public void sendCurrentText(boolean newLine) { sendCurrentText(defaultConsoleColor, newLine); }
    public void sendCurrentText(Color color) { sendCurrentText(color, true); }
    public void sendCurrentText(Color color, boolean newLine) {
        Text text = getDefaultText(consoleInput.getText());
        text.setFill(color);
        addText(text, newLine);
        simulateMessageSend(consoleInput.getText());
        consoleInput.setText("");
    }

    public void simulateMessageSend(String message) {
        sent.add(message);
        currentText = 0;
    }

    public TextField getConsoleInput() {
        return consoleInput;
    }
    public TextFlow getConsoleText() {
        return consoleText;
    }

    public void disableInput() {
        this.topBox.getChildren().remove(inputBox);
    }
    public void enableInput() {
        if (this.topBox.getChildren().contains(inputBox)) {
            this.topBox.getChildren().add(inputBox);
        }
    }
    public void toggleInput(boolean input) {
        if (input) {
            enableInput();
        } else {
            disableInput();
        }
    }

    public void setMainBg(Paint paint) {
        Background background = new Background(new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY));
        inputBox.setBackground(background);
        consoleTextScrollPane.setBackground(background);
        consoleTextScrollPane.setBorder(new Border(new BorderStroke(paint, BorderStrokeStyle.NONE, CornerRadii.EMPTY, BorderWidths.EMPTY)));
        consoleText.setBackground(background);
        consoleInput.setBackground(background);
        wrapConsoleText.setBackground(background);
        this.setBackground(background);
        mainBg = background;
    }

    public void setTextColor(Color color) {
        String style = toRgbString(color);
        consoleInput.setStyle(style);
        textFill = style;
        for (Node text : consoleText.getChildren()) {
            if (text.getClass() == Text.class) {
                ((Text) text).setFill(color);
            }
        }
        defaultConsoleColor = color;
    }

    private String toRgbString(Color c) {
        return "rgb("
                + to255Int(c.getRed())
                + "," + to255Int(c.getGreen())
                + "," + to255Int(c.getBlue())
                + ")";
    }
    private int to255Int(double d) {
        return (int) (d * 255);
    }

    public PrintStream getStream() {
        if (printStream == null) {
            printStream = generateNewPrintStream(defaultConsoleColor);
        }
        return printStream;
    }

    public PrintStream generateNewPrintStream(Color streamColor) {
        return new PrintStream(new OutputStream() {
            private final StringBuilder builder = new StringBuilder();

            @Override
            public void write(int b) {
                if (b == '\n') {
                    String s = builder.toString();
                    builder.setLength(0);
                    Text text = getDefaultText();
                    text.setText(s);
                    text.setFill(streamColor);
                    addText(text, true);
                } else if (b != '\r') {
                    builder.append((char) b);
                }
            }
        });
    }

    public void addPrintWriter(PrintWriter in) {
        ConsoleEvent event = (inputtedString, eventConsole) -> in.println(inputtedString);
        this.printWriters.put(in, event);
        this.protectedOnUserInput.add(event);
    }
    public void removePrintWriter(PrintWriter writer) {
        if (printWriters.containsKey(writer)) {
            this.protectedOnUserInput.remove(this.printWriters.get(writer));
            this.printWriters.remove(writer);
        }
    }

    public Text[] genTexts(String... s) { return genTexts(" ", '&', s); }
    public Text[] genTexts(char split, String... s) { return genTexts(" ", split, s); }
    public Text[] genTexts(String inBetween, String... s) { return genTexts(inBetween, '&', s); }
    public Text[] genTexts(String inBetween, char split, String... s) {
        ArrayList<Text> texts = new ArrayList<>();
        for (String string : s) {
            texts.addAll(Arrays.asList(genText(string + inBetween, split)));
        }
        return texts.toArray(new Text[0]);
    }
    public Text[] genText(String s) { return genText(s, '&'); }
    public Text[] genText(String s, char split) {
        if (!s.startsWith(String.valueOf(split))) s = split + "r" + s;
        String[] colors = s.split(String.valueOf(split));
        ArrayList<Text> texts = new ArrayList<>();
        Text lastText = getDefaultText("");
        for (String string : colors) {
            if (!string.equals("")) {
                Color color = TextModifier.colorFromChar(string.charAt(0));
                Font font = TextModifier.fontFromChar(string.charAt(0), lastText.getFont());
                boolean strikeThrough = TextModifier.strikethroughFromChar(string.charAt(0));
                boolean underline = TextModifier.underlineFromChar(string.charAt(0));
                Text newText = duplicateText(lastText);
                newText.setText(string.substring(1));
                if (color != null && !color.equals(lastText.getFill())) {
                    newText.setFill(color);
                } else if (font != null && !font.equals(lastText.getFont())) {
                    newText.setFont(font);
                } else if (underline != lastText.isUnderline()) {
                    newText.setUnderline(underline);
                } else if (strikeThrough != lastText.isStrikethrough()) {
                    newText.setStrikethrough(strikeThrough);
                }
                lastText = newText;
                texts.add(newText);
            }
        }
        return texts.toArray(new Text[0]);
    }

    public Text genButton(String text) { return genButton(text, defaultConsoleColor); }
    public Text genButton(String text, Color textFill) {
        Text started = getDefaultText(text);
        started.setFill(textFill);
        started.setFont(Font.font(started.getFont().getFamily(), FontWeight.BOLD, started.getFont().getSize()));
        started.setEffect(new Glow(0.4));
        started.setCursor(Cursor.HAND);
        started.setOpacity(0.8);
        started.setOnMouseEntered(mouseEvent -> {
            FadeTransition fadeTransition = new FadeTransition(new Duration(175), started);
            fadeTransition.setToValue(1);
            fadeTransition.play();
        });
        started.setOnMouseExited(mouseEvent -> {
            FadeTransition fadeTransition = new FadeTransition(new Duration(175), started);
            fadeTransition.setToValue(0.8);
            fadeTransition.play();
        });
        return started;
    }

    public static Text duplicateText(Text text) {
        Text newText = new Text(text.getText());
        newText.setFont(text.getFont());
        newText.setTextAlignment(text.getTextAlignment());
        newText.setTabSize(text.getTabSize());
        newText.setVisible(text.isVisible());
        newText.setFill(text.getFill());
        return newText;
    }

    public void setFont(Font font) {
        this.font = font;
    }
    public Font getFont() {
        return font;
    }
}
