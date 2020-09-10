package sample.test.syntaxPiece.events;

import sample.test.interpretation.run.CodeState;

import java.util.List;

public interface EventProcessedHandler {

    void processed(CodeState state, List<Object> values, Event event, String... args);

}
