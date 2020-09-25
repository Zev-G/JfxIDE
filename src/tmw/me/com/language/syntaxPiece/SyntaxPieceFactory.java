package tmw.me.com.language.syntaxPiece;

import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;

import java.util.ArrayList;

public interface SyntaxPieceFactory {

    String getUsage();
    String getRegex();
    SyntaxPiece<?> getSyntaxPiece();
    ArrayList<ExpressionFactory<?>> getExpressionArgs();


}
