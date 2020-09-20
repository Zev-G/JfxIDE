package sample.language.syntaxPiece.expressions;

import sample.language.interpretation.run.CodeState;

import java.util.List;

public interface ExpressionCreationHandler<T> {
    T genValue(CodeState state, List<Object> values, String... args);
}
