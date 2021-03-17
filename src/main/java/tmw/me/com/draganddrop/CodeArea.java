package tmw.me.com.draganddrop;

import javafx.scene.layout.Pane;
import tmw.me.com.language.syntax.SyntaxManager;

public class CodeArea extends Pane {

    public CodeArea() {
        this.getChildren().add(new CodeLineVisual(SyntaxManager.SYNTAX_MANAGER.EFFECT_FACTORIES.get(12)));
        this.getChildren().add(new CodeLineVisual(SyntaxManager.SYNTAX_MANAGER.EFFECT_FACTORIES.get(10)));
    }

}
