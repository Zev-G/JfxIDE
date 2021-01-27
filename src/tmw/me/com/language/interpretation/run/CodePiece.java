package tmw.me.com.language.interpretation.run;

import tmw.me.com.language.interpretation.parse.Parser;
import tmw.me.com.language.interpretation.parse.error.ParseError;
import tmw.me.com.language.syntaxPiece.effects.Effect;
import tmw.me.com.language.syntaxPiece.events.Event;

import java.util.ArrayList;
import java.util.Collection;

public class CodePiece {

    private final int line;
    private final String code;
    private Effect effect;

    private CodeChunk codeChunk;
    private Event event;

    public CodePiece(String code, int line) {
        this.code = code;
        this.line = line;
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
    public int getLine() {
        return line;
    }

    public final ArrayList<ParseError> parsed(Parser parser) {
        ArrayList<ParseError> parseErrors = new ArrayList<>();
        if (effect != null) {
            Collection<ParseError> effectParseErrors = effect.parsed(parser);
            if (effectParseErrors != null && !effectParseErrors.isEmpty()) {
                parseErrors.addAll(effectParseErrors);
            }
        }
        else if (event != null) {
            Collection<ParseError> eventParseErrors = event.parsed(parser);
            if (eventParseErrors != null && !eventParseErrors.isEmpty()) {
                parseErrors.addAll(eventParseErrors);
            }
        }
        return parseErrors;
    }

}
