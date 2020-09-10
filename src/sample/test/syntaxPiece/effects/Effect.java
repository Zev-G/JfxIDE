package sample.test.syntaxPiece.effects;

import sample.test.interpretation.run.CodePiece;
import sample.test.syntaxPiece.SyntaxPiece;

public abstract class Effect extends SyntaxPiece<Effect> {

    protected CodePiece parent;


    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public void activate(String inputtedString, Object... args) {

    }

    public void activate() {

    }

    public void setParent(CodePiece parent) {
        this.parent = parent;
    }
    public CodePiece getParent() {
        return parent;
    }



}
