package tmw.me.com.ide.codeEditor;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.languages.LanguageLibrary;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;
import tmw.me.com.ide.codeEditor.languages.SfsLanguage;
import tmw.me.com.ide.tools.builders.SVGPathBuilder;
import tmw.me.com.ide.tools.concurrent.schedulers.ChangeListenerScheduler;
import tmw.me.com.ide.tools.concurrent.schedulers.ConsumerEventScheduler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p>This {@link CodeArea} adds lots of extra functionality, much of which can be customized with custom {@link LanguageSupport}.</p>
 * <br/>
 * <h3>Features</h3>
 * <ul>
 *     <li>Auto Indentation</li>
 *     <li>Comment continuation</li>
 *     <li>Backwards Indentation</li>
 *     <li>Surround selection with bracket/quote</li>
 *     <li>Find and Replace</li>
 *     <li>Auto double quotes and double brackets</li>
 *     <li>Code Highlighting*</li>
 *     <li>Auto Complete*</li>
 *     <li>Error line number highlighting*</li>
 * </ul>
 * <h3>Planned Features</h3>
 * <ul>
 *     <li>More interactive highlighting features</li>
 *     <li>A nice context menu</li>
 * </ul>
 * <p>*Controlled by LanguageSupport</p>
 */
public class IntegratedTextEditor extends CodeArea {

    public static final String INDENT = "  ";

    // Properties
    private final ObservableList<Integer> errorLines = FXCollections.observableArrayList();
    private final BooleanProperty showingFindAndReplace = new SimpleBooleanProperty(this, "showing-find-and-replace", false);
    private final ObjectProperty<LanguageSupport> languageSupport = new SimpleObjectProperty<>();

    private final ArrayList<IdeSpecialParser.PossiblePiecePackage> factoryOrder = new ArrayList<>();
    private final VBox popupBox = new VBox();
    private final ScrollPane popupScrollPane = new SmoothishScrollpane(popupBox);
    private final BorderPane titleBox = new BorderPane();
    private final VBox popupParent = new VBox(titleBox, popupScrollPane);
    private final Label popupTitleText = new Label("Autocomplete Suggestions");
    private final Popup autoCompletePopup = new Popup();
    private final ArrayList<String> selectionQueue = new ArrayList<>();

    private final Label findLabel    = new Label("Find Next  ");
    private final Label replaceLabel = new Label("Replace All");
    private final TextField findTextField = new TextField();
    private final TextField replaceTextField = new TextField();
    private final SVGPath closeFr = SVGPathBuilder.create().addStyleClass("fr-close").setPickOnBounds(true)
            .setContent("M15.898,4.045c-0.271-0.272-0.713-0.272-0.986,0l-4.71,4.711L5.493,4.045c-0.272-0.272-0.714-0.272-0.986,0s-0.272,0.714,0,0.986l4.709,4.711l-4.71,4.711c-0.272,0.271-0.272,0.713,0,0.986c0.136,0.136,0.314,0.203,0.492,0.203c0.179,0,0.357-0.067,0.493-0.203l4.711-4.711l4.71,4.711c0.137,0.136,0.314,0.203,0.494,0.203c0.178,0,0.355-0.067,0.492-0.203c0.273-0.273,0.273-0.715,0-0.986l-4.711-4.711l4.711-4.711C16.172,4.759,16.172,4.317,15.898,4.045z")
            .build();
    private final SVGPath toggleRegex = SVGPathBuilder.create().addStyleClass("fr-tool-bar-item").setPickOnBounds(true).makeSelectable()
            .setContent("M 9.3 10.5 V 6.9 l -2.9 1.8 l -1 -1.8 l 3.1 -1.7 l -3.1 -1.7 l 1.1 -1.8 l 2.9 1.8 V 0 h 2.1 v 3.6 l 2.9 -1.8 L 15.4 3.6 l -3.1 1.7 L 15.4 6.9 l -1.1 1.8 l -2.9 -1.8 v 3.5 H 9.3 z M 4.3 13.2 c 0 -1.7 -1.8 -2.7 -3.3 -1.9 c -1.4 0.8 -1.4 2.9 0 3.8 C 2.5 15.9 4.3 14.8 4.3 13.2 z")
            .build();
    private final HBox frRightToolBar = new HBox(toggleRegex);
    private final Label centerFrText = new Label("Find and Replace");
    private final BorderPane frTop = new BorderPane();
    private final HBox findHBox = new HBox(findLabel, findTextField);
    private final HBox replaceHBox = new HBox(replaceLabel, replaceTextField);
    private final VBox findAndReplaceVBox = new VBox(frTop, findHBox, replaceHBox);
    private final Pane findAndReplaceLayoutHolder = new Pane(findAndReplaceVBox);

