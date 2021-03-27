package tmw.me.com.ide.codeEditor.texteditor;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveList;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.MiniMap;
import tmw.me.com.ide.codeEditor.highlighting.Highlighter;
import tmw.me.com.ide.codeEditor.languages.LanguageLibrary;
import tmw.me.com.ide.codeEditor.languages.LanguageSupplier;
import tmw.me.com.ide.codeEditor.languages.LanguageSupport;
import tmw.me.com.ide.codeEditor.languages.PlainTextLanguage;
import tmw.me.com.ide.codeEditor.texteditor.filters.KeyFilter;
import tmw.me.com.ide.codeEditor.visualcomponents.AutocompletePopup;
import tmw.me.com.ide.codeEditor.visualcomponents.FindAndReplace;
import tmw.me.com.ide.codeEditor.visualcomponents.tooltip.EditorTooltip;
import tmw.me.com.ide.notifications.SuccessNotification;
import tmw.me.com.ide.settings.IdeSettings;
import tmw.me.com.ide.tools.tabPane.ComponentTabContent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>This class adds lots of extra functionality over the {@link CodeArea} which it is built upon. Much of that functionality can be customized with custom {@link LanguageSupport}.</p>
 * <br/>
 * <h2>Features</h3>
 * <ul>
 *     <li>Code Formatting*</li>
 *     <li>Find and Replace</li>
 *     <li>Code Highlighting*</li>
 *     <li>A powerful highlighting pipeline fueled by the {@link Highlighter} class.</li>
 *     <li>Auto Complete*</li>
 *     <li>Error line number highlighting*</li>
 *     <li>Tooltips*</li>
 * </ul>
 * <h2>Planned Features</h3>
 * <ul>
 *     <li>A nice context menu</li>
 *     <li>A Minimap (WIP)</li>
 * </ul>
 * <h3>Notes:</h3>
 * <p>*Controlled by LanguageSupport</p>
 */
public class IntegratedTextEditor extends BehavioralLanguageEditor implements ComponentTabContent<IntegratedTextEditor> {

    // Settings
    private static final boolean USING_MINIMAP = false;
    private static final boolean USING_AUTOCOMPLETE = true;

    // Properties
    private final ObservableList<Integer> errorLines = FXCollections.observableArrayList();

    private final AutocompletePopup autoCompletePopup = new AutocompletePopup(this);
    private final MiniMap miniMap = USING_MINIMAP ? new MiniMap() : null;
    private final FindAndReplace findAndReplace = new FindAndReplace(this);
    private final EditorTooltip tooltip = new EditorTooltip(this);

    private final ChoiceBox<LanguageSupplier<? extends LanguageSupport>> currentLanguage = new ChoiceBox<>();
    private final Label caretPositionLabel = new Label("1:1");
    private final Label selectionInfoLabel = new Label();
    private final HBox bottomPane = new HBox(selectionInfoLabel, caretPositionLabel, currentLanguage);

    private final VirtualizedScrollPane<IntegratedTextEditor> virtualizedScrollPane = new VirtualizedScrollPane<>(this);
    private final AnchorPane textAreaHolder = new AnchorPane(virtualizedScrollPane, findAndReplace, bottomPane);


    /**
     * Constructs a new IntegratedTextEditor with {@link PlainTextLanguage} as it's languageSupport support.
     */
    public IntegratedTextEditor() {
        this(new PlainTextLanguage());
    }

    @Override
    protected boolean alternateIsFocused() {
        return autoCompletePopup.isFocused() || isFocused();
    }

