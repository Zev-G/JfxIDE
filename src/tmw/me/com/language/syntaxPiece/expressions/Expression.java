package tmw.me.com.language.syntaxPiece.expressions;

import tmw.me.com.language.syntaxPiece.SyntaxPiece;

public abstract class Expression<T> extends SyntaxPiece<Expression<?>> {

    protected final Class<T> thisClass;
    protected SyntaxPiece<?> parent;

    protected Class<?> generateClass;

    protected Expression(Class<T> thisClass) {
        this.thisClass = thisClass;
    }

    public Class<T> getGenericClass() {
        return thisClass;
    }

    public abstract Object activate();

    public abstract Object activateForValue(String input, Object... args);

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setParent(SyntaxPiece<?> parent) {
        this.parent = parent;
    }

    public SyntaxPiece<?> getParent() {
        return parent;
    }

    public void setGenerateClass(Class<?> generateClass) {
        this.generateClass = generateClass;
    }

    public Class<?> getGenerateClass() {
        return generateClass;
    }
}
