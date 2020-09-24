package sample.language.syntax.addons;

import sample.language.syntaxPiece.effects.EffectFactory;
import sample.language.syntaxPiece.events.WhenEventFactory;
import sample.language.syntaxPiece.expressions.ExpressionFactory;
import sample.language.syntaxPiece.expressions.ExpressionPriority;

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
