package sample.test.syntaxPiece.effects;

import sample.test.interpretation.run.CodeState;

import java.util.List;

public interface EffectCreationHandler {

    void activated(CodeState state, List<Object> values, String... args);

}
