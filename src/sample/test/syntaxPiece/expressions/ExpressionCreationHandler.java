package sample.test.syntaxPiece.expressions;

import sample.test.interpretation.run.CodeState;

import java.util.List;

public interface ExpressionCreationHandler<T> {
    T genValue(CodeState state, List<Object> values, String... args);
}