    private final ChoiceBox<LanguageSupport> currentLanguage = new ChoiceBox<>();
    private final HBox bottomPane = new HBox(currentLanguage);

    private final VirtualizedScrollPane<IntegratedTextEditor> virtualizedScrollPane = new VirtualizedScrollPane<>(this);
    private final AnchorPane textAreaHolder = new AnchorPane(virtualizedScrollPane, findAndReplaceLayoutHolder, bottomPane);

    private double dragStart;
    private final ParallelTransition fadeOut;
    private final ParallelTransition fadeIn;

    private int findSelectedIndex;
    private final ArrayList<IndexRange> found = new ArrayList<>();

    private int selectionIndex = 0;

    private final EventHandler<MouseEvent> popupItemEvent = mouseEvent -> {
        if (mouseEvent.getSource() instanceof Node) {
            mouseEvent.consume();
            if (selectionIndex == popupBox.getChildren().indexOf((Node) mouseEvent.getSource())) {
                this.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false));
            } else {
                Node selected = this.popupBox.getChildren().get(selectionIndex);
                if (selected != null) {
                    int indexOf = this.popupBox.getChildren().indexOf((Node) mouseEvent.getSource());
                    selected.getStyleClass().remove("selected-syntax");
                    popupBox.getChildren().get(indexOf).getStyleClass().add("selected-syntax");
                    selectionIndex = indexOf;
                }
            }
        }
    };


    /**
     * Constructs a new IntegratedTextEditor with {@link SfsLanguage} as it's language support.
     */
    public IntegratedTextEditor() {
        this(LanguageLibrary.SFS);
    }

    /**
     *
     * @param languageSupport The specified {@link LanguageSupport} for the text editor.
     */
    public IntegratedTextEditor(LanguageSupport languageSupport) {

        // Some other listeners
        parentProperty().addListener((observableValue, parent, t1) -> {
            if (t1 != virtualizedScrollPane && t1 != null)
                System.err.println("Integrated Text Area's parent must always be equal to textAreaHolder");
        });

        currentLanguage.setConverter(new StringConverter<>() {
            @Override
            public String toString(LanguageSupport languageSupport) {
                return languageSupport.getLanguageName();
            }

            @Override
            public LanguageSupport fromString(String s) {
                return null;
            }
        });
        for (LanguageSupport loopSupport : LanguageLibrary.genNewLanguages()) {
            if (!loopSupport.getLanguageName().equals(languageSupport.getLanguageName())) {
                currentLanguage.getItems().add(loopSupport);
            } else {
                currentLanguage.getItems().add(languageSupport);
            }
        }
        currentLanguage.getSelectionModel().select(languageSupport);
        currentLanguage.getSelectionModel().selectedItemProperty().addListener((observableValue, languageSupport1, t1) -> languageSupportProperty().set(t1));

        transition: {
            FadeTransition fadeOutTransition = new FadeTransition(new Duration(200));
            fadeOutTransition.setToValue(0);
            ScaleTransition scaleOutTransition = new ScaleTransition(new Duration(200));
            scaleOutTransition.setToX(0.6);
            scaleOutTransition.setToY(0.6);
            TranslateTransition translateOutTransition = new TranslateTransition(new Duration(200));
            findAndReplaceVBox.heightProperty().addListener((observableValue, number, t1) -> translateOutTransition.setToY(-t1.doubleValue()));
            fadeOut = new ParallelTransition(findAndReplaceVBox, fadeOutTransition, scaleOutTransition, translateOutTransition);
            fadeOut.setOnFinished(actionEvent -> {
                findAndReplaceVBox.setVisible(false);
                findAndReplaceLayoutHolder.setVisible(false);
                findAndReplaceLayoutHolder.setMouseTransparent(true);
                findAndReplaceVBox.setTranslateY(0);
                this.requestFocus();
            });
            TranslateTransition translateTransition = new TranslateTransition(new Duration(200));
            translateTransition.setToY(0);
            FadeTransition fadeTransition = new FadeTransition(new Duration(200));
            fadeTransition.setToValue(1);
            ScaleTransition scaleTransition = new ScaleTransition(new Duration(200));
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);
            fadeIn = new ParallelTransition(findAndReplaceVBox, fadeTransition, scaleTransition, translateTransition);

        }
        findAndReplaceVBox.setOnMousePressed(mouseEvent -> dragStart = mouseEvent.getSceneY());
        findAndReplaceVBox.setOnMouseDragged(mouseEvent -> {
            if (showingFindAndReplace.get()) {
                double newFade = fadeOut.getTotalDuration().toMillis() - fadeOut.getTotalDuration().toMillis() * (mouseEvent.getSceneY() / dragStart);
                fadeOut.play();
                fadeOut.jumpTo(new Duration(newFade));
                fadeOut.pause();
            }
        });
        findAndReplaceVBox.setOnMouseReleased(mouseEvent -> {
            if (showingFindAndReplace.get()) {
                double newLoc = fadeOut.getTotalDuration().toMillis() - fadeOut.getTotalDuration().toMillis() * (mouseEvent.getSceneY() / dragStart);
                if (newLoc > 55) {
                    showingFindAndReplace.set(false);
                } else {
                    fadeIn.play();
                }
            }
        });

        showingFindAndReplace.addListener((observableValue, aBoolean, t1) -> {
            if (t1 && !aBoolean) {
                findAndReplaceVBox.setOpacity(0);
                findAndReplaceVBox.setScaleX(0.3);
                findAndReplaceVBox.setScaleY(0.3);
                findAndReplaceVBox.setTranslateY(-175);
                findAndReplaceVBox.setVisible(true);
                findAndReplaceLayoutHolder.setVisible(true);
                findAndReplaceLayoutHolder.setMouseTransparent(false);
                fadeIn.setOnFinished(actionEvent -> {
                    highlightFind();
                    fadeIn.setOnFinished(null);
                });
                fadeIn.play();
            } else if (aBoolean && !t1) {
                fadeOut.play();
                highlight();
            }
        });

        this.languageSupport.addListener((observableValue, languageSupport1, t1) -> {
            if (languageSupport1 != null) {
                this.getStylesheets().remove(languageSupport1.getStyleSheet());
                if (t1 != null) {
                    highlight();
                    selectionQueue.clear();
                    autoCompletePopup.hide();
                }
                languageSupport1.removeBehaviour(this);
            }
            if (t1 != null) {
                this.getStylesheets().add(t1.getStyleSheet());
                t1.addBehaviour(this);
            }
        });

        // Language
        this.languageSupport.set(languageSupport);

        // Highlighting
        this.plainTextChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(new ConsumerEventScheduler<>(200, false, plainTextChange -> highlight()));

        // Style classes
        findAndReplaceVBox.getStyleClass().add("fr-vbox");
        findHBox.getStyleClass().add("fr-hbox");
        replaceHBox.getStyleClass().add("fr-hbox");
        findTextField.getStyleClass().add("fr-text-field");
        replaceTextField.getStyleClass().add("fr-text-field");
        findLabel.getStyleClass().add("fr-label");
        replaceLabel.getStyleClass().add("fr-label");
        frTop.getStyleClass().add("fr-top-border-pane");
        centerFrText.getStyleClass().add("fr-title");
        popupTitleText.getStyleClass().add("popup-title");
        popupParent.getStyleClass().add("auto-complete-parent");
        currentLanguage.getStyleClass().add("language-choice-box");

        // Value tweaking and value setting
        this.popupScrollPane.setMaxHeight(300);
        this.getStylesheets().add(IntegratedTextEditor.class.getResource("ite.css").toExternalForm());
        popupParent.getStylesheets().add(Ide.STYLE_SHEET);
        autoCompletePopup.getContent().add(popupParent);
        autoCompletePopup.setAutoHide(true);
        this.setParagraphGraphicFactory(LineGraphicFactory.get(this));
        findAndReplaceLayoutHolder.setMouseTransparent(false);
        findAndReplaceLayoutHolder.setVisible(false);
        findAndReplaceVBox.setVisible(false);
        findAndReplaceVBox.setSpacing(5);
        findHBox.setSpacing(3);
        replaceHBox.setSpacing(3);
        frTop.setLeft(closeFr);
        frTop.setRight(frRightToolBar);
        frTop.setCenter(centerFrText);
        titleBox.setCenter(popupTitleText);
        popupScrollPane.setFitToWidth(true);
        popupBox.setFillWidth(true);
        popupParent.setEffect(new DropShadow());
        bottomPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        // Layout
        AnchorPane.setTopAnchor(virtualizedScrollPane, 0D); AnchorPane.setBottomAnchor(virtualizedScrollPane, 25D);
        AnchorPane.setRightAnchor(virtualizedScrollPane, 0D); AnchorPane.setLeftAnchor(virtualizedScrollPane, 0D);

        AnchorPane.setTopAnchor(findAndReplaceLayoutHolder, 0D);
        AnchorPane.setRightAnchor(findAndReplaceLayoutHolder, 25D);

        AnchorPane.setLeftAnchor(bottomPane, 0D); AnchorPane.setRightAnchor(bottomPane, 0D);
        AnchorPane.setBottomAnchor(bottomPane, 0D);

        // This event handlers
        Pattern whiteSpace = Pattern.compile( "^\\s+" );
        this.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
            KeyCode keyCode = keyEvent.getCode();
            String textAfterCaret = this.getText().substring(this.getCaretPosition());
            if ((keyCode == KeyCode.F || keyCode == KeyCode.R || keyCode == KeyCode.H) && keyEvent.isControlDown()) {
                showingFindAndReplace.set(!showingFindAndReplace.get());
            }
            if (!selectionQueue.isEmpty() && keyCode == KeyCode.LEFT || keyCode == KeyCode.RIGHT || keyCode == KeyCode.UP || keyCode == KeyCode.DOWN) {
                if (autoCompletePopup.isShowing() && (keyCode == KeyCode.UP || keyCode == KeyCode.DOWN)) {
                    if (popupBox.getChildren().size() > selectionIndex) {
                        keyEvent.consume();
                        Node selected = this.popupBox.getChildren().get(selectionIndex);
                        if (selected != null) {
                            int indexOf = this.popupBox.getChildren().indexOf(selected);
                            if (keyCode == KeyCode.UP) {
                                indexOf--;
                            } else {
                                indexOf++;
                            }
                            if (indexOf >= this.popupBox.getChildren().size()) {
                                indexOf = 0;
                            } else if (indexOf < 0) {
                                indexOf = this.popupBox.getChildren().size() - 1;
                            }
                            selected.getStyleClass().remove("selected-syntax");
                            popupBox.getChildren().get(indexOf).getStyleClass().add("selected-syntax");
                            selectionIndex = indexOf;
                        }
                    }
                } else {
                    selectionQueue.clear();
                }
            }
            if (this.getSelectedText().length() > 0) {
                if (keyEvent.isShiftDown() && (keyCode == KeyCode.QUOTE || keyCode == KeyCode.OPEN_BRACKET)) {
                    String selectionText = this.getSelectedText();
                    IndexRange selected = this.getSelection();
                    if (keyCode == KeyCode.QUOTE) {
                        selectionText = "\"" + selectionText + "\"";
                    } else {
                        selectionText = "{" + selectionText + "}";
                    }
                    String finalSelectionText = selectionText;
                    int start = selected.getStart();
                    int end = selected.getEnd();
                    this.replaceText(selected.getStart(), selected.getEnd(), finalSelectionText);
                    Platform.runLater(() -> {
                        this.replaceText(end + 2, end + 3, "");
                        this.selectRange(start + 1, end + 1);
                    });
                }
            } else if (keyEvent.isShiftDown() && keyCode == KeyCode.OPEN_BRACKET && !textAfterCaret.startsWith("}") && !keyEvent.isConsumed()) {
                Platform.runLater( () -> {
                    this.insertText(this.getCaretPosition(), "}");
                    this.moveTo(this.getCaretPosition() - 1);
                });
            } else if (keyEvent.isShiftDown() && keyCode == KeyCode.QUOTE && !textAfterCaret.startsWith("\"") && !keyEvent.isConsumed()) {
                Platform.runLater(() -> {
                    this.insertText(this.getCaretPosition(), "\"");
                    this.moveTo(this.getCaretPosition() - 1);
                });
            }
            if (keyCode == KeyCode.ENTER) {
                Matcher m = whiteSpace.matcher(line);
                Platform.runLater( () -> this.insertText(this.getCaretPosition(), (line.trim().startsWith("#") ? "#" : "") + (m.find() ? m.group() : "") + (line.endsWith(":") ? INDENT : "")));
            } else if (keyEvent.isShiftDown()) {
                if (keyCode == KeyCode.TAB) {
                    if (this.getSelectedText().length() <= 1) {
                        Matcher matcher = whiteSpace.matcher(line);
                        if (matcher.find()) {
                            String whiteSpaceInLine = matcher.group();
                            if (whiteSpaceInLine.startsWith("\t") || whiteSpaceInLine.startsWith(INDENT)) {
                                String replaced = (whiteSpaceInLine.startsWith("\t") ? whiteSpaceInLine.replaceFirst("\t", "") : whiteSpaceInLine.replaceFirst(" {2}", ""));
                                int lineStart = 0;
                                for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
                                    lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
                                }
                                int textEnd = lineStart + whiteSpaceInLine.length();
                                int finalLineStart = lineStart;
                                Platform.runLater(() -> this.replaceText(finalLineStart, textEnd, replaced));
                            }
                        }
                    } else {
                        int start = this.getSelection().getStart();
                        int end = this.getSelection().getEnd();
                        String indentedBackwards = indentBackwards(getSelectedText());
                        this.replaceText(start, end, indentedBackwards);
                        this.selectRange(start,
                                end + (indentedBackwards.length() - (end - start))
                                );
                    }
                }
            } else if (keyCode == KeyCode.TAB) {
                if (!keyEvent.isControlDown() && autoCompletePopup.isShowing()) {
                    if (factoryOrder.size() > selectionIndex) {
                        keyEvent.consume();
                        String text = factoryOrder.get(selectionIndex).getPutIn();
                        if (text.length() > 0 && this.getSelectedText().equals("")) {
                            insertAutocomplete();
                        } else {
                            int lineStart = 0;
                            for (int i = this.getCurrentParagraph(); i >= 0; i--) {
                                lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
                            }
                            lineStart--;
                            this.replaceText(lineStart, lineStart, "");
                        }
                    }
                } else if (!keyEvent.isControlDown() && !selectionQueue.isEmpty() && this.getSelectedText().equals("")) {
                    selectNext();
                    keyEvent.consume();
                } else if (!keyEvent.isControlDown() && selectionQueue.isEmpty() && !this.getSelectedText().equals("")) {
                    keyEvent.consume();
                    int start = this.getSelection().getStart();
                    int end = this.getSelection().getEnd();
                    String indentForwards = indentForwards(getSelectedText());
                    this.replaceText(start, end, indentForwards);
                    this.selectRange(start,
                            end + (indentForwards.length() - (end - start))
                    );
                }
            }
        });
        this.caretBoundsProperty().addListener((observableValue, integer, t1) -> {
            if (this.caretBoundsProperty().getValue().isPresent()) {
                Bounds bounds = this.caretBoundsProperty().getValue().get();
                Platform.runLater(() -> {
                    autoCompletePopup.setX(bounds.getMaxX());
                    autoCompletePopup.setY(bounds.getMaxY());
                });
            }
        });
        this.sceneProperty().addListener((observableValue, scene, t1) -> {
            if (t1 != null) {
                Window t11 = t1.getWindow();
                if (t11 != null) {
                    t11.xProperty().addListener((observableValue11, number, t111) -> autoCompletePopup.hide());
                    t11.yProperty().addListener((observableValue11, number, t111) -> autoCompletePopup.hide());
                    t11.widthProperty().addListener((observableValue2, number, t12) -> autoCompletePopup.hide());
                    t11.heightProperty().addListener((observableValue2, number, t12) -> autoCompletePopup.hide());
                }
            }
        });
        this.setOnMousePressed(mouseEvent -> {
            autoCompletePopup.hide();
            selectionQueue.clear();
        });
        this.textProperty().addListener((observableValue, s, t1) -> {
            if (this.getCaretPosition() < t1.length()) {
                String lastCharacter = this.getText(this.getCaretPosition(), this.getCaretPosition() + 1);
                int bracketsBefore = stringOccurrences(s, '{');
                int bracketsAfter = stringOccurrences(t1, '{');
                if (bracketsAfter == bracketsBefore - 1 && lastCharacter.equals("}")) {
                    this.replaceText(this.getCaretPosition(), this.getCaretPosition() + 1, "");
                } else {
                    int quotesBefore = stringOccurrences(s, '"');
                    int quotesAfter = stringOccurrences(t1, '"');
                    if (quotesAfter == quotesBefore - 1 && lastCharacter.equals("\"")) {
                        this.replaceText(this.getCaretPosition(), this.getCaretPosition() + 1, "");
                    }
                }
            }
            Platform.runLater(() -> fillBox(this.getParagraph(this.getCurrentParagraph()).getSegments().get(0)));
        });

        // Other event handlers
        popupScrollPane.setOnKeyPressed(keyEvent -> {
            keyEvent.consume();
            if (autoCompletePopup.isFocused()) {
                this.fireEvent(new KeyEvent(this, this, keyEvent.getEventType(), keyEvent.getCharacter(), keyEvent.getText(), keyEvent.getCode(), keyEvent.isShiftDown(), keyEvent.isControlDown(), keyEvent.isAltDown(), keyEvent.isMetaDown()));
            }
            if (keyEvent.getCode() == KeyCode.TAB) {
                insertAutocomplete();
            }
        });
        findLabel.setOnMousePressed(mouseEvent -> {
            if (!found.isEmpty()) {
                findSelectedIndex++;
                if (findSelectedIndex >= found.size()) {
                    findSelectedIndex = 0;
                }
                IndexRange indexRange = found.get(findSelectedIndex);
                this.selectRange(indexRange.getStart(), indexRange.getEnd());
                Platform.runLater(this::requestFocus);
            }
        });
        replaceLabel.setOnMousePressed(mouseEvent -> {
            int originalTextLength = getText().length();
            for (IndexRange indexRange : found) {
                int difference = getText().length() - originalTextLength;
                this.replaceText(indexRange.getStart() + difference, indexRange.getEnd() + difference, replaceTextField.getText());
            }
        });
        findTextField.textProperty().addListener(new ChangeListenerScheduler<>(400, false, (observableValue, s, t1) -> {
            findTextField.getStyleClass().remove("fr-error");
            highlightFind();
        }));
        closeFr.setOnMousePressed(mouseEvent -> showingFindAndReplace.set(false));
        toggleRegex.setOnMousePressed(mouseEvent -> highlightFind());
    }

    public AnchorPane getTextAreaHolder() {
        return textAreaHolder;
    }

    public int getFindSelectedIndex() {
        return findSelectedIndex;
    }

    /**
     * Computes and applies the highlighting on a separate thread.
     */
    public void highlight() {
        if (!showingFindAndReplace.get()) {
            HighlightingThread highlightingThread = new HighlightingThread();
            highlightingThread.setText(this.getText());
            highlightingThread.start();
        } else {
            highlightFind();
        }
    }

    /**
     * Computes and applies the default highlighting then computes and applies the find and replace highlighting.
     */
    public void highlightFind() {
        new HighlightingThread() {

            @Override
            public String getText() {
                return IntegratedTextEditor.this.getText();
            }

            @Override
            public void run() {
                super.run();
                if (findTextField.getText().equals(""))
                    return;
                Pattern pattern;
                try {
                    pattern = toggleRegex.getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected")) ?
                            Pattern.compile(findTextField.getText()) : Pattern.compile(Pattern.quote(findTextField.getText()));
                } catch (PatternSyntaxException e) {
                    if (!findTextField.getStyleClass().contains("fr-error"))
                        findTextField.getStyleClass().add("fr-error");
                    return;
                }
                findTextField.getStyleClass().remove("fr-error");
                Matcher matcher = pattern.matcher(getText());
                found.clear();
                while (matcher.find()) {
                    found.add(new IndexRange(matcher.start(), matcher.end()));
                }
                Platform.runLater(() -> {
                    for (IndexRange indexRange : connectIndexesToNeighbors(found)) {
                        setStyle(indexRange.getStart(), indexRange.getEnd(), Collections.singleton("found"));
                    }
                });
            }
        }.start();
    }

    /**
     * Inserts the selected item in the auto complete popup.
     */
    public void insertAutocomplete() {
        String text = factoryOrder.get(selectionIndex).getPutIn();
        if (factoryOrder.get(selectionIndex).isReplaceLine()) {
            int lineStart = 0;
            for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
                lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
            }
            String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
            Pattern whiteSpace = Pattern.compile("^\\s+");
            Matcher matcher = whiteSpace.matcher(line);
            if (matcher.find()) {
                text = matcher.group() + text;
            }
            this.replaceText(lineStart, this.getCaretPosition(), text);
        } else {
            this.insertText(this.getCaretPosition(), text);
        }
        selectionQueue.clear();
        selectNext();

    }

    /**
     * Selects the next item in the auto complete popup; wraps around if needed.
     */
    public void selectNext() {
        StringBuilder builder = new StringBuilder();
        boolean inPercentageSign = false;
        int loops = 0;
        int lineStart = 0;
        int parenStart = 0;
        for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
            lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
        }
        boolean first = true;
        String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
        for (char c : line.toCharArray()) {
            if (c == '%') {
                if (inPercentageSign) {
                    inPercentageSign = false;
                    builder.append(c);
                    if (first) {
                        this.selectRange(parenStart + lineStart, loops + lineStart + 1);
                        first = false;
                    } else {
                        selectionQueue.add(builder.toString());
                    }
                    builder = new StringBuilder();
                } else {
                    inPercentageSign = true;
                    builder.append(c);
                    parenStart = loops;
                }
            }
            loops++;
        }
    }

    /**
     *
     * @param line The line which the text is sampled from; should be switched to an int, this is largely controlled by {@link LanguageSupport}
     */
    private void fillBox(String line) {
        if (line.trim().length() > 0) {
            factoryOrder.clear();
            ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = languageSupport.get().getPossiblePieces(line);
            if (possiblePiecePackages != null && !possiblePiecePackages.isEmpty()) {
                ArrayList<Node> newChildren = new ArrayList<>();
                for (IdeSpecialParser.PossiblePiecePackage entry : languageSupport.get().getPossiblePieces(line)) {
                    factoryOrder.add(entry);
                    Label filledIn = new Label(entry.getFilledIn());
                    Label notFilledIn = new Label(entry.getNotFilledIn());
                    filledIn.getStyleClass().add("highlighted-label");
                    filledIn.setFont(new Font(16));
                    notFilledIn.setFont(new Font(16));
                    HBox box = new HBox(filledIn, notFilledIn);
                    box.setOnMousePressed(popupItemEvent);
                    box.setAccessibleText(entry.getPutIn());
                    newChildren.add(box);
                }
                if (!newChildren.isEmpty()) {
                    popupBox.getChildren().setAll(newChildren);
                    popupBox.getChildren().get(0).getStyleClass().add("selected-syntax");
                    selectionIndex = 0;
                    autoCompletePopup.show(this.getScene().getWindow());
                    autoCompletePopup.setHeight(popupScrollPane.getHeight());
                    popupScrollPane.setVvalue(0);
                } else {
                    autoCompletePopup.hide();
                    popupBox.getChildren().clear();
                }
            } else {
                autoCompletePopup.hide();
                popupBox.getChildren().clear();
            }
        } else {
            autoCompletePopup.hide();
        }
    }

    // Utility oriented methods
    public String properlyIndentString(String text) {
        String[] lines = text.split("\n");
        String indentCharacter = INDENT;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                indentCharacter = "\t";
                break;
            } else if (line.startsWith(INDENT)) {
                break;
            }
        }
        int minimumIndent = 999;
        for (String line : lines) {
            int lineIndent = 0;
            while (line.startsWith(indentCharacter)) {
                lineIndent++;
                line = line.substring(indentCharacter.length());
            }
            if (lineIndent < minimumIndent) {
                minimumIndent = lineIndent;
            }
        }
        StringBuilder newVersion = new StringBuilder();
        for (String line : lines) {
            newVersion.append(line.substring(minimumIndent)).append("\n");
        }
        String newString = newVersion.toString();
        return newString.substring(0, newString.length() - 1);
    }
    public String indentBackwards(String text) {
        if (text.length() <= 1) {
            return text;
        }
        String[] lines = text.split("\n");
        String indentCharacter = INDENT;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                indentCharacter = "\t";
                break;
            } else if (line.startsWith(INDENT)) {
                break;
            }
        }
        StringBuilder newString = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith(indentCharacter)) {
                line = line.substring(indentCharacter.length());
            }
            newString.append("\n").append(line);
        }
        return newString.substring(1);
    }
    public String indentForwards(String text) {
        String[] lines = text.split("\n");
        String indentCharacter = INDENT;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                indentCharacter = "\t";
                break;
            } else if (line.startsWith(INDENT)) {
                break;
            }
        }
        if (text.length() <= 1) {
            return indentCharacter + text;
        }
        StringBuilder newString = new StringBuilder();
        for (String line : lines) {
            newString.append("\n").append(indentCharacter).append(line);
        }
        return newString.substring(1);
    }
    public int[] expandFromPoint(int caretPosition, Character... stopAt) {
        int right = expandInDirection(caretPosition, 1, stopAt);
        right = right < getText().length() ? right + 1 : right;
        return new int[] { expandInDirection(caretPosition, -1, stopAt), right };
    }
    public int expandInDirection(int start, int dir, Character... stopAt) {
        String text = this.getText();
        List<Character> characters = Arrays.asList(stopAt);
        while (start > 0 && start < text.length() && !characters.contains(text.charAt(start))) start += dir;
        return start;
    }
    public ArrayList<IndexRange> allInstancesOfStringInString(String lookIn, String lookFor) {
        ArrayList<IndexRange> areas = new ArrayList<>();
        Matcher matcher = Pattern.compile("(?=" + Pattern.quote(lookFor) +")").matcher(lookIn);
        while (matcher.find()) {
            areas.add(new IndexRange(matcher.start(), matcher.start() + lookFor.length()));
        }
        return areas;
    }
    public int absolutePositionFromLine(int line) {
        int lineStart = 0;
        for (int i = line - 1; i >= 0; i--) {
            lineStart = lineStart + this.getParagraph(i).getText().length() + 1;
        }
        return lineStart;
    }
    private int stringOccurrences(String string, char checkFor) {
        int occurrences = 0;
        for (char c : string.toCharArray()) {
            if (c == checkFor) occurrences++;
        }
        return occurrences;
    }
    private List<IndexRange> connectIndexesToNeighbors(List<IndexRange> indexRanges) {
        if (indexRanges.isEmpty())
            return indexRanges;
        ArrayList<IndexRange> newIndexRanges = new ArrayList<>();
        IndexRange lastIndexRange = indexRanges.get(0);
        for (int i = 1; i < indexRanges.size(); i++) {
            IndexRange currentIndexRange = indexRanges.get(i);
            if (lastIndexRange.getEnd() == currentIndexRange.getStart())
                lastIndexRange = new IndexRange(lastIndexRange.getStart(), currentIndexRange.getEnd());
            else {
                newIndexRanges.add(lastIndexRange);
                lastIndexRange = currentIndexRange;
            }
        }
        newIndexRanges.add(lastIndexRange);
        return newIndexRanges;
    }

    // Getters and Setters
    public LanguageSupport getLanguage() {
        return languageSupport.get();
    }
    public ObjectProperty<LanguageSupport> languageSupportProperty() {
        return languageSupport;
    }
    public void setLanguageSupport(LanguageSupport languageSupport) {
        this.languageSupport.set(languageSupport);
    }

    public ObservableList<Integer> getErrorLines() {
        return errorLines;
    }

    private class HighlightingThread extends Thread {

        private String text;

        public HighlightingThread() {
            super();
            this.setDaemon(true);
        }

        @Override
        public void run() {
            findSelectedIndex = -1;
            computeHighlighting(getText());
        }


        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        private void computeHighlighting(String text) {
            Pattern pattern = languageSupport.get().generatePattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(text);
                int lastKwEnd = 0;
                StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
                while (matcher.find()) {
                    String styleClass = languageSupport.get().styleClass(matcher);
                    spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                    spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                    lastKwEnd = matcher.end();
                }
                spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
                StyleSpans<Collection<String>> styleSpans = spansBuilder.create();
                Platform.runLater(() -> {
                    try {
                        setStyleSpans(0, styleSpans);
                    } catch (IndexOutOfBoundsException ignored) {
                    }
                });
            }
        }



    }

}
