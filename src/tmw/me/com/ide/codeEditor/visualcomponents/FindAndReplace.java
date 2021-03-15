package tmw.me.com.ide.codeEditor.visualcomponents;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import tmw.me.com.ide.codeEditor.highlighting.SortableStyleSpan;
import tmw.me.com.ide.codeEditor.highlighting.StyleSpansFactory;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;
import tmw.me.com.ide.tools.builders.SVGPathBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FindAndReplace extends Pane implements VisualComponent<FindAndReplace> {

    private final BooleanProperty showingFindAndReplace = new SimpleBooleanProperty(this, "showing-find-and-replace", false);

    private final Label findLabel = new Label("Find Next  ");
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

    private double dragStart;
    private final ParallelTransition fadeOut;
    private final ParallelTransition fadeIn;

    private int findSelectedIndex;
    private final ArrayList<IndexRange> found = new ArrayList<>();

    private final IntegratedTextEditor editor;
    private final StyleSpansFactory<Collection<String>> factory;

    public FindAndReplace(IntegratedTextEditor textEditor) {

        this.editor = textEditor;
        factory = new StyleSpansFactory<>(editor) {

            @Override
            public Collection<SortableStyleSpan<Collection<String>>> genSpans(String text) {
                if (findTextField.getText().length() == 0 || !isShowing())
                    return Collections.emptyList();
                Pattern pattern;
                try {
                    pattern = toggleRegex.getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected")) ?
                            Pattern.compile(findTextField.getText()) : Pattern.compile(Pattern.quote(findTextField.getText()));
                } catch (PatternSyntaxException e) {
                    if (!findTextField.getStyleClass().contains("fr-error"))
                        findTextField.getStyleClass().add("fr-error");
                    return Collections.emptyList();
                }
                ArrayList<SortableStyleSpan<Collection<String>>> styleSpans = new ArrayList<>();
                findTextField.getStyleClass().remove("fr-error");
                Matcher matcher = pattern.matcher(text);
                found.clear();
                while (matcher.find()) {
                    found.add(new IndexRange(matcher.start(), matcher.end()));
                    styleSpans.add(new SortableStyleSpan<>(Collections.singleton("found"), matcher.start(), matcher.end()));
                }
                return styleSpans;
            }

        };

        getChildren().add(findAndReplaceVBox);

        // Transitions
        transition:
        {
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
                this.setVisible(false);
                this.setMouseTransparent(true);
                findAndReplaceVBox.setTranslateY(0);
                textEditor.requestFocus();
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

        // Value Tweaking
        this.setMouseTransparent(false);
        this.setVisible(false);
        findAndReplaceVBox.setVisible(false);
        findAndReplaceVBox.setSpacing(5);
        findHBox.setSpacing(3);
        replaceHBox.setSpacing(3);
        frTop.setLeft(closeFr);
        frTop.setRight(frRightToolBar);
        frTop.setCenter(centerFrText);

        // Listeners
        findLabel.setOnMousePressed(mouseEvent -> {
            if (!found.isEmpty()) {
                findSelectedIndex++;
                if (findSelectedIndex >= found.size()) {
                    findSelectedIndex = 0;
                }
                IndexRange indexRange = found.get(findSelectedIndex);
                textEditor.selectRange(indexRange.getStart(), indexRange.getEnd());
                textEditor.showParagraphAtTop(textEditor.getCurrentParagraph());
                Platform.runLater(textEditor::requestFocus);
            }
        });
        replaceLabel.setOnMousePressed(mouseEvent -> {
            int originalTextLength = textEditor.getText().length();
            for (IndexRange indexRange : found) {
                int difference = textEditor.getText().length() - originalTextLength;
                textEditor.replaceText(indexRange.getStart() + difference, indexRange.getEnd() + difference, replaceTextField.getText());
            }
        });
        findTextField.textProperty().addListener((observableValue, s, t1) -> {
            findTextField.getStyleClass().remove("fr-error");
            textEditor.highlight();
        });
        closeFr.setOnMousePressed(mouseEvent -> showingFindAndReplace.set(false));
        toggleRegex.setOnMousePressed(mouseEvent -> textEditor.highlight());

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
                this.setVisible(true);
                this.setMouseTransparent(false);
                fadeIn.setOnFinished(actionEvent -> {
                    textEditor.highlight();
                    fadeIn.setOnFinished(null);
                });
                fadeIn.play();
            } else if (aBoolean && !t1) {
                fadeOut.play();
                textEditor.highlight();
            }
        });
    }

    public StyleSpansFactory<Collection<String>> getFactory() {
        return factory;
    }

    @Override
    public void addToITE(IntegratedTextEditor ite) {

    }

    @Override
    public void receiveKeyEvent(KeyEvent event, IntegratedTextEditor editor) {
        KeyCode keyCode = event.getCode();
        if ((keyCode == KeyCode.F || keyCode == KeyCode.R || keyCode == KeyCode.H) && event.isControlDown()) {
            showingFindAndReplace.set(!showingFindAndReplace.get());
        }
    }

    public boolean isShowing() {
        return showingFindAndReplace.get();
    }

    public int getFindSelectedIndex() {
        return findSelectedIndex;
    }

    public void setFindSelectionIndex(int i) {
        findSelectedIndex = i;
    }
}
