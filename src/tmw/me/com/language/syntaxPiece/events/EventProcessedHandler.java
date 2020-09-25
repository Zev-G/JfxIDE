package tmw.me.com.language.syntaxPiece.events;

import tmw.me.com.language.interpretation.run.CodeState;

import java.util.List;

public interface EventProcessedHandler {

    void processed(CodeState state, List<Object> values, Event event, String... args);

}
