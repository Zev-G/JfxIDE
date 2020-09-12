package sample.test.interpretation.run;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class CodeChunk extends CodeState {

    public static final ArrayList<CodeChunk> ALL_CHUNKS = new ArrayList<>();

    public static final boolean printing = false;

    private final ArrayList<CodePiece> pieces = new ArrayList<>();
    private boolean finished = false;


    public CodeChunk(CodePiece... pieces) {
        this(Arrays.asList(pieces));
    }
    public CodeChunk(Collection<CodePiece> pieces) {
        this.pieces.addAll(pieces);
        ALL_CHUNKS.add(this);
    }

    public void run() {
        finished = false;
        System.out.println("Running Code Chunk: " + code + " current variables: " + variables.values() + " parent: " + parent + (parent != null ? "Parent variables: " + parent.getVariables().values() : ""));
        for (CodePiece piece : pieces) {
            if (!finished) {
                System.out.println("-=-Running Piece: (" + piece.getCode() + ") -=-");
                piece.setCodeChunk(this);
                piece.run();
            }
        }
    }

    public void runPiece(CodePiece piece) {
        if (piece != null) {
            pieces.add(piece);
            piece.setCodeChunk(this);
            piece.run();
        } else {
            System.err.println("Invalid code attempted to be ran");
        }
    }

    public void setGlobal(boolean b) {
        this.global = b;
    }

    public void addPiece(CodePiece piece) {
        piece.setCodeChunk(this);
        this.pieces.add(piece);
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    public boolean isFinished() {
        return finished;
    }

    public ArrayList<CodePiece> getPieces() {
        return pieces;
    }

    @Override
    public CodeChunk duplicateWithoutVariables() {
        return duplicateWithoutVariables(parent);
    }

    @Override
    public CodeChunk duplicateWithoutVariables(CodeState parent) {
        CodeChunk newState = new CodeChunk(pieces);
        newState.setGlobal(global);
        newState.setCode(code);
        newState.setParent(parent);
        newState.getLocalExpressions().addAll(getLocalExpressions());
        ((ArrayList<CodeState>) this.children.clone()).forEach(codeState -> newState.getChildren().add(codeState.duplicateWithoutVariables(newState)));
        newState.setNewVariable(newVariable);
        return newState;
    }

}
