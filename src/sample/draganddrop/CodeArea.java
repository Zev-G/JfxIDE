package sample.draganddrop;

import javafx.scene.layout.Pane;
import sample.test.interpretation.Interpreter;

public class CodeArea extends Pane {

    public CodeArea() {
        this.getChildren().add(new CodeLineVisual(Interpreter.EFFECT_FACTORIES.get(12)));
        this.getChildren().add(new CodeLineVisual(Interpreter.EFFECT_FACTORIES.get(10)));
    }

}
