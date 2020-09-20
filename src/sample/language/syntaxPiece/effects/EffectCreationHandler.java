package sample.language.syntaxPiece.effects;

import sample.language.interpretation.run.CodeState;

import java.util.List;

public interface EffectCreationHandler {

    void activated(CodeState state, List<Object> values, String... args);

}
