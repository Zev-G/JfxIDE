package tmw.me.com.language.syntaxPiece.effects;

import tmw.me.com.language.interpretation.parse.Parser;
import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.language.syntaxPiece.SyntaxPiece;
import tmw.me.com.language.syntaxPiece.SyntaxPieceFactory;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;
import tmw.me.com.language.variable.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class EffectFactory extends Effect implements SyntaxPieceFactory {

    private final String usage;
    private final String regex;
    private EffectCreationHandler effectCreationHandler;
    private Consumer<Parser> finishedParsing = null;

    private final ArrayList<ExpressionFactory<?>> expressionArgs = new ArrayList<>();
    private final ArrayList<Class<?>> classes = new ArrayList<>();

    public EffectFactory(String usage, String regex, EffectCreationHandler effectCreationHandler) {
        this.usage = usage;
        this.regex = regex;
        this.effectCreationHandler = effectCreationHandler;
    }
    public EffectFactory(String regex, EffectCreationHandler effectCreationHandler) {
        this.usage = regex;
        this.regex = regex;
        this.effectCreationHandler = effectCreationHandler;
    }

    public void activate() {
        ArrayList<Object> arguments = new ArrayList<>();
        int loops = 0;
        if (CodeChunk.printing) System.out.println("Activating effect w/ regex: " + regex + "\n  With expressionArgs: " + expressionArgs + "\n  From code: " + code);
        for (Class<?> argClass : classes) {
            if (CodeChunk.printing) System.out.println("On loop class: " + argClass + " On expression factory: " + expressionArgs.get(loops));
            if (expressionArgs.size() > loops &&
                    (argClass
                            .isAssignableFrom(
                                    expressionArgs.get(loops)
                                            .getGenericClass())
                            || expressionArgs.get(loops).getGenericClass().isAssignableFrom(argClass))
                    || argClass == String.class
                    || expressionArgs.get(loops).getGenericClass() == Variable.class) {
                expressionArgs.get(loops).setGenerateClass(argClass);
                arguments.add(expressionArgs.get(loops).activate());
                if (CodeChunk.printing) System.out.println("Got arg " + loops);
                loops++;
            } else {
                arguments.add(null);
            }
        }
        activate(code, arguments.toArray());
    }

    @Override
    public void activate(String inputtedString, Object... args) {
        if (CodeChunk.printing) System.out.println("Activating effect w/ regex: " + regex + " and code: " + code);
        effectCreationHandler.activated(parent.getCodeChunk(), Arrays.asList(args), inputtedString.trim().split(" "));
        if (CodeChunk.printing) System.out.println("Activated effect");
    }

    public ArrayList<Class<?>> getArgs() {
        ArrayList<Class<?>> classes = new ArrayList<>();
        if (CodeChunk.printing) System.out.println("-Getting arguments (for effect with regex: " + regex + ")-");
        StringBuilder builder = new StringBuilder();
        ArrayList<String> pieces = new ArrayList<>();
        for (char c : regex.toCharArray()) {
            if (c == '%' && builder.toString().length() >= 1) {
                if (builder.toString().startsWith("%")) {
                    builder.append(c);
                    pieces.add(builder.toString());
                    builder = new StringBuilder();
                    continue;
                } else if (!builder.toString().contains("%")) {
                    pieces.add(builder.toString());
                    builder = new StringBuilder();
                }
            }
            builder.append(c);
        }
        if (!regex.endsWith("%")) {
            pieces.add(builder.toString());
        }
        if (pieces.isEmpty()) pieces.add(regex);
        for (String var : pieces) {
            Class<?> addClass = SyntaxManager.SYNTAX_MANAGER.SUPPORTED_TYPES.get(var.replaceAll("%", "").trim());
            if (addClass != null || var.startsWith("%")) {
//                if (CodeChunk.printing)
                if (CodeChunk.printing) System.out.println("On arg class: " + addClass + " (from: " + var + ")");
                classes.add(addClass);
            }
        }
        return classes;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    public String getRegex() {
        return regex;
    }

    @Override
    public SyntaxPiece<?> getSyntaxPiece() {
        return this;
    }

    public void setEffectCreationHandler(EffectCreationHandler effectCreationHandler) {
        assert effectCreationHandler != null;
        this.effectCreationHandler = effectCreationHandler;
    }
    public EffectCreationHandler getEffectCreationHandler() {
        return effectCreationHandler;
    }

    public ArrayList<ExpressionFactory<?>> getExpressionArgs() {
        return expressionArgs;
    }

    public ArrayList<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public void setFinishedParsing(Consumer<Parser> finishedParsing) {
        this.finishedParsing = finishedParsing;
    }

    public EffectFactory duplicate() {
        EffectFactory factory = new EffectFactory(usage, regex, effectCreationHandler);
        factory.getExpressionArgs().addAll(expressionArgs);
        factory.setCode(code);
        factory.setParent(parent);
        factory.setFinishedParsing(finishedParsing);
        factory.getClasses().addAll(classes);
        return factory;
    }

    @Override
    public void parsed(Parser parser) {
        classes.clear();
        classes.addAll(getArgs());
        if (finishedParsing != null)
            finishedParsing.accept(parser);
        expressionArgs.forEach(expressionFactory -> expressionFactory.parsed(parser));
    }

}
