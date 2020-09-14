package sample.test.syntaxPiece.events.statements;

import sample.test.interpretation.run.CodeChunk;
import sample.test.syntaxPiece.events.Event;

public class ElseStatement extends Event {

    public ElseStatement() {

    }

    @Override
    public void run() {
        CodeChunk chunk = this.getParent().getCodeChunk();
        boolean ifStatement = chunk.getLastEvent() instanceof IfStatement
                && !(((IfStatement) chunk.getLastEvent()).isRanLastTime());
        boolean elseIfStatement = chunk.getLastEvent() instanceof ElseIfStatement && (!((ElseIfStatement) chunk.getLastEvent()).isRanLastTime());
        if (ifStatement || elseIfStatement) {
            super.run();
        }
    }

    @Override
    public Event duplicate() {
        return new ElseStatement();
    }
}
