package sample.test.syntaxPiece;

import sample.test.syntaxPiece.expressions.ExpressionFactory;

import java.util.ArrayList;

public interface SyntaxPieceFactory {

    String getUsage();
    String getRegex();
    SyntaxPiece<?> getSyntaxPiece();
    ArrayList<ExpressionFactory<?>> getExpressionArgs();


}
