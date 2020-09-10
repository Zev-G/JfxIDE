package sample.test.syntaxPiece.events;

import sample.test.interpretation.run.CodeChunk;
import sample.test.interpretation.run.CodePiece;
import sample.test.syntaxPiece.SyntaxPiece;

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



    @Override
    public Event duplicate() {
        Event event = new Event(){};
        event.setRunChunk(runChunk);
        event.setParent(parent);
        event.setTopLevel(isTopLevel);
        return event;
    }

    public Object clone() {
        return duplicate();
    }
}
