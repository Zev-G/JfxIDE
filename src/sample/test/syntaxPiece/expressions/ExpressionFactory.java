package sample.test.syntaxPiece.expressions;

import sample.test.interpretation.run.CodeChunk;
import sample.test.interpretation.SyntaxManager;
import sample.test.syntaxPiece.SyntaxPiece;
import sample.test.syntaxPiece.SyntaxPieceFactory;
import sample.test.syntaxPiece.effects.Effect;
import sample.test.syntaxPiece.events.Event;
import sample.test.variable.Variable;

import java.util.ArrayList;
import java.util.Arrays;

public class ExpressionFactory<T> extends Expression<T> implements SyntaxPieceFactory {

    private final String regex;
    private ExpressionCreationHandler<T> expressionCreationHandler;

    private ArrayList<ExpressionFactory<?>> expressionArgs = new ArrayList<>();

    public ExpressionFactory(String regex, ExpressionCreationHandler<T> expressionCreationHandler, Class<T> thisClass) {
        super(thisClass);
        this.regex = regex;
        this.expressionCreationHandler = expressionCreationHandler;
    }
    public ExpressionFactory(String text, String regex, ExpressionCreationHandler<T> expressionCreationHandler, Class<T> thisClass) {
        super(thisClass);
        this.text = text;
        this.regex = regex;
        this.expressionCreationHandler = expressionCreationHandler;
    }

    public String getRegex() {
        return this.regex;
    }

    @Override
    public SyntaxPiece<?> getSyntaxPiece() {
        return this;
    }

    @Override
    public Object activate() {
        ArrayList<Object> args = new ArrayList<>();
        int loops = 0;
        if (CodeChunk.printing) System.out.println("Activating expression factory: " + this);
        for (Class<?> argClass : getArgs()) {
            if (argClass != null) {
                if (CodeChunk.printing) System.out.println("On loop class: " + argClass + " On expression factory: " + expressionArgs.get(loops));
                if (expressionArgs.size() > loops && expressionArgs.size() > 0 &&
                        (argClass.isAssignableFrom(expressionArgs.get(loops).getGenericClass())
                                || expressionArgs.get(loops).getGenericClass().isAssignableFrom(argClass))
                        || argClass == String.class
                        || expressionArgs.get(loops).getGenericClass() == Variable.class) {
                    expressionArgs.get(loops).setGenerateClass(argClass);
                    args.add(expressionArgs.get(loops).activate());
                    loops++;
                } else {
                    args.add(null);
                }
            } else {
                System.out.println("Arg class equals null, expression arg in slot: " + expressionArgs.get(loops));
            }
        }
        if (CodeChunk.printing) System.out.println("Arguments are finished for expression factory: " + this + " (args: " + args + ")");
        return activateForValue(code, args.toArray());
    }

    @Override
    public Object activateForValue(String inputtedString, Object... args) {
        CodeChunk chunk = state;
        SyntaxPiece<?> loopParent = parent;
        if (CodeChunk.printing) System.out.println("Getting chunk, parent: " + loopParent);
        while (chunk == null) {
            if (loopParent instanceof Effect) {
                chunk = ((Effect) loopParent).getParent().getCodeChunk();
            } else if (loopParent instanceof Expression<?>) {
                loopParent = ((Expression<?>) loopParent).getParent();
            } else if (loopParent instanceof Event) {
                chunk = ((Event) loopParent).getParent().getCodeChunk();
                if (CodeChunk.printing) System.out.println("Chunk: " + chunk);
            } else {
                System.out.println("Stuck in loop!: " + loopParent);
                break;
            }
        }
        if (CodeChunk.printing) System.out.println("Inputted String: " + inputtedString);
        Object objectForValue = expressionCreationHandler.
                genValue(chunk,
                        Arrays.asList(args),
                        inputtedString.trim().split(" "));
        if (CodeChunk.printing) System.out.println("Arrived at object: " + objectForValue);
        if (objectForValue != null && generateClass != Variable.class && objectForValue.getClass() == Variable.class) {
            if (CodeChunk.printing) System.out.println("Parsing variable as object");
            objectForValue = ((Variable<?>) objectForValue).getValue();
        }
        if (CodeChunk.printing) System.out.println("Returning object: " + objectForValue);
        return objectForValue;
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
            Class<?> addClass = SyntaxManager.SUPPORTED_TYPES.get(var.replaceAll("%", "").trim());
            if (addClass != null || var.startsWith("%")) {
                if (CodeChunk.printing) System.out.println("On arg class: " + addClass + " (from: " + var + ")");
                classes.add(addClass);
            }
        }
        return classes;
    }

    public void setExpressionCreationHandler(ExpressionCreationHandler<T> expressionCreationHandler) {
        assert expressionCreationHandler != null;
        this.expressionCreationHandler = expressionCreationHandler;
    }
    public ExpressionCreationHandler<T> getExpressionCreationHandler() {
        return expressionCreationHandler;
    }

    public ArrayList<ExpressionFactory<?>> getExpressionArgs() {
        return expressionArgs;
    }

    public void forChildren(ExpressionEffector effector) {
        for (ExpressionFactory<?> factory : expressionArgs) {
            effector.forEffect(factory);
            factory.forChildren(effector);
        }
    }

    @Override
    public ExpressionFactory<T> duplicate() {
        ExpressionFactory<T> expressionFactory = new ExpressionFactory<>(this.regex, expressionCreationHandler, thisClass);
        expressionFactory.setCode(code);
        expressionFactory.setParent(parent);
        expressionFactory.setGenerateClass(generateClass);
        return expressionFactory;
    }

    @Override
    public String toString() {
        return "ExpressionFactory{" +
                "regex='" + regex + '\'' +
                ", genericClass=" + getGenericClass() +
                '}';
    }

    @Override
    public String getText() {
        if (text.equals("")) return regex;
        return super.getText();
    }
}
