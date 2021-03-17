package tmw.me.com.language.syntaxPiece.events.statements;

import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.syntaxPiece.events.Event;

public class ElseStatement extends Event {

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
    public ElseStatement duplicate() {
        ElseStatement elseStatement = new ElseStatement();
        elseStatement.setCode(code);
        elseStatement.setParent(parent);
        elseStatement.setRunChunk(getRunChunk());
        return elseStatement;
    }


}
