package sample.language;

import sample.language.syntax.SyntaxManager;
import sample.language.interpretation.parse.Parser;
import sample.language.interpretation.run.CodeChunk;
import sample.language.syntaxPiece.events.Function;

public final class FXScript {

    public static final Parser PARSER = new Parser();

    public static void restart() {
        Function.ALL_FUNCTIONS.clear();
        SyntaxManager.EFFECT_FACTORIES.clear();
        SyntaxManager.SUPPORTED_TYPES.clear();
        SyntaxManager.EVENT_FACTORIES.clear();
        SyntaxManager.EXPRESSIONS.clear();
        SyntaxManager.HIGHEST.clear();
        SyntaxManager.HIGH.clear();
        SyntaxManager.MEDIUM.clear();
        SyntaxManager.LOW.clear();
        SyntaxManager.LOWEST.clear();
        SyntaxManager.init();

        stop();
        CodeChunk.ALL_CHUNKS.clear();
    }

    public static void stop() {
        for (CodeChunk chunk : CodeChunk.ALL_CHUNKS) {
            chunk.setFinished(true);
        }
    }

}
