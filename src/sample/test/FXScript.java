package sample.test;


import sample.test.interpretation.Interpreter;
import sample.test.interpretation.run.CodeChunk;
import sample.test.syntaxPiece.events.Function;

public final class FXScript {

    public static void restart() {
        Function.ALL_FUNCTIONS.clear();
        Interpreter.EFFECT_FACTORIES.clear();
        Interpreter.SUPPORTED_TYPES.clear();
        Interpreter.EVENT_FACTORIES.clear();
        Interpreter.EXPRESSIONS.clear();
        Interpreter.HIGHEST.clear();
        Interpreter.HIGH.clear();
        Interpreter.MEDIUM.clear();
        Interpreter.LOW.clear();
        Interpreter.LOWEST.clear();
        Interpreter.init();
        stop();
        CodeChunk.ALL_CHUNKS.clear();
    }

    public static void stop() {
        for (CodeChunk chunk : CodeChunk.ALL_CHUNKS) {
            chunk.setFinished(true);
        }
    }

}
