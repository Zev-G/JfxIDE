package tmw.me.com.language.syntaxPiece.expressions;


/**
 * <p>
 * This is largely useless now. It is still used to parse more common expressions first but other than that is is not used the way it used to be.
 * </p>
 * The different priority levels that a particular expression can hold.
 * When creating an expression it is best practice to pick the lowest value possible.
 * <p>
 * {@link #LOWEST} The lowest priority that an expression can hold.
 * </p>
 * <p>
 * {@link #LOW} For low priority expressions that can't be lowest.
 * </p>
 * <p>
 * {@link #MEDIUM} For medium priority expressions.
 * </p>
 * <p>
 * {@link #HIGH} For high priority expressions. Be careful when using.
 * </p>
 * <p>
 * {@link #HIGHEST} For the highest priority expressions. Be careful this can override core things like string declaration.
 * </p>
 */
public enum ExpressionPriority {

    /**
     * Lowest priority: Should generally be used when the expression takes multiple complex type parameters.
     */
    LOWEST,

    /**
     * Low priority: This should be used when the expression takes more than two parameters, or when the expression takes a complex parameter.
     */
    LOW,

    /**
     * Medium priority: Use this when the expression takes a parameter.
     */
    MEDIUM,

    /**
     * High priority: Use when the expression takes no parameter.
     */
    HIGH,

    /**
     * Highest priority: Use only for type declaration.
     */
    HIGHEST;

    public int i = 0;

    int y = -3;


}
