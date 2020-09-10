package sample.test.syntaxPiece;

import sample.test.syntaxPiece.expressions.ExpressionFactory;

import java.util.ArrayList;

public interface SyntaxPieceFactory {

    String getRegex();
    SyntaxPiece<?> getSyntaxPiece();
    ArrayList<ExpressionFactory<?>> getExpressionArgs();


}
