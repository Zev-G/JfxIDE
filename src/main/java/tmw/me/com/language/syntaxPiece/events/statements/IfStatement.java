package tmw.me.com.language.syntaxPiece.events.statements;

import tmw.me.com.language.syntaxPiece.events.Event;
import tmw.me.com.language.syntaxPiece.expressions.Expression;

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

    @Override
    public IfStatement duplicate() {
        IfStatement ifStatement = new IfStatement(condition);
        ifStatement.setCode(code);
        ifStatement.setRunChunk(getRunChunk());
        ifStatement.setParent(parent);
        return ifStatement;
    }


}
