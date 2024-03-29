package tmw.me.com.language.syntaxPiece.events;

import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.interpretation.run.CodePiece;
import tmw.me.com.language.syntaxPiece.SyntaxPiece;

public abstract class Event extends SyntaxPiece<Event> {

    private boolean isTopLevel;
    private CodeChunk runChunk;

    protected CodePiece parent;

    public void setRunChunk(CodeChunk chunk) {
        this.runChunk = chunk;
    }

    public CodeChunk getRunChunk() {
        return runChunk;
    }

    public void run() {
        this.getParent().getCodeChunk().setLastEvent(this);
        runChunk.run();
    }

    public void runWhenArrivedTo() {
        run();
    }


    public CodePiece getParent() {
        return parent;
    }

    public void setParent(CodePiece parent) {
        this.parent = parent;
    }

    public void setTopLevel(boolean topLevel) {
        isTopLevel = topLevel;
    }

    public boolean isTopLevel() {
        return isTopLevel;
    }


}
