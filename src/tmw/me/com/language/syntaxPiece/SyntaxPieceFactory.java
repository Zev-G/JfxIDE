package tmw.me.com.language.syntaxPiece;

import tmw.me.com.language.interpretation.parse.Parser;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;

import java.util.ArrayList;
import java.util.function.Consumer;

public interface SyntaxPieceFactory {

    String getUsage();
    String getRegex();
    SyntaxPiece<?> getSyntaxPiece();
    ArrayList<ExpressionFactory<?>> getExpressionArgs();
    void setFinishedParsing(Consumer<Parser> finishedParsing);


}
