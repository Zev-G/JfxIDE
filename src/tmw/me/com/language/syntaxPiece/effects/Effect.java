package tmw.me.com.language.syntaxPiece.effects;

import tmw.me.com.language.interpretation.run.CodePiece;
import tmw.me.com.language.syntaxPiece.SyntaxPiece;

public abstract class Effect extends SyntaxPiece<Effect> {

    protected CodePiece parent;


    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public abstract void activate(String inputtedString, Object... args);

    public abstract void activate();

    public void setParent(CodePiece parent) {
        this.parent = parent;
    }
    public CodePiece getParent() {
        return parent;
    }



}
