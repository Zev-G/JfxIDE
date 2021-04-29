package tmw.me.com.language.interpretation.parse;

import tmw.me.com.language.interpretation.parse.error.ParseError;
import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.interpretation.run.CodePiece;
import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.language.syntaxPiece.SyntaxPiece;
import tmw.me.com.language.syntaxPiece.SyntaxPieceFactory;
import tmw.me.com.language.syntaxPiece.effects.Effect;
import tmw.me.com.language.syntaxPiece.effects.EffectFactory;
import tmw.me.com.language.syntaxPiece.events.Event;
import tmw.me.com.language.syntaxPiece.events.Function;
import tmw.me.com.language.syntaxPiece.events.WhenEventFactory;
import tmw.me.com.language.syntaxPiece.events.statements.ElseStatement;
import tmw.me.com.language.syntaxPiece.events.statements.IfStatement;
import tmw.me.com.language.syntaxPiece.expressions.Expression;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;
import tmw.me.com.language.variable.Variable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class Parser {

    private Consumer<ParseError> errorHandler;
    private final SyntaxManager syntaxManager;

    public Parser(SyntaxManager syntaxManager) {
        this.syntaxManager = syntaxManager;
        errorHandler = gotten -> {
            gotten.print();
            System.out.println("Printer from here");
        };
    }

    public Parser(SyntaxManager syntaxManager, Consumer<ParseError> errorHandler) {
        this.errorHandler = errorHandler;
        this.syntaxManager = syntaxManager;
    }

    public Consumer<ParseError> getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(Consumer<ParseError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public static boolean PRINTING_CHUNK_PARSING_INFO = false;

    /**
     * @param code The code from which the CodeChunk is parsed.
     * @return A parsed CodeChunk that is ready to be ran.
     */
    public final CodeChunk parseChunk(String code, File file) {
        return parseChunk(code, null, file, 0);
    }

    public final CodeChunk parseChunk(String code, CodeChunk parent, File file, int lineOffset) {
        CodeChunk chunk = new CodeChunk();
        chunk.setCode("Top");
        boolean inEvent = false;
        int eventStart = 0;
        Event lastEvent = null;
        StringBuilder eventCode = new StringBuilder();
        String[] pieces = code.split("\n");
        String lastEventCode = "";
        for (int i = 0; i < pieces.length + 1; i++) {
            String line = "";
            if (pieces.length > i) {
                line = pieces[i];
                line = line.replaceFirst(" {2}", "\t");
            }
            if (PRINTING_CHUNK_PARSING_INFO)
                System.out.println("On number: " + i + " text: " + line + " offset: " + lineOffset);
            if (!line.replaceAll("\\s", "").startsWith("#") && !line.replaceAll("\\s", "").equals("")) {
                CodePiece addPiece = null;
                if (!line.startsWith("\t") && !line.endsWith(":")) {
                    addPiece = parsePiece(line.trim(), file, i + 1 + lineOffset);
                    addPiece.setCodeChunk(parent);
                    if (addPiece.getEffect() != null && ((EffectFactory) addPiece.getEffect()).getRegex().startsWith("$"))
                        addPiece.run();
                }
                if (line.startsWith("\t")) {
                    inEvent = true;
                    if (eventStart == 0)
                        eventStart = i;
                    eventCode.append(line.replaceFirst("\t", "")).append("\n");
                } else {
                    if (PRINTING_CHUNK_PARSING_INFO) System.out.println("Line: " + line + " doesn't start with a tab");
                    if (inEvent) {
                        if (PRINTING_CHUNK_PARSING_INFO) System.out.println("Exiting event");
                        CodeChunk runChunk = parseChunk(eventCode.toString(), null, file, eventStart + lineOffset);
                        runChunk.setCode(lastEventCode);
                        runChunk.setParent(chunk);
                        if (lastEvent != null) {
                            lastEvent.setRunChunk(runChunk);
                            runChunk.setHolder(lastEvent);
                        }
                        eventCode = new StringBuilder();
                    }
                    eventStart = 0;
                    inEvent = false;
                }
                if (line.endsWith(":") && !line.startsWith("\t")) {
                    lastEvent = parseEvent(line, file, i + 1 + lineOffset);
                    lastEventCode = line;
                    if (PRINTING_CHUNK_PARSING_INFO)
                        System.out.println("Got event for line: " + line + " (event: " + lastEvent + ")");
                    addPiece = new CodePiece(line, i + 1 + lineOffset);
                    assert lastEvent != null;
                    addPiece.setEvent(lastEvent);
                }
                if (addPiece != null) {
                    chunk.addPiece(addPiece);
                    for (ParseError error : addPiece.parsed(this)) {
                        errorHandler.accept(error);
                    }
                }
            }
        }
        if (inEvent && lastEvent != null) {
            CodeChunk runChunk = parseChunk(eventCode.toString(), null, file, eventStart);
            runChunk.setCode(lastEventCode);
            runChunk.setParent(chunk);
            lastEvent.setRunChunk(runChunk);
        }
        return chunk;
    }

    public final CodePiece parsePiece(String code, File file, int lineNum) {
        Effect effect = parseLine(code, file, lineNum);
        CodePiece piece = new CodePiece(code, lineNum);
        if (effect != null) {
            piece.setEffect(effect);
        }
        return piece;
    }


    /**
     * <b>How it works:</b>
     * <p>
     * This method will loop through every inputted {@link SyntaxPieceFactory} and will then separate the text of said factory's REGEX
     * into pieces where each piece is separated by an expression (i.e. "%anything-here%"). For every piece of text: if the piece of
     * text is not an expression, we test if the literal (no "%type%") regex matches the corresponding code. If it does not we will {@code continue} the loop.
     * Assuming it does we will then check if the last piece was an expression. If it was we will save the text in between so that it can be
     * parsed as an expression later. We continue this process until either: the loop continues due to the code not matching what is required
     * by the loop SyntaxPieceFactory, or we reach the end of the regex. Should the end of the regex be met we then start to attempt to parse
     * each separated {@link Expression} if any one of these expressions either doesn't match or isn't of the correct type we continue to the
     * next factory. Once we have all the expressions we return the {@link SyntaxPiece} that's gathered from the SyntaxPieceFactory.
     * If we continue to the end of the list of factories we return null;
     * </p>
     * <p>
     * *Any line starting with (if (CodeChunk.printing)) is temporary.
     * </p>
     *
     * @param code           The code from which the code will be extracted from.
     * @param possiblePieces The SyntaxPieceFactories that will be tested.
     * @return A parsed syntax piece. This could be a Event, Effect or Expression.
     */
    public <T extends SyntaxPieceFactory> T parseSyntaxPiece(String code, ArrayList<T> possiblePieces, File file, int line) {
        code = code.trim();
        if (CodeChunk.printing)
            System.out.println("Parsing syntax piece: " + code + " with possible pieces: " + possiblePieces);
        pieces:
        for (SyntaxPieceFactory syntaxPieceFactory : possiblePieces) {
            if (CodeChunk.printing) System.out.println("On factory w/ regex: " + syntaxPieceFactory.getRegex());
            // Important variable creation
            String codeBuffer = code;
            ArrayList<String> pieces = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            String regex = syntaxPieceFactory.getRegex().startsWith("$") ? syntaxPieceFactory.getRegex().substring(1) : syntaxPieceFactory.getRegex();
            ArrayList<String> argTypes = new ArrayList<>();
            boolean lastWasExpression = false;
            // Split into pieces separated by whether or not they're an expression
            // TODO Add support for optional expression arguments
            for (char c : regex.toCharArray()) {
                if (c == '%' && builder.toString().length() >= 1) {
                    if (builder.toString().startsWith("%")) {
                        builder.append(c);
                        pieces.add(builder.toString());
                        argTypes.add(builder.toString());
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
            if (CodeChunk.printing) System.out.println("Pieces: " + pieces);
            ArrayList<String> nestedExpressionTexts = new ArrayList<>();
            // Loop through texts figuring out what texts are expressions and which ones arent while simultaneously separating them
            for (int i = 0; i < pieces.size() + 1; i++) {
                // Init piece and get then get the correct piece from the list of pieces.
                // We check if the ArrayList size isn't to large because the algorithm loops one more time than the length of the ArrayList so that we can catch the last expression.
                String piece = "";
                if (pieces.size() > i) {
                    piece = pieces.get(i);
                }
                // Due to how the code is separated all expression pieces will begin and end with a percentage sign.
                boolean isAnExpression = piece.startsWith("%");
                if (CodeChunk.printing)
                    System.out.println("On piece: " + piece + "(" + i + ") which " + (isAnExpression ? "is" : "isn't") + " an expression");
                // This adds the text for the last expression. We wait until after because the way we figure out when we got to the other side of the expression is by checking for the next piece of text.
                // This is also why we loop one more time. See above for a bit more info on this.
                if (lastWasExpression) {
                    StringBuilder expressionCode = new StringBuilder();
                    String beforeCodeBuffer = codeBuffer;
                    Pattern pattern = Pattern.compile(piece);
                    while (!pattern.matcher(codeBuffer).lookingAt()) {
                        if (codeBuffer.equals("")) {
                            if (CodeChunk.printing)
                                System.out.println("Continuing to next factory due to codeBuffer not being valid");
                            continue pieces;
                        }
                        expressionCode.append(codeBuffer.charAt(0));
                        codeBuffer = codeBuffer.substring(1);
                    }
                    String chosen = (expressionCode.toString().length() > 0 ? expressionCode.toString() : beforeCodeBuffer);
                    nestedExpressionTexts.add(chosen);
                }
                // Refactoring the codeBuffer if the code doesn't match our expression factory's regex.
                if (!isAnExpression) {
                    if (!Pattern.compile(piece).matcher(codeBuffer).lookingAt()) {
                        if (CodeChunk.printing)
                            System.out.println("Continuing to the next factory on factory with regex: " + syntaxPieceFactory.getRegex());
                        continue pieces;
                    }
                    codeBuffer = codeBuffer.replaceFirst(piece, "");
                }
                lastWasExpression = isAnExpression;
            }
            // Once this point has been reached we have all the nested expressions
            ArrayList<ExpressionFactory<?>> nestedExpressionFactories = new ArrayList<>();
            String codeWithoutExpressions = code;
            int i = 0;
            for (String expression : nestedExpressionTexts) {
                if (CodeChunk.printing)
                    System.out.println("-Getting nested expression for text: (" + expression + ")-");
                ExpressionFactory<?> nestedExpressionFactory = parseExpression(expression, file, line);
                if (nestedExpressionFactory != null) {
                    if (!syntaxManager.SUPPORTED_TYPES.get(argTypes.get(i).replaceAll("%", "")).isAssignableFrom(nestedExpressionFactory.getGenericClass()) && !Variable.class.isAssignableFrom(nestedExpressionFactory.getGenericClass()) && nestedExpressionFactory.getGenericClass() != Object.class) {
                        continue pieces;
                    }
                    nestedExpressionFactory.setCode(expression);
                    if (CodeChunk.printing)
                        System.out.println("Got nested expression for text: (" + Pattern.quote(expression) + "). Now replacing string in codeWithoutExpressions(" + codeWithoutExpressions + ")");
                    codeWithoutExpressions = codeWithoutExpressions.replaceFirst(Pattern.quote(expression), "");
                } else {
                    if (CodeChunk.printing) System.out.println("Continuing cause bad expression");
                    continue pieces;
                }
                nestedExpressionFactories.add(nestedExpressionFactory);
                i++;
            }
            String parsedRegex = syntaxPieceFactory.getRegex().replaceFirst("\\$", "");
            parsedRegex = parsedRegex.replaceAll("%(.*?)%", "");
            if (!codeWithoutExpressions.matches(parsedRegex)) {
                if (CodeChunk.printing)
                    System.out.println("Continuing due to code not matching. Code w/out Expressions: " + codeWithoutExpressions + " Parsed Regex: " + parsedRegex);
                continue;
            }
            if (CodeChunk.printing) System.out.println("Nested Expression factories: " + nestedExpressionFactories);
            SyntaxPiece<?> syntaxPiece = syntaxPieceFactory.getSyntaxPiece().duplicate();
            syntaxPiece.setCode(code);
            nestedExpressionFactories.forEach(expressionFactory -> expressionFactory.setParent(syntaxPiece));
            syntaxPieceFactory = (SyntaxPieceFactory) syntaxPiece;
            syntaxPieceFactory.getExpressionArgs().addAll(nestedExpressionFactories);
            return (T) syntaxPieceFactory.getSyntaxPiece();
        }
        return null;
    }

    public <T extends SyntaxPieceFactory> T parseSyntaxPiece(String code, ArrayList<T> possiblePieces) {
        return parseSyntaxPiece(code, possiblePieces, null, 0);
    }

    /**
     * @param code The code which will be parsed and the effect will be extracted from.
     * @return The effect that is parsed from the given code.
     */
    public Effect parseLine(String code, File file, int lineNum) {
        Effect parsedEffect = parseSyntaxPiece(code.trim(), syntaxManager.EFFECT_FACTORIES, file, lineNum);
        if (parsedEffect == null) {
            errorHandler.accept(new ParseError(lineNum, code, "Unrecognized Effect", file));
        }
        return parsedEffect;
    }

    public Effect parseLine(String code) {
        return parseLine(code, null, 0);
    }

    /**
     * @param code The code from which the Event is parsed. Should be only one line. For example: "when {button} is pressed:"
     * @return A parsed event that doesn't contain it's CodeChunk.
     */
    public Event parseEvent(String code, File file, int lineNum) {
        // Different types of Events:
        //  * If statements
        //  * Else statements
        //  * List loops
        //  * Number loops
        //  * Functions
        //  * Run when x
        code = code.trim();
        if (code.startsWith("if")) {
            Expression<?> expression = parseExpression(code.replaceFirst("if ", "").replaceAll(":", ""), file, lineNum);
            if (expression != null && expression.getGenericClass() == Boolean.class) {
                return new IfStatement((Expression<Boolean>) expression);
            }
        } else if (code.startsWith("else")) {
            if (!code.contains("if")) {
                return new ElseStatement();
            }
        } else if (code.startsWith("function")) {
            ArrayList<String> spaces = new ArrayList<>(Arrays.asList(code.split(" ")));
            spaces.remove(0);
            if (spaces.size() > 1 && spaces.get(1).contains("(")) {
                spaces.remove(0);
            }
            StringBuilder spacesConnected = new StringBuilder();
            int i = 0;
            for (String space : spaces) {
                i++;
                spacesConnected.append(space);
                if (i != spaces.size()) {
                    spacesConnected.append(" ");
                }
            }
            String functionName = spacesConnected.toString().replaceAll("\\((.*?)\\)", "").replaceAll(":", "");
            String params = spacesConnected.toString().split("\\(")[1].replaceAll(":", "").replaceAll("\\)", "");
            ArrayList<Function.FunctionArgument> functionArguments = new ArrayList<>();
            for (String param : params.split(",( *)")) {
                if (param.split(" ").length > 1) {
                    Class<?> type = syntaxManager.SUPPORTED_TYPES.get(param.split(" ")[0].toLowerCase());
                    if (type != null) {
                        System.out.println("Param: " + param + "(" + type.toGenericString() + ")");
                        String name = param.split(" ")[1];
                        functionArguments.add(new Function.FunctionArgument(type, name));
                    }
                }
            }
            Function<?> function = new Function<>(functionName);
            function.getArguments().addAll(functionArguments);
            return function;
        } else {
            String justCode = code.replaceAll(":", "").replaceFirst("\\$", "");
            if (CodeChunk.printing) System.out.println("Getting event from code: " + justCode);
            WhenEventFactory syntaxPiece = parseSyntaxPiece(justCode, syntaxManager.EVENT_FACTORIES, file, lineNum);
            // Error Handling
            if (syntaxPiece == null) {
                errorHandler.accept(new ParseError(lineNum, code, "Unrecognized Event", file));
                return null;
            }
            if (code.startsWith("$") || syntaxPiece.getRegex().startsWith("$")) {
                syntaxPiece.setParent(new CodePiece("", lineNum));
                syntaxPiece.runWhenArrivedTo();
                syntaxPiece.setEventProcessedHandler((state, values, event, args) -> {
                });
            }
            return syntaxPiece;
        }
        errorHandler.accept(new ParseError(lineNum, code, "Unrecognized Event", file));
        return null;
    }

    public Event parseEvent(String code) {
        return parseEvent(code, null, 0);
    }

    /**
     * @param code Inputted code, should already be trimmed and improved.
     * @return The parsed Expression. Will return null if it isn't a valid Expression.
     */
    public ExpressionFactory<?> parseExpression(String code, File file, int lineNum) {
        code = code.trim();
        if (CodeChunk.printing) System.out.println("--Parsing Expression (code: " + code + ")--");
        HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> fullMap = new HashMap<>();
        addHashMapToHashMap(fullMap, syntaxManager.HIGHEST);
        addHashMapToHashMap(fullMap, syntaxManager.HIGH);
        addHashMapToHashMap(fullMap, syntaxManager.MEDIUM);
        addHashMapToHashMap(fullMap, syntaxManager.LOW);
        addHashMapToHashMap(fullMap, syntaxManager.LOWEST);
        ArrayList<ExpressionFactory<?>> allExpressions = new ArrayList<>();
        fullMap.values().forEach(allExpressions::addAll);
        return parseSyntaxPiece(code, allExpressions, file, lineNum);
    }

    public ExpressionFactory<?> parseExpression(String code) {
        return parseExpression(code, null, 0);
    }

    /**
     * @param addTo   The HashMap which addFrom is added to.
     * @param addFrom The HashMap that is added to addTo
     * @param <T>     The type of object stored in the arraylist
     */
    private <T> void addHashMapToHashMap(HashMap<Class<?>, ArrayList<T>> addTo, HashMap<Class<?>, ArrayList<T>> addFrom) {
        for (Class<?> loopClass : addFrom.keySet()) {
            addTo.computeIfAbsent(loopClass, k -> new ArrayList<>());
            addTo.get(loopClass).addAll(addFrom.get(loopClass));
        }
    }

    /**
     * This method is used to get a list of strings separated by percentage signs.
     *
     * @param code The different expressions will be separated from this string
     * @return A {@code ArrayList} of strings where the expressions start and end with a %
     */
    public ArrayList<String> generateExpressionPiecesFromString(String code) {
        ArrayList<String> separatedStrings = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (char character : code.toCharArray()) {
            if (character == '%') {
                if (builder.toString().startsWith("%")) {
                    builder.append(character);
                    separatedStrings.add(builder.toString());
                    builder = new StringBuilder();
                    continue;
                } else {
                    separatedStrings.add(builder.toString());
                    builder = new StringBuilder();
                }
            }
            builder.append(character);
        }
        if (!builder.toString().equals("")) {
            separatedStrings.add(builder.toString());
        }
        return separatedStrings;
    }

}