    /**
     * @param languageSupport The specified {@link LanguageSupport} for the text editor.
     */
    public IntegratedTextEditor(LanguageSupport languageSupport) {
        super(languageSupport);

        if (USING_MINIMAP) {
            textAreaHolder.getChildren().add(1, miniMap);
        }

        setContextMenu(generateContextMenu());

        // Some other listeners
        parentProperty().addListener((observableValue, parent, t1) -> {
            if (t1 != virtualizedScrollPane && t1 != null)
                System.err.println("Integrated Text Area's parent must always be equal to textAreaHolder");
        });

        currentLanguage.setConverter(new StringConverter<>() {
            @Override
            public String toString(LanguageSupplier<? extends LanguageSupport> languageSupport) {
                return languageSupport.getName();
            }

            @Override
            public LanguageSupplier<LanguageSupport> fromString(String s) {
                return null;
            }
        });
        for (LanguageSupplier<? extends LanguageSupport> loopSupport : LanguageLibrary.defaultLanguages) {
            if (loopSupport != null && !loopSupport.getName().equals(languageSupport.getLanguageName())) {
                currentLanguage.getItems().add(loopSupport);
            } else {
                currentLanguage.getItems().add(languageSupport.toSimpleSupplier());
            }
        }
        currentLanguage.getSelectionModel().select(languageSupport.toSimpleSupplier());
        currentLanguage.getSelectionModel().selectedItemProperty().addListener((observableValue, languageSupport1, t1) -> setLanguage(t1.get()));

        // Style classes
        currentLanguage.getStyleClass().add("language-choice-box");
        bottomPane.getStyleClass().addAll("bottom-ite-pane");

        // Value tweaking and value setting
        this.getStylesheets().add(STYLE_SHEET);

        this.setParagraphGraphicFactory(LineGraphicFactory.get(this));

        // Layout
        AnchorPane.setTopAnchor(virtualizedScrollPane, 0D);
        AnchorPane.setBottomAnchor(virtualizedScrollPane, 30D);
        AnchorPane.setLeftAnchor(virtualizedScrollPane, 0D);

        AnchorPane.setTopAnchor(findAndReplace, 0D);
        AnchorPane.setRightAnchor(findAndReplace, 25D);

        AnchorPane.setLeftAnchor(bottomPane, 0D);
        AnchorPane.setRightAnchor(bottomPane, 0D);
        AnchorPane.setBottomAnchor(bottomPane, 0D);

        double divideBy = 9.5;
        AnchorPane.setRightAnchor(virtualizedScrollPane, getWidth() / divideBy);

        // This event handlers

        caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            int line = lineFromAbsoluteLocation(newValue + 1);
            String caretPos = (line + 1) + ":" + (newValue - absolutePositionFromLine(line) + 1);
            caretPositionLabel.setText(caretPos);
        });

        this.addEventFilter(KeyEvent.KEY_PRESSED, this::receiveKeyPressed);
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
            autoCompletePopup.getSelectionQueue().clear();
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
            if (t1.contains("\t")) {
                String replacement = IdeSettings.tabSize();
                LiveList<Paragraph<Collection<String>, String, Collection<String>>> paragraphs = getParagraphs();
                for (int i = 0; i < paragraphs.size(); i++) {
                    Paragraph<Collection<String>, String, Collection<String>> par = paragraphs.get(i);
                    Matcher tabMatcher = Pattern.compile("\t").matcher(par.getText());
                    while (tabMatcher.find()) {
                        int end = tabMatcher.end();
                        int start = tabMatcher.start();
                        replaceText(i, start, i, end, replacement);
                    }
                }
            }
            Platform.runLater(() -> {
                autoCompletePopup.fillBox(this, this.getParagraph(this.getCurrentParagraph()).getSegments().get(0));
                if (getCaretBounds().isPresent()) {
                    Bounds caretBounds = getCaretBounds().get();
                    autoCompletePopup.setX(caretBounds.getMinX() - fontSize.get());
                    autoCompletePopup.setY(caretBounds.getMinY() + fontSize.get());
                }
            });

        });

        selectedTextProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                int length = newValue.length();
                String text = newValue.length() + " " + (length == 1 ? "char" : "chars");
                if (newValue.contains("\n")) {
                    int lineBreaks = allInstancesOfStringInString(newValue, "\n").size();
                    text = text + ", " + lineBreaks + " " + (lineBreaks == 1 ? "line break" : "line breaks");
                }
                selectionInfoLabel.setText(text);
            } else {
                selectionInfoLabel.setText("");
            }
        });

        // Other event handlers

        if (USING_MINIMAP) {
            assert miniMap != null;
            miniMap.loadFromITE(this);
            widthProperty().addListener((observableValue, number, t1) -> {
                miniMap.setMaxWidth(t1.doubleValue() / divideBy);
                miniMap.setMinWidth(t1.doubleValue() / divideBy);
                AnchorPane.setRightAnchor(virtualizedScrollPane, getWidth() / divideBy);
            });
            virtualizedScrollPane.heightProperty().addListener((observableValue, number, t1) -> miniMap.getMinimapContainer().setMaxHeight(t1.doubleValue()));
            miniMap.setMaxWidth(getWidth() / divideBy);
            miniMap.setMinWidth(getWidth() / divideBy);
            miniMap.getMinimapContainer().setMaxHeight(virtualizedScrollPane.getHeight());
            AnchorPane.setRightAnchor(miniMap, 0D);
            AnchorPane.setTopAnchor(miniMap, 0D);
            AnchorPane.setBottomAnchor(miniMap, 15D);
        }

        getKeyFilters().add(new KeyFilter<>() {
            @Override
            public void receiveAcceptedInput(KeyEvent event, FilteredEditor editor) {
                if (event.getCode() == KeyCode.SLASH && event.isControlDown()) {
                    String selectedText = getSelectedText();
                    if (selectedText == null || selectedText.isEmpty() || !selectedText.contains("\n")) {
                        int lineNum = lineFromAbsoluteLocation(getCaretPosition());
                        String lineText = getText(lineNum);
                        if (lineText.trim().startsWith(getLanguage().getCommentChars())) {
                            String uncommented = lineText.replaceFirst(Pattern.quote(getLanguage().getCommentChars()), "");
                            replaceText(lineNum, 0, lineNum, lineText.length(), uncommented);
                        } else {
                            insertText(absolutePositionFromLine(lineNum), getLanguage().getCommentChars());
                        }
                    } else {
                        IndexRange selection = getSelection();
                        int current = 0;
                        List<Paragraph<Collection<String>, String, Collection<String>>> paragraphs = new ArrayList<>();
                        boolean comment = false;
                        for (Paragraph<Collection<String>, String, Collection<String>> par : getParagraphs()) {
                            current += par.length() + 1;
                            if (current > selection.getEnd()) {
                                break;
                            } else if (current >= selection.getStart()) {
                                paragraphs.add(par);
                                if (!par.getText().trim().startsWith(getLanguage().getCommentChars()))
                                    comment = true;
                            }
                        }
                        int startLine = lineFromAbsoluteLocation(selection.getStart());
                        for (int i = 0; i < paragraphs.size(); i++) {
                            Paragraph<Collection<String>, String, Collection<String>> par = paragraphs.get(i);
                            int lineNum = i + startLine;
                            String lineText = par.getText();
                            if (!comment) {
                                String uncommented = lineText.replaceFirst(Pattern.quote(getLanguage().getCommentChars()), "");
                                replaceText(lineNum, 0, lineNum, lineText.length(), uncommented);
                            } else {
                                insertText(absolutePositionFromLine(lineNum), getLanguage().getCommentChars());
                            }
                        }
                    }
                }
            }
        });
    }

    private ContextMenu generateContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem copy = new MenuItem("Copy");
        copy.setOnAction(actionEvent -> super.copy());
        MenuItem paste = new MenuItem("Paste");
        paste.setOnAction(actionEvent -> super.paste());
        menu.getItems().addAll(copy, paste);
        return menu;
    }

    @Override
    protected void fontSizeChanged(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
        super.fontSizeChanged(observable, oldVal, newVal);
        String newStyle = "-fx-font-size: " + newVal.intValue() + ";";
        autoCompletePopup.getTopBox().setStyle(newStyle);
        tooltip.getNode().setStyle(newStyle);
    }

    @Override
    protected void anyLanguageChanged(LanguageSupport oldLang, LanguageSupport newLang) {
        super.anyLanguageChanged(oldLang, newLang);
        if (newLang != null) {
            if (oldLang != null) {
                autoCompletePopup.getSelectionQueue().clear();
                autoCompletePopup.hide();
            }
        }
    }

    @Override
    protected void languageRemoved(LanguageSupport language) {
        super.languageRemoved(language);
        getErrorLines().clear();
    }

    public void receiveKeyPressed(KeyEvent keyEvent) {
//         Getting certain important variables which are used a lot in other places.
        String line = this.getParagraph(this.getCurrentParagraph()).getSegments().get(0);
        KeyCode keyCode = keyEvent.getCode();
        String textAfterCaret = this.getText().substring(this.getCaretPosition());
        // This code shows or closes the Find And Replace window
        // TODO replace this with something which loops over a list of VisualComponents.
        findAndReplace.receiveKeyEvent(keyEvent, this);
        autoCompletePopup.receiveKeyEvent(keyEvent, this);
        // Auto close brackets and quotes
        // TODO add auto closing for parentheses and square brackets and single quotes
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
            Platform.runLater(() -> {
                this.insertText(this.getCaretPosition(), "}");
                this.moveTo(this.getCaretPosition() - 1);
            });
        } else if (keyEvent.isShiftDown() && keyCode == KeyCode.QUOTE && !textAfterCaret.startsWith("\"") && !keyEvent.isConsumed()) {
            Platform.runLater(() -> {
                this.insertText(this.getCaretPosition(), "\"");
                this.moveTo(this.getCaretPosition() - 1);
            });
        }
        // Auto indent
        if (keyCode == KeyCode.ENTER) {
            Pattern whiteSpace = Pattern.compile("^\\s+");
            Matcher m = whiteSpace.matcher(line);
            boolean found = m.find();
            if (found || line.endsWith(":")) {
                String s = found ? m.group() : "";
                Platform.runLater(() -> this.insertText(this.getCaretPosition(), s + (line.endsWith(":") ? IdeSettings.tabSize() : "")));
            }
        } else if (keyEvent.isShiftDown()) {
            if (keyCode == KeyCode.TAB) {
                if (this.getSelectedText().length() <= 1) {
                    Pattern whiteSpace = Pattern.compile("^\\s+");
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
        } else if (keyCode == KeyCode.TAB && !autoCompletePopup.isShowing()) {
            if (!keyEvent.isControlDown() && !autoCompletePopup.getSelectionQueue().isEmpty() && this.getSelectedText().equals("")) {
                autoCompletePopup.selectNext(this);
                keyEvent.consume();
            } else if (!keyEvent.isControlDown() && autoCompletePopup.getSelectionQueue().isEmpty() && !this.getSelectedText().equals("")) {
                keyEvent.consume();
                int start = this.getSelection().getStart();
                int end = this.getSelection().getEnd();
                String indentForwards = indentForwards(getSelectedText());
                this.replaceText(start, end, indentForwards);
                this.selectRange(start,
                        end + (indentForwards.length() - (end - start))
                );
            } else if (!keyEvent.isControlDown()) {
                keyEvent.consume();
                insertText(getCaretPosition(), IdeSettings.tabSize());
            }
        }
        // Scrolling
        if (keyEvent.isControlDown()) {
            if (keyCode == KeyCode.EQUALS) {
                if (keyEvent.isShiftDown()) {
                    keyEvent.consume();
                    fontSize.set(fontSize.get() + 2);
                } else {
                    keyEvent.consume();
                    fontSize.set(18);
                }
            } else if (keyCode == KeyCode.MINUS) {
                keyEvent.consume();
                if (fontSize.get() >= 8) {
                    fontSize.set(fontSize.get() - 2);
                }
            }
        }
    }

    public AnchorPane getTextAreaHolder() {
        return textAreaHolder;
    }

    public FindAndReplace getFindAndReplace() {
        return findAndReplace;
    }

    public boolean isCurrentlyUsingAutoComplete() {
        return USING_AUTOCOMPLETE && languageSupport.get().isUsingAutoComplete();
    }

    public ObservableList<Integer> getErrorLines() {
        return errorLines;
    }


    @Override
    public IntegratedTextEditor getImportantNode() {
        return this;
    }

    @Override
    public Region getMainNode() {
        return getTextAreaHolder();
    }

    @Override
    public void save(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(getTabbedText());
            fileWriter.close();
            Parent parent = getParent();
            while (parent != null) {
                if (parent instanceof Ide) {
                    ((Ide) parent).getNotificationPane().showNotification(new Duration(2000),
                            new SuccessNotification("Saved: " + file.getName()));
                    break;
                }
                parent = parent.getParent();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IntegratedTextEditor createNewCopy() {
        IntegratedTextEditor newIntegratedTextEditor = new IntegratedTextEditor(getLanguage().toSupplier().get());
        newIntegratedTextEditor.replaceText(getText());
        IntegratedTextEditor.linkITEs(this, newIntegratedTextEditor);
        return newIntegratedTextEditor;
    }

    public void lockLanguageUI() {
        textAreaHolder.getChildren().remove(bottomPane);
        AnchorPane.setBottomAnchor(virtualizedScrollPane, 0D);
    }

    public void selectLine(int finalI) {
        selectRange(finalI, 0, finalI, getParagraph(finalI).getText().length());
    }

    @Override
    public MenuItem[] addContext() {
        MenuItem run = new MenuItem("Run");
        run.setOnAction(actionEvent -> languageSupport.get().runCalled(this, null));
        CheckMenuItem rapText = new CheckMenuItem("Wrap text");
        this.wrapTextProperty().unbind();
        this.wrapTextProperty().bind(rapText.selectedProperty());
        return new MenuItem[]{new SeparatorMenuItem(), run, rapText};
    }

    public VirtualizedScrollPane<IntegratedTextEditor> getVirtualizedScrollPane() {
        return virtualizedScrollPane;
    }

    @Override
    public void onHighlight() {
        getFindAndReplace().setFindSelectionIndex(-1);
    }

}
