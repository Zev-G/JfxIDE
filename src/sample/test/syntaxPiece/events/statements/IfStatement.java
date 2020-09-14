package sample.test.syntaxPiece.events.statements;

import sample.test.syntaxPiece.events.Event;
import sample.test.syntaxPiece.expressions.Expression;

public class IfStatement extends Event {

    private Expression<Boolean> condition;
    private boolean ranLastTime;

    public IfStatement(Expression<Boolean> condition) {
        setCondition(condition);
    }

    @Override
    public void run() {
        this.getParent().getCodeChunk().setLastEvent(this);
        if ((Boolean) condition.activate()) {
            ranLastTime = true;
            super.run();
        } else {
            ranLastTime = false;
        }
    }

    public Expression<Boolean> getCondition() {
        return condition;
    }
    public void setCondition(Expression<Boolean> condition) {
        this.condition = condition;
        condition.setParent(this);
    }

    public boolean isRanLastTime() {
        return ranLastTime;
    }
}
