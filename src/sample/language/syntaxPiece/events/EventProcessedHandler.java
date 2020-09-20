package sample.language.syntaxPiece.events;

import sample.language.interpretation.run.CodeState;

import java.util.List;

public interface EventProcessedHandler {

    void processed(CodeState state, List<Object> values, Event event, String... args);

}
