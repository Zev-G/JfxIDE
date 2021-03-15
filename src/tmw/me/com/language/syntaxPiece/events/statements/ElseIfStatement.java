package tmw.me.com.language.syntaxPiece.events.statements;

import tmw.me.com.language.syntaxPiece.events.Event;
import tmw.me.com.language.syntaxPiece.expressions.Expression;

public class ElseIfStatement extends Event {

    private Expression<Boolean> condition;
    private boolean ranLastTime;

    public ElseIfStatement() {

    }

    @Override
    public void run() {
        boolean ifStatement = this.state.getLastEvent() instanceof IfStatement && !(((IfStatement) this.state.getLastEvent()).isRanLastTime());
        boolean elseIfStatement = this.state.getLastEvent() instanceof ElseIfStatement && (!((ElseIfStatement) this.state.getLastEvent()).isRanLastTime());
        if (ifStatement || elseIfStatement) {
            if ((Boolean) condition.activate()) {
                ranLastTime = true;
                super.run();
            } else {
                ranLastTime = false;
            }
        }
    }

    public Expression<Boolean> getCondition() {
        return condition;
    }

    public void setCondition(Expression<Boolean> condition) {
        this.condition = condition;
    }

    public boolean isRanLastTime() {
        return ranLastTime;
    }

    @Override
    public ElseIfStatement duplicate() {
        ElseIfStatement elseIfStatement = new ElseIfStatement();
        elseIfStatement.setCode(code);
        elseIfStatement.setCondition(condition);
        elseIfStatement.setRunChunk(getRunChunk());
        elseIfStatement.setParent(parent);
        elseIfStatement.setTopLevel(isTopLevel());
        return elseIfStatement;
    }

}
