package sample.language.syntaxPiece.effects;

import sample.language.interpretation.run.CodePiece;
import sample.language.syntaxPiece.SyntaxPiece;

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
