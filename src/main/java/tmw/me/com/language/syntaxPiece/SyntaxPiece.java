package tmw.me.com.language.syntaxPiece;

import tmw.me.com.language.interpretation.parse.Parser;
import tmw.me.com.language.interpretation.parse.error.ParseError;
import tmw.me.com.language.interpretation.run.CodeChunk;

import java.util.ArrayList;

public abstract class SyntaxPiece<T extends SyntaxPiece<?>> {

    protected String code;
    protected CodeChunk state;
    protected String text;

    public abstract T duplicate();

    public ArrayList<ParseError> parsed(Parser parser) {
        return null;
    }

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
