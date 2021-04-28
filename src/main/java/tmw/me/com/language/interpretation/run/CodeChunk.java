package tmw.me.com.language.interpretation.run;

import tmw.me.com.language.syntaxPiece.events.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public class CodeChunk extends CodeChunkBase implements Cloneable {

    public static final ArrayList<CodeChunk> ALL_CHUNKS = new ArrayList<>();

    public static final boolean printing = false;

    private final ArrayList<CodePiece> pieces = new ArrayList<>();
    private boolean finished = false;

    private Consumer<CodePiece> ranPiece;

    public CodeChunk(CodePiece... pieces) {
        this(Arrays.asList(pieces));
    }

    public CodeChunk(Collection<CodePiece> pieces) {
        this.pieces.addAll(pieces);
        ALL_CHUNKS.add(this);
    }

    public void run() {
        finished = false;
//        System.out.println("Running run chunk: " + this);
//        System.out.println("Variables Before: " + variables);
        for (CodePiece piece : pieces) {
            if (!finished) {
//                System.out.println("-=-Running Piece: (" + piece.getCode() + ") -=-");
                if (ranPiece != null) ranPiece.accept(piece);
                piece.setCodeChunk(this);
                piece.run();
            }
        }
//        System.out.println("Variables After (" + code + "): " + variables);
        if (ranPiece != null) ranPiece.accept(null);
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

    public void setRanPiece(Consumer<CodePiece> ranPiece) {
        this.ranPiece = ranPiece;
        children.forEach(codeState -> {
            if (codeState instanceof CodeChunk) ((CodeChunk) codeState).setRanPiece(ranPiece);
        });
    }

    public Consumer<CodePiece> getRanPiece() {
        return ranPiece;
    }

    @Override
    public CodeChunk duplicateWithoutVariables() {
        return duplicateWithoutVariables(parent, holder);
    }

    @Override
    public CodeChunk duplicateWithoutVariables(CodeChunkBase parent, Event holder) {
        CodeChunk newState = new CodeChunk(pieces);
        newState.setGlobal(global);
        newState.setCode(code);
        newState.setParent(parent);
        newState.setHolder(holder);
        if (holder != null) {
            holder.setRunChunk(newState);
        }
        newState.getLocalExpressions().addAll(getLocalExpressions());
        ((ArrayList<CodeChunkBase>) this.children.clone()).forEach(codeState -> {
            if (codeState != null) {
                Event dupedChildHolder = codeState.getHolder().duplicate();
                CodePiece piece = codeState.getHolder().getParent();
                codeState.duplicateWithoutVariables(newState, dupedChildHolder);
                CodePiece replacementPiece = new CodePiece(piece.getCode(), piece.getLine());
                replacementPiece.setEvent(dupedChildHolder);
                pieces.set(pieces.indexOf(piece), replacementPiece);
            }
        });
        newState.setNewVariable(newVariable);
        newState.setRanPiece(ranPiece);
        return newState;
    }


    @Override
    public String toString() {
        return "CodeChunk" + "@" + hashCode() + "{" +
                "holder=" + holder +
                ", code='" + code + '\'' +
                ", children=" + children +
                '}';
    }
}
