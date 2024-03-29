package tmw.me.com.ide.codeEditor.visualcomponents.tooltip;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.texteditor.BehavioralEditor;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.visualcomponents.AutocompletePopup;
import tmw.me.com.ide.codeEditor.visualcomponents.VisualComponent;
import tmw.me.com.ide.settings.IdeSettings;
import tmw.me.com.jfxhelper.NodeUtils;

import java.time.Duration;

public class EditorTooltip extends Popup implements VisualComponent<EditorTooltip> {

    static final long MOUSE_OVER_DELAY = 700;
    static final double TRANS_IN_TIME = 250;

    private final BorderPane content = new BorderPane();
    private final IntegratedTextEditor editor;
    private final TooltipMovedScheduler tooltipScheduler = new TooltipMovedScheduler(this);

    public EditorTooltip(IntegratedTextEditor editor) {
        getContent().add(content);
        this.editor = editor;

        content.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, AutocompletePopup.SHADOW_COLOR, 20, 0.3, 0, 2));
        content.getStyleClass().add("tooltip-top");
        content.getStylesheets().addAll(Ide.STYLE_SHEET);
        NodeUtils.bindParentToIDEStyle(content, IdeSettings.currentTheme);
        content.setMaxHeight(400);

        setAutoHide(true);

        editor.setMouseOverTextDelay(Duration.ofMillis(MOUSE_OVER_DELAY));
        editor.addEventFilter(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, event -> {
            if (!isShowing() && editor.getLanguage().showingTooltip(this, event.getCharacterIndex())) {
                tooltipScheduler.setCurrentHover((Node) event.getSource());
                content.setOpacity(0);
                NodeUtils.transOpacity(content, 1, TRANS_IN_TIME, null);
                Point2D pos = event.getScreenPosition();
                show(editor, pos.getX() + 1, pos.getY() - editor.getFontSize());
            }
        });
        editor.addEventFilter(MouseEvent.MOUSE_MOVED, new TooltipMovedScheduler(this));
    }

    @Override
    public void apply(BehavioralEditor ite) {
        // Not Overridden because it is a visual component which all ITEs have
    }

    @Override
    public void receiveKeyEvent(KeyEvent event, BehavioralEditor textEditor) {

    }

    public BorderPane getNode() {
        return content;
    }

    public void setContent(Node node) {
        content.setCenter(node);
    }

    public IntegratedTextEditor getEditor() {
        return editor;
    }
}
