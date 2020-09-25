package tmw.me.com.language.syntaxPiece.effects;

import tmw.me.com.language.interpretation.run.CodeState;

import java.util.List;

public interface EffectCreationHandler {

    void activated(CodeState state, List<Object> values, String... args);

}
