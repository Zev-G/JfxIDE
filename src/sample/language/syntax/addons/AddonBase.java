package sample.language.syntax.addons;

import sample.language.syntax.SyntaxManager;
import sample.language.syntaxPiece.effects.EffectFactory;
import sample.language.syntaxPiece.events.WhenEventFactory;
import sample.language.syntaxPiece.expressions.ExpressionFactory;
import sample.language.syntaxPiece.expressions.ExpressionPriority;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the Base for all created addons. All native addons can be found in the addons folder.
 * <p>If you are creating your own addon use this code below to initiate it</p>
 * <br/>
 * <pre>
 *     static {
 *         {@link SyntaxManager#ADDON_CLASSES}.add(YOUR_CLASS);
 *     }
 * </pre>
 */
public abstract class AddonBase {

    protected static final HashMap<String, Class<?>> SUPPORTED_TYPES = SyntaxManager.SUPPORTED_TYPES;
    protected static final HashMap<ExpressionPriority, HashMap<Class<?>, ArrayList<ExpressionFactory<?>>>> EXPRESSIONS = SyntaxManager.EXPRESSIONS;
    protected static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> LOWEST = SyntaxManager.LOWEST;
    protected static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> LOW = SyntaxManager.LOW;
    protected static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> MEDIUM = SyntaxManager.MEDIUM;
    protected static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> HIGH = SyntaxManager.HIGH;
    protected static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> HIGHEST = SyntaxManager.HIGHEST;

    protected static final ArrayList<EffectFactory> EFFECT_FACTORIES = SyntaxManager.EFFECT_FACTORIES;
    protected static final ArrayList<WhenEventFactory> EVENT_FACTORIES = SyntaxManager.EVENT_FACTORIES;

    public abstract void addTypes();
    public abstract void addSyntax();

}
