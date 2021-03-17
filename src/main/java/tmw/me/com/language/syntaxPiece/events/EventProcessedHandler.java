package tmw.me.com.language.syntaxPiece.events;

import tmw.me.com.language.interpretation.run.CodeChunk;

import java.util.List;

public interface EventProcessedHandler {

    void processed(CodeChunk state, List<Object> values, Event event, String... args);

}
