package tmw.me.com.language.syntaxPiece.effects;

import tmw.me.com.language.interpretation.run.CodeChunk;

import java.util.List;

public interface EffectCreationHandler {

    void activated(CodeChunk state, List<Object> values, String... args);

}
