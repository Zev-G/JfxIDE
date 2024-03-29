package tmw.me.com.language.syntaxPiece.events;

import tmw.me.com.language.interpretation.parse.Parser;
import tmw.me.com.language.interpretation.parse.error.ParseError;
import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.language.syntaxPiece.SyntaxPiece;
import tmw.me.com.language.syntaxPiece.SyntaxPieceFactory;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;
import tmw.me.com.language.variable.Variable;

import java.util.ArrayList;
import java.util.function.Consumer;

public class WhenEventFactory extends Event implements SyntaxPieceFactory {

    private final String usage;
    private final String regex;
    private EventProcessedHandler eventProcessedHandler;
    private Consumer<Parser> finishedParsing = null;

    private final ArrayList<ExpressionFactory<?>> expressionArgs = new ArrayList<>();
    private final ArrayList<Class<?>> classes = new ArrayList<>();

    public WhenEventFactory(String usage, String regex, EventProcessedHandler eventProcessedHandler) {
        this.regex = regex;
        this.usage = usage + (usage.endsWith(":") ? "" : ":");
        this.eventProcessedHandler = eventProcessedHandler;
    }

    public WhenEventFactory(String regex, EventProcessedHandler eventProcessedHandler) {
        this.regex = regex;
        this.usage = regex + ":";
        this.eventProcessedHandler = eventProcessedHandler;
    }

    public void reached() {
        ArrayList<Object> arguments = new ArrayList<>();
        int loops = 0;
        if (CodeChunk.printing)
            System.out.println("Activating effect w/ regex: " + regex + "\n  With expressionArgs: " + expressionArgs);
        for (Class<?> argClass : classes) {
            if (CodeChunk.printing)
                System.out.println("On loop class: " + argClass + " On expression factory: " + expressionArgs.get(loops));
            if (expressionArgs.size() > loops &&
                    (argClass.isAssignableFrom(expressionArgs.get(loops).getGenericClass())
                            || expressionArgs.get(loops).getGenericClass().isAssignableFrom(argClass))
                    || argClass == String.class
                    || expressionArgs.get(loops).getGenericClass() == Variable.class) {
                expressionArgs.get(loops).setGenerateClass(argClass);
                arguments.add(expressionArgs.get(loops).activate());
                loops++;
            } else {
                arguments.add(null);
            }
        }
        eventProcessedHandler.processed(parent.getCodeChunk(), arguments, this, code.split(" "));
    }

    @Override
    public void runWhenArrivedTo() {
        reached();
    }

    public ArrayList<Class<?>> getArgs() {
        ArrayList<Class<?>> classes = new ArrayList<>();
        if (CodeChunk.printing) System.out.println("-Getting arguments (for effect with regex: " + regex + ")-");
        for (String var : regex.split("(?=%([A-z]+)%)|(?<=% )")) {
            Class<?> addClass = SyntaxManager.SYNTAX_MANAGER.SUPPORTED_TYPES.get(var.replaceAll("%", "").trim());
            if (addClass != null || var.startsWith("%")) {
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

    public void setEventProcessedHandler(EventProcessedHandler eventProcessedHandler) {
        this.eventProcessedHandler = eventProcessedHandler;
    }

    public ArrayList<ExpressionFactory<?>> getExpressionArgs() {
        return expressionArgs;
    }

    @Override
    public WhenEventFactory duplicate() {
        WhenEventFactory event = new WhenEventFactory(usage, regex, eventProcessedHandler);
        event.setRunChunk(getRunChunk());
        event.setParent(parent);
        event.setTopLevel(isTopLevel());
        event.getExpressionArgs().addAll(expressionArgs);
        event.setCode(code);
        event.setFinishedParsing(finishedParsing);
        event.getClasses().addAll(classes);
        return event;
    }

    public void setFinishedParsing(Consumer<Parser> finishedParsing) {
        this.finishedParsing = finishedParsing;
    }

    public Consumer<Parser> getFinishedParsing() {
        return finishedParsing;
    }

    public ArrayList<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public ArrayList<ParseError> parsed(Parser parser) {
        classes.clear();
        classes.addAll(getArgs());
        if (finishedParsing != null)
            finishedParsing.accept(parser);
        ArrayList<ParseError> parseErrors = new ArrayList<>();
        expressionArgs.forEach(expressionFactory -> parseErrors.addAll(expressionFactory.parsed(parser)));
        return parseErrors;
    }

}
