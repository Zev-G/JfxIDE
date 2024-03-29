package tmw.me.com.language.syntaxPiece.expressions;

import tmw.me.com.language.interpretation.run.CodeChunk;

import java.util.List;

public interface ExpressionCreationHandler<T> {
    T genValue(CodeChunk state, List<Object> values, String... args);
}
