package tmw.me.com.language.syntaxPiece;

import tmw.me.com.language.interpretation.parse.Parser;
import tmw.me.com.language.interpretation.run.CodeChunk;

public abstract class SyntaxPiece<T extends SyntaxPiece<?>> {

    protected String code;
    protected CodeChunk state;
    protected String text;

    public abstract T duplicate();
    public abstract void parsed(Parser parser);

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
