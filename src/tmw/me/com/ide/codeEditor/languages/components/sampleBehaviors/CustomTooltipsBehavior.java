package tmw.me.com.ide.codeEditor.languages.components.sampleBehaviors;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.stage.Popup;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.languages.components.Behavior;

import java.time.Duration;

/**
 * Tooltips
 */
public abstract class CustomTooltipsBehavior extends Behavior {

    private IntegratedTextEditor integratedTextEditor;
    private final Popup popup = new Popup();
    private final EventHandler<MouseOverTextEvent> enterEventHandler = mouseOverTextEvent -> {
        updateVisuals(integratedTextEditor, mouseOverTextEvent);
        Point2D pos = mouseOverTextEvent.getScreenPosition();
        popup.show(integratedTextEditor, pos.getX(), pos.getY());
    };
    private final EventHandler<MouseOverTextEvent> exitEventHandler = mouseOverTextEvent -> popup.hide();

    @Override
    public void apply(IntegratedTextEditor integratedTextEditor) {
        this.integratedTextEditor = integratedTextEditor;
        integratedTextEditor.setMouseOverTextDelay(Duration.ofSeconds(1));
        integratedTextEditor.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, enterEventHandler);
        integratedTextEditor.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, exitEventHandler);
        initiateVisuals(popup);
    }

    @Override
    public void remove(IntegratedTextEditor integratedTextEditor) {
        popup.hide();
        integratedTextEditor.removeEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, enterEventHandler);
        integratedTextEditor.removeEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, exitEventHandler);
    }

    public abstract void updateVisuals(IntegratedTextEditor integratedTextEditor, MouseOverTextEvent mouseOverTextEvent);
    public abstract void initiateVisuals(Popup popup);

}
