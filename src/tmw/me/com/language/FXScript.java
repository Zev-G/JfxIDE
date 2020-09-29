package tmw.me.com.language;

import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.language.interpretation.parse.Parser;
import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.syntaxPiece.events.Function;

public final class FXScript {

    public static final Parser PARSER = new Parser(SyntaxManager.SYNTAX_MANAGER);


    public static void restart() {
        restart(SyntaxManager.SYNTAX_MANAGER);
    }
    public static void restart(SyntaxManager syntaxManager) {
        Function.ALL_FUNCTIONS.clear();
        syntaxManager.EFFECT_FACTORIES.clear();
        syntaxManager.SUPPORTED_TYPES.clear();
        syntaxManager.EVENT_FACTORIES.clear();
        syntaxManager.EXPRESSIONS.clear();
        syntaxManager.HIGHEST.clear();
        syntaxManager.HIGH.clear();
        syntaxManager.MEDIUM.clear();
        syntaxManager.LOW.clear();
        syntaxManager.LOWEST.clear();
        syntaxManager.ADDONS.clear();
        syntaxManager.ADDON_CLASSES.clear();
        syntaxManager.init();

        stop();
        CodeChunk.ALL_CHUNKS.clear();
    }

    public static void stop() {
        for (CodeChunk chunk : CodeChunk.ALL_CHUNKS) {
            chunk.setFinished(true);
        }
    }

}
