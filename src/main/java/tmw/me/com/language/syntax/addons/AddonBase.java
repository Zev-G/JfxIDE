package tmw.me.com.language.syntax.addons;

import tmw.me.com.language.syntaxPiece.effects.EffectFactory;
import tmw.me.com.language.syntaxPiece.events.WhenEventFactory;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionPriority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;

public abstract class AddonBase {

    public abstract HashMap<String, Class<?>> addTypes();

    public abstract EnumMap<ExpressionPriority, HashMap<Class<?>, ArrayList<ExpressionFactory<?>>>> addExpressions(Collection<Class<?>> classes);

    public abstract ArrayList<EffectFactory> addEffects();

    public abstract ArrayList<WhenEventFactory> addEvents();

}
