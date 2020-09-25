package tmw.me.com.language.syntaxPiece.expressions;

import tmw.me.com.language.interpretation.run.CodeState;

import java.util.List;

public interface ExpressionCreationHandler<T> {
    T genValue(CodeState state, List<Object> values, String... args);
}
