package sample.test.syntaxPiece;

import sample.test.interpretation.run.CodeChunk;

public abstract class SyntaxPiece<T extends SyntaxPiece<?>> {

    protected String code;
    protected CodeChunk state;
    protected String text;

    public T duplicate() { return null; }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CodeChunk getState() {
        return state;
    }

    public void setState(CodeChunk state) {
        this.state = state;
    }

    public String getText() {
        return text;
    }
}
