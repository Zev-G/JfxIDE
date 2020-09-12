package sample.test.interpretation.run;

import sample.test.syntaxPiece.effects.Effect;
import sample.test.syntaxPiece.events.Event;

public class CodePiece {

    private final String code;
    private Effect effect;

    private CodeChunk codeChunk;
    private Event event;

    public CodePiece(String code) {
        this.code = code;
    }

    public void run() {
        if (effect != null) {
            effect.activate();
        } else if (event != null) {
            event.runWhenArrivedTo();
        }
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
        effect.setParent(this);
    }

    public Effect getEffect() {
        return effect;
    }

    public void setCodeChunk(CodeChunk codeChunk) {
        this.codeChunk = codeChunk;
    }

    public CodeChunk getCodeChunk() {
        return codeChunk;
    }

    public String getCode() {
        return code;
    }

    public void setEvent(Event event) {
        this.event = event;
        if (event != null) {
            event.setParent(this);
        }
    }

    public Event getEvent() {
        return event;
    }
}